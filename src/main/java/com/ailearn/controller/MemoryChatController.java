package com.ailearn.controller;

import com.ailearn.model.ChatRequest;
import com.ailearn.model.ChatResponse;
import com.ailearn.service.MemoryChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * <h1>🧠 对话记忆 Controller（第 3 课配套）</h1>
 *
 * <p>演示多轮对话记忆：同一 conversationId 下的对话，AI 会记住历史。</p>
 *
 * <h2>接口列表</h2>
 * <table border="1">
 *   <tr><th>接口</th><th>方法</th><th>说明</th></tr>
 *   <tr><td>/api/chat/memory</td><td>POST</td><td>带记忆的对话</td></tr>
 *   <tr><td>/api/chat/memory/{convId}/clear</td><td>DELETE</td><td>清除记忆</td></tr>
 *   <tr><td>/api/chat/memory/{convId}/size</td><td>GET</td><td>查看记忆条数</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/chat")
public class MemoryChatController {

    private static final Logger log = LoggerFactory.getLogger(MemoryChatController.class);
    private final MemoryChatService memoryChatService;

    public MemoryChatController(MemoryChatService memoryChatService) {
        this.memoryChatService = memoryChatService;
    }

    /**
     * <h3>带记忆的对话</h3>
     *
     * <p>使用方法：</p>
     * <pre>{@code
     * // 第1轮
     * POST /api/chat/memory
     * { "message": "我叫张三", "conversationId": "my-session" }
     *
     * // 第2轮（同一 conversationId）
     * POST /api/chat/memory
     * { "message": "我叫什么？", "conversationId": "my-session" }
     * // AI 回答："你叫张三" ← 记住了！
     *
     * // 换个 conversationId 就是新对话
     * POST /api/chat/memory
     * { "message": "我叫什么？", "conversationId": "new-session" }
     * // AI 回答："我不知道" ← 没有记忆
     * }</pre>
     */
    @PostMapping("/memory")
    public ChatResponse chatWithMemory(@RequestBody ChatRequest request) {
        log.info("🧠 记忆对话请求 [{}]：{}",
                request.getConversationId(), request.getMessage());

        try {
            String reply = memoryChatService.chat(
                    request.getConversationId(), request.getMessage());
            return ChatResponse.success(reply, request.getConversationId());
        } catch (Exception e) {
            log.error("❌ 记忆对话失败", e);
            return ChatResponse.error("对话失败：" + e.getMessage());
        }
    }

    /**
     * <h3>清除指定会话的记忆</h3>
     *
     * <p>当用户想"重新开始"时调用。</p>
     */
    @DeleteMapping("/memory/{conversationId}/clear")
    public ChatResponse clearMemory(@PathVariable String conversationId) {
        log.info("🗑️ 清除记忆 [{}]", conversationId);
        memoryChatService.clearMemory(conversationId);
        return ChatResponse.success("记忆已清除", conversationId);
    }

    /**
     * <h3>查看指定会话的历史消息数量</h3>
     */
    @GetMapping("/memory/{conversationId}/size")
    public ChatResponse getMemorySize(@PathVariable String conversationId) {
        int size = memoryChatService.getMemorySize(conversationId);
        return ChatResponse.success("当前记忆条数：" + size, conversationId);
    }
}
