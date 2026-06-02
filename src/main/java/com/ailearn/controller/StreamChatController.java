package com.ailearn.controller;

import com.ailearn.service.StreamChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * <h1>🌊 流式对话 Controller（第 2 课配套）</h1>
 *
 * <p>流式（Streaming）是 AI 对话应用区别于传统 API 的核心特性。
 * 使用 SSE（Server-Sent Events）协议实现。</p>
 *
 * <h2>SSE 协议要点</h2>
 * <table border="1">
 *   <tr><th>项目</th><th>说明</th></tr>
 *   <tr><td>Content-Type</td><td>text/event-stream</td></tr>
 *   <tr><td>方向</td><td>单向：服务器 → 客户端</td></tr>
 *   <tr><td>数据格式</td><td>data: &lt;内容&gt;\n\n</td></tr>
 *   <tr><td>结束标记</td><td>data: [DONE]\n\n</td></tr>
 *   <tr><td>重连</td><td>浏览器自动重连（EventSource API）</td></tr>
 *   <tr><td>对比 WebSocket</td><td>更简单，不需要双向通信的场景首选 SSE</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/chat")
public class StreamChatController {

    private static final Logger log = LoggerFactory.getLogger(StreamChatController.class);
    private final StreamChatService streamChatService;

    public StreamChatController(StreamChatService streamChatService) {
        this.streamChatService = streamChatService;
    }

    /**
     * <h3>流式对话接口（浏览器可直接访问）</h3>
     *
     * <pre>{@code
     * // 浏览器 EventSource 调用
     * const es = new EventSource('/api/chat/stream?message=请介绍一下Java的GC机制');
     * es.onmessage = (e) => {
     *     if (e.data === '[DONE]') { es.close(); return; }
     *     document.getElementById('chat').innerHTML += e.data;
     * };
     * }</pre>
     *
     * <pre>{@code
     * // curl 调用
     * curl -N "http://localhost:8080/api/chat/stream?message=你好"
     * }</pre>
     *
     * @param message 用户消息（query 参数）
     * @return SSE 格式的 Token 流
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(
            @RequestParam(defaultValue = "你好，请介绍一下你自己") String message) {

        log.info("🌊 流式请求：{}", message);

        return streamChatService.streamChat(message)
                // map：将每个 Token 包装成 ServerSentEvent
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build())
                // concatWith：在所有 Token 之后拼接一个 [DONE] 标记
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder()
                                .data("[DONE]")
                                .build()
                ))
                .doOnError(error ->
                        log.error("❌ 流式输出错误：{}", error.getMessage()))
                // onErrorResume：出错时也返回 [DONE] 标记，优雅关闭
                .onErrorResume(error -> Flux.just(
                        ServerSentEvent.<String>builder()
                                .data("[ERROR] " + error.getMessage())
                                .build(),
                        ServerSentEvent.<String>builder()
                                .data("[DONE]")
                                .build()
                ));
    }

    /**
     * <h3>带角色设定的流式对话</h3>
     */
    @GetMapping(value = "/stream/role", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChatWithRole(
            @RequestParam String message,
            @RequestParam(defaultValue = "Java 面试官") String role) {

        log.info("🌊 流式角色请求：role={}, message={}", role, message);

        return streamChatService.streamChatWithRole(message, role)
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build())
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder().data("[DONE]").build()
                ));
    }

    /**
     * <h3>文案生成器（演示具体业务场景）</h3>
     *
     * <pre>{@code
     * GET /api/chat/stream/copywriting?topic=一款Java学习App&style=幽默&wordLimit=200
     * }</pre>
     */
    @GetMapping(value = "/stream/copywriting", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateCopywriting(
            @RequestParam String topic,
            @RequestParam(defaultValue = "专业") String style,
            @RequestParam(defaultValue = "150") int wordLimit) {

        log.info("📝 文案生成请求：topic={}, style={}, limit={}", topic, style, wordLimit);

        return streamChatService.generateCopywriting(topic, style, wordLimit)
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build())
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder().data("[DONE]").build()
                ));
    }
}
