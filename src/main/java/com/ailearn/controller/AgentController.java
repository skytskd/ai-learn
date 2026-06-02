package com.ailearn.controller;

import com.ailearn.model.ChatRequest;
import com.ailearn.model.ChatResponse;
import com.ailearn.service.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * <h1>🤖 AI Agent Controller（第 5 课配套）</h1>
 *
 * <p>Agent 是可自主决策和调用工具的 AI 程序。
 * 它可以调用天气查询、计算器、数据库查询等工具来完成复杂任务。</p>
 *
 * <h2>接口列表</h2>
 * <table border="1">
 *   <tr><th>接口</th><th>方法</th><th>说明</th></tr>
 *   <tr><td>/api/agent/chat</td><td>POST</td><td>Agent 对话（带工具调用）</td></tr>
 *   <tr><td>/api/agent/tools</td><td>GET</td><td>查看可用工具列表</td></tr>
 * </table>
 *
 * <h2>可以试试这样问 Agent：</h2>
 * <ol>
 *   <li>"今天杭州天气怎么样？适合出去玩吗？" — 天气工具</li>
 *   <li>"帮我算一下 12345 * 6789 等于多少" — 计算器工具</li>
 *   <li>"研发部有多少人？薪资最高的是谁？" — 数据库工具</li>
 *   <li>"杭州天气适合出去玩吗？如果去的话帮我算一下行程花费" — 多工具组合</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * <h3>Agent 对话接口</h3>
     *
     * <p>AI 会自动判断是否需要调用工具，并在需要时自动调用。</p>
     *
     * <pre>{@code
     * POST /api/agent/chat
     * { "message": "明天杭州天气如何？" }
     *
     * // AI 内部流程：
     * // 1. 判断需要调用 WeatherTool
     * // 2. 调用 getWeather("杭州")
     * // 3. 获取结果："明天杭州晴..."
     * // 4. 返回自然语言回答
     * }</pre>
     */
    @PostMapping("/chat")
    public ChatResponse agentChat(@RequestBody ChatRequest request) {
        log.info("🤖 Agent 对话请求：{}", request.getMessage());

        try {
            String reply = agentService.chat(request.getMessage());
            return ChatResponse.success(reply, request.getConversationId());
        } catch (Exception e) {
            log.error("❌ Agent 对话失败", e);
            return ChatResponse.error("Agent 对话失败：" + e.getMessage());
        }
    }

    /**
     * <h3>查看可用工具列表</h3>
     */
    @GetMapping("/tools")
    public ChatResponse listTools() {
        String tools = agentService.getAvailableTools();
        return ChatResponse.success(tools, null);
    }
}
