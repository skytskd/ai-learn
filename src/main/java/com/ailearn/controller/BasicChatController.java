package com.ailearn.controller;

import com.ailearn.model.ChatRequest;
import com.ailearn.model.ChatResponse;
import com.ailearn.service.BasicChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * <h1>📡 基础对话 Controller（第 1 课配套）</h1>
 *
 * <p>演示最基本的 AI 对话接口。所有接口都是同步的（阻塞），
 * 适合短回答或 API 编排场景。</p>
 *
 * <h2>接口列表</h2>
 * <table border="1">
 *   <tr><th>接口</th><th>方法</th><th>说明</th></tr>
 *   <tr><td>/api/chat/simple</td><td>POST</td><td>最简对话</td></tr>
 *   <tr><td>/api/chat/role</td><td>POST</td><td>带角色设定</td></tr>
 *   <tr><td>/api/chat/exam</td><td>POST</td><td>面试题生成（模板）</td></tr>
 *   <tr><td>/api/chat/temperature</td><td>POST</td><td>Temperature 实验</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/chat")
public class BasicChatController {

    private static final Logger log = LoggerFactory.getLogger(BasicChatController.class);
    private final BasicChatService basicChatService;

    public BasicChatController(BasicChatService basicChatService) {
        this.basicChatService = basicChatService;
    }

    /**
     * <h3>最简单的 AI 对话</h3>
     *
     * <pre>{@code
     * POST /api/chat/simple
     * Content-Type: application/json
     *
     * {
     *   "message": "什么是 Spring AI？"
     * }
     * }</pre>
     */
    @PostMapping("/simple")
    public ChatResponse simpleChat(@RequestBody ChatRequest request) {
        log.info("📥 基础对话请求：{}", request.getMessage());
        try {
            String reply = basicChatService.simpleChat(request.getMessage());
            return ChatResponse.success(reply, null);
        } catch (Exception e) {
            log.error("❌ 对话失败", e);
            return ChatResponse.error("对话失败：" + e.getMessage());
        }
    }

    /**
     * <h3>带角色设定的对话</h3>
     *
     * <p>通过 System Prompt 设定 AI 的角色和行为。</p>
     */
    @PostMapping("/role")
    public ChatResponse chatWithRole(@RequestBody ChatRequest request) {
        log.info("📥 角色对话请求：{}", request.getMessage());
        try {
            String reply = basicChatService.chatWithSystemPrompt(request.getMessage());
            return ChatResponse.success(reply, request.getConversationId());
        } catch (Exception e) {
            log.error("❌ 对话失败", e);
            return ChatResponse.error("对话失败：" + e.getMessage());
        }
    }

    /**
     * <h3>面试题生成（PromptTemplate 演示）</h3>
     *
     * <pre>{@code
     * POST /api/chat/exam
     * Content-Type: application/json
     *
     * {
     *   "message": "多线程",
     *   "topic": "多线程与并发",
     *   "difficulty": "高级"
     * }
     * }</pre>
     * <p>可选参数通过 URL 查询参数传递：topic, difficulty。</p>
     */
    @PostMapping("/exam")
    public ChatResponse generateExamQuestion(
            @RequestBody ChatRequest request,
            @RequestParam(defaultValue = "Java 基础") String topic,
            @RequestParam(defaultValue = "中级") String difficulty) {
        log.info("📥 面试题生成请求：topic={}, difficulty={}", topic, difficulty);
        try {
            String reply = basicChatService.chatWithTemplate(
                    request.getMessage(), topic, difficulty);
            return ChatResponse.success(reply, request.getConversationId());
        } catch (Exception e) {
            log.error("❌ 生成失败", e);
            return ChatResponse.error("生成失败：" + e.getMessage());
        }
    }

    /**
     * <h3>Temperature 参数实验</h3>
     *
     * <p>用同一个问题，不同的 Temperature 值调用两次，
     * 观察回答的差异。</p>
     */
    @PostMapping("/temperature")
    public ChatResponse chatWithTemperature(
            @RequestBody ChatRequest request,
            @RequestParam(defaultValue = "0.7") double temperature) {
        log.info("📥 Temperature 实验请求：temp={}", temperature);
        try {
            String reply = basicChatService.chatWithTemperature(
                    request.getMessage(), temperature);
            return ChatResponse.success(reply, request.getConversationId());
        } catch (Exception e) {
            log.error("❌ 对话失败", e);
            return ChatResponse.error("对话失败：" + e.getMessage());
        }
    }
}
