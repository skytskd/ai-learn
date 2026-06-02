package com.ailearn.service;

import com.ailearn.tool.CalculatorTool;
import com.ailearn.tool.WeatherTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.stereotype.Service;

/**
 * <h1>🤖 第 5 课：AI Agent 服务（工具调用 / Function Calling）</h1>
 *
 * <p><b>Agent（智能体）</b>是 AI 应用的高级形态。
 * 它不仅仅是"回答问题"，而是能<b>自主决策、调用工具、完成复杂任务</b>。</p>
 *
 * <h2>核心概念</h2>
 * <ol>
 *   <li><b>Agent</b> —— 能自主决策和行动的 AI 程序</li>
 *   <li><b>Tool / Function</b> —— Agent 可以调用的外部工具（API、数据库、计算器等）</li>
 *   <li><b>ReAct 模式</b> —— Reasoning + Acting，思考和行动交替进行</li>
 *   <li><b>Tool Description</b> —— 决定 AI 是否能正确选择工具的说明书</li>
 * </ol>
 *
 * <h2>Agent 工作流程（ReAct 模式）</h2>
 * <pre>
 *   用户: "明天杭州天气如何？适合出门吗？"
 *     │
 *     ▼
 *   Thought: 需要查询杭州明天的天气
 *     │
 *     ▼
 *   Action: 调用 WeatherTool.getWeather("杭州")
 *     │
 *     ▼
 *   Observation: 明天杭州晴，18-25°C，微风
 *     │
 *     ▼
 *   Thought: 天气很好，适合出门
 *     │
 *     ▼
 *   Final Answer: 明天杭州晴天，气温 18-25°C，非常适合出门活动！
 * </pre>
 *
 * <h2>Tool 注册机制</h2>
 * <p>Spring AI 中有三种注册 Tool 的方式：</p>
 * <table border="1">
 *   <tr><th>方式</th><th>实现</th><th>适用场景</th></tr>
 *   <tr><td>@Tool 注解</td><td>在方法上加 @Tool</td><td>简单工具</td></tr>
 *   <tr><td>ToolCallback</td><td>实现 ToolCallback 接口</td><td>复杂工具（推荐）</td></tr>
 *   <tr><td>Function Bean</td><td>注册为 Spring Bean</td><td>通用函数</td></tr>
 * </table>
 *
 * @see org.springframework.ai.tool.annotation.Tool
 * @see org.springframework.ai.chat.client.ChatClient
 */
@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final ChatClient.Builder chatClientBuilder;
    private final WeatherTool weatherTool;
    private final CalculatorTool calculatorTool;

    public AgentService(ChatClient.Builder chatClientBuilder,
                        WeatherTool weatherTool,
                        CalculatorTool calculatorTool) {
        this.chatClientBuilder = chatClientBuilder;
        this.weatherTool = weatherTool;
        this.calculatorTool = calculatorTool;
    }

    /**
     * <h3>Agent 对话（带工具调用能力）</h3>
     *
     * <p>这是 Agent 的核心方法。当用户问题需要调用工具时，
     * AI 会自动识别并调用对应的工具，然后将工具结果融合到最终回答中。</p>
     *
     * <h3>典型交互示例</h3>
     * <pre>{@code
     * // 示例 1：天气查询
     * chat("明天杭州会下雨吗？");
     * // AI 自动判断 → 需要调用 WeatherTool
     * // → 获取天气数据 → 返回自然语言回答
     *
     * // 示例 2：数学计算
     * chat("123 * 456 + 789 等于多少？");
     * // AI 自动判断 → 需要调用 CalculatorTool
     * // → 计算结果 → 返回答案
     *
     * // 示例 3：普通对话（不需要工具）
     * chat("你好，请介绍一下你自己");
     * // AI 判断不需要工具 → 直接回答
     * }</pre>
     *
     * <h3>Tool Description 的关键作用</h3>
     * <p>AI 通过<b>工具的 description</b>来决定是否调用以及调用哪个工具。
     * 因此 description 要写清楚：</p>
     * <ul>
     *   <li><b>功能描述：</b>这个工具做什么</li>
     *   <li><b>适用场景：</b>什么时候应该调用这个工具</li>
     *   <li><b>参数说明：</b>每个参数的含义和格式</li>
     *   <li><b>返回值说明：</b>工具返回什么</li>
     * </ul>
     *
     * @param userMessage 用户的自然语言输入
     * @return Agent 的回答（可能融合了工具调用结果）
     */
    public String chat(String userMessage) {
        log.info("🤖 Agent 收到消息：{}", userMessage);

        return chatClientBuilder.build()
                .prompt()
                .system("""
                    你是一个智能助手，可以使用外部工具来完成任务。
                    当用户的请求涉及以下内容时，请调用对应的工具：
                    - 天气查询 → WeatherTool
                    - 数学计算 → CalculatorTool

                    重要规则：
                    1. 先判断是否需要工具，需要时果断调用
                    2. 不需要工具的问题直接回答
                    3. 工具返回的数据要转换成用户友好的自然语言
                    4. 回答用中文
                    """)
                .user(userMessage)
                // 关键：注册工具
                // ToolCallbacks.from() 将多个工具对象打包成一个 ToolCallbackProvider
                // ChatClient 会将工具信息（名称、描述、参数格式）发送给 LLM
                // LLM 决定是否调用工具以及如何调用
                .tools(ToolCallbacks.from(weatherTool, calculatorTool))
                .call()
                .content();
    }

    /**
     * <h3>对话 + 调试信息</h3>
     *
     * <p>返回 Agent 的完整思考过程，方便调试和学习。</p>
     * <p>可以看到 AI 是否调用了工具、调用了哪个工具、参数是什么。</p>
     *
     * @param userMessage 用户消息
     * @return 包含工具调用详情的回答
     */
    public String chatWithDebugInfo(String userMessage) {
        log.info("🤖 Agent (debug) 收到消息：{}", userMessage);

        // ChatResponse 包含了完整的响应信息
        // 可以用 getMetadata() 查看工具调用记录
        var response = chatClientBuilder.build()
                .prompt()
                .system("""
                    你是一个智能助手，可以使用工具完成任务。
                    - 天气查询 → WeatherTool
                    - 数学计算 → CalculatorTool
                    """)
                .user(userMessage)
                .tools(ToolCallbacks.from(weatherTool, calculatorTool))
                .call()
                .chatResponse();

        // 检查是否有工具调用
        if (response.getMetadata().get("toolCalls") != null) {
            log.debug("🔧 工具调用详情：{}", response.getMetadata().get("toolCalls"));
        }

        return response.getResult().getOutput().getText();
    }

    /**
     * 获取当前可用的工具列表
     */
    public String getAvailableTools() {
        return """
            可用工具列表：
            1. WeatherTool  —— 查询指定城市当前天气
            2. CalculatorTool —— 执行基础数学运算（加减乘除）
            """;
    }
}
