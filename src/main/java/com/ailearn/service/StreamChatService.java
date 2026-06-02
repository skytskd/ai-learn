package com.ailearn.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * <h1>🌊 第 2 课：流式输出服务</h1>
 *
 * <p>流式输出是 AI 应用区别于传统 API 的核心特性之一。
 * 它让大模型的回答像打字机一样逐字输出，极大提升用户体验。</p>
 *
 * <h2>核心知识点</h2>
 * <ol>
 *   <li><b>ChatClient.stream()</b> —— 返回 Flux&lt;String&gt;，逐 Token 推送</li>
 *   <li><b>Flux（响应式流）</b> —— 非阻塞的异步数据流</li>
 *   <li><b>SSE（Server-Sent Events）</b> —— 单向推送通道</li>
 *   <li><b>call() vs stream()</b> —— 同步完整 vs 流式增量</li>
 * </ol>
 *
 * <h2>call() vs stream() 对比</h2>
 * <table border="1">
 *   <tr><th>特性</th><th>call()</th><th>stream()</th></tr>
 *   <tr><td>返回类型</td><td>String（完整文本）</td><td>Flux&lt;String&gt;（Token 序列）</td></tr>
 *   <tr><td>用户体验</td><td>等待后一次性展示</td><td>像打字机逐字出现</td></tr>
 *   <tr><td>首字响应</td><td>全部生成后才返回</td><td>第一个 Token 就返回</td></tr>
 *   <tr><td>服务器压力</td><td>时间到 → 一次返回</td><td>持续推送（SSE）</td></tr>
 *   <tr><td>适用场景</td><td>短回答、API 编排</td><td>对话界面、长回答</td></tr>
 * </table>
 *
 * <h2>SSE 协议简述</h2>
 * <pre>
 *   HTTP 请求头 Accept: text/event-stream
 *   响应格式：
 *     data: {"chunk": "你"}
 *     data: {"chunk": "好"}
 *     data: {"chunk": "！"}
 *     data: [DONE]
 * </pre>
 *
 * @see reactor.core.publisher.Flux
 * @see org.springframework.http.MediaType#TEXT_EVENT_STREAM
 */
@Service
public class StreamChatService {

    private final ChatClient chatClient;

    public StreamChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * <h3>流式对话（推荐用于 Web 聊天界面）</h3>
     *
     * <p>这个方法返回一个 Flux&lt;String&gt;，每个元素是一个 Token 片段。
     * 前端可以通过 EventSource API 接收流式数据。</p>
     *
     * <h3>前端代码示例</h3>
     * <pre>{@code
     * const eventSource = new EventSource('/api/chat/stream?message=你好');
     * eventSource.onmessage = (event) => {
     *   if (event.data === '[DONE]') {
     *     eventSource.close();
     *     return;
     *   }
     *   // 逐字追加到聊天界面
     *   chatBox.innerHTML += event.data;
     * };
     * }</pre>
     *
     * <h3>后端响应流程</h3>
     * <ol>
     *   <li>ChatClient.stream().content() 返回 Flux&lt;String&gt;</li>
     *   <li>Controller 将 Flux 包装成 SSE 格式的 ServerSentEvent</li>
     *   <li>Spring WebFlux 将 Flux 序列化为 text/event-stream</li>
     *   <li>浏览器 EventSource 逐条接收 data 行</li>
     * </ol>
     *
     * @param userMessage 用户消息
     * @return Token 的响应式流
     */
    public Flux<String> streamChat(String userMessage) {
        // stream() 替代 call()：返回 Flux 而不是 String
        // content() 提取 Flux<String> 而不是单个 String
        return chatClient.prompt()
                .system("你是一名友好的 AI 助手，用中文简洁回答。")
                .user(userMessage)
                .stream()
                .content();
    }

    /**
     * <h3>带 System Prompt 的流式对话</h3>
     *
     * <p>流式输出中同样可以设置 System Prompt，和同步调用完全一致。</p>
     *
     * @param userMessage 用户消息
     * @param role        角色描述（如 "Java 面试官"、"代码审查员"）
     * @return Token 的响应式流
     */
    public Flux<String> streamChatWithRole(String userMessage, String role) {
        return chatClient.prompt()
                .system(String.format("""
                    你是一名 %s。请使用以下规则：
                    - 用中文回答
                    - 回答要专业、有条理
                    - 代码示例要完整可运行
                    """, role))
                .user(userMessage)
                .stream()
                .content();
    }

    /**
     * <h3>文案生成器（展示流式输出的创意场景）</h3>
     *
     * <p>这是一个具体的业务场景示例。
     * 用流式输出展示 AI 生成内容的过程。</p>
     *
     * @param topic   生成主题
     * @param style   文案风格（如 "正式"、"幽默"、"文艺"）
     * @param wordLimit 字数限制
     * @return Token 流
     */
    public Flux<String> generateCopywriting(String topic, String style, int wordLimit) {
        return chatClient.prompt()
                .system(String.format("""
                    你是一名资深文案策划，擅长创作各种风格的文案。
                    请使用「%s」风格，字数控制在 %d 字以内。
                    """, style, wordLimit))
                .user("请针对「" + topic + "」创作一段营销文案。")
                .stream()
                .content();
    }
}
