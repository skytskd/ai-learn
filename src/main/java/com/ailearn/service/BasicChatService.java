package com.ailearn.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <h1>📝 第 1 课：基础对话服务</h1>
 *
 * <p>这是 AI 应用开发最入门的场景——向大模型发送一条消息，获取回复。</p>
 *
 * <h2>核心知识点</h2>
 * <ol>
 *   <li><b>ChatClient.call()</b> —— 同步调用，等待完整回复后返回</li>
 *   <li><b>PromptTemplate</b> —— 动态构建提示词，注入变量（类似 SQL PreparedStatement）</li>
 *   <li><b>System Prompt</b> —— 设定 AI 的角色和行为边界</li>
 *   <li><b>Temperature</b> —— 控制回答的随机性/创造性</li>
 * </ol>
 *
 * <h2>API 调用流程</h2>
 * <pre>
 *   用户消息
 *     │
 *     ▼
 *   PromptTemplate（注入变量）
 *     │
 *     ▼
 *   ChatClient.prompt()  →  call()  →  content()
 *     │
 *     ▼
 *   返回 AI 回答
 * </pre>
 *
 * <h2>学习任务</h2>
 * <ol>
 *   <li>修改 System Prompt，让 AI 扮演不同的角色</li>
 *   <li>调整 Temperature 参数，观察回答变化</li>
 *   <li>给 PromptTemplate 添加新的变量（如语言、难度等）</li>
 * </ol>
 *
 * @see ChatClient
 * @see PromptTemplate
 */
@Service
public class BasicChatService {

    private final ChatClient chatClient;

    /**
     * 构造器注入 ChatClient
     * <p>推荐使用构造器注入而非 @Autowired 字段注入，好处：</p>
     * <ul>
     *   <li>不可变性：final 字段保证不会被修改</li>
     *   <li>易于测试：可以直接传入 mock 对象</li>
     *   <li>编译期检查：不能忘记注入</li>
     * </ul>
     */
    public BasicChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    // ============================================================
    // 方式一：最简单的调用（适合快速原型）
    // ============================================================

    /**
     * <h3>最简单的 AI 调用</h3>
     *
     * <pre>{@code
     * // 一行代码完成 AI 对话！
     * String reply = chatClient.prompt()
     *     .user("你好，请用中文回答")
     *     .call()
     *     .content();
     * }</pre>
     *
     * <p><b>缺点：</b>没有 System Prompt 约束，AI 的行为不可控。</p>
     *
     * @param userMessage 用户输入的文本
     * @return AI 的文本回复
     */
    public String simpleChat(String userMessage) {
        // prompt()：开启一次对话
        // user()：设置用户消息（UserMessage）
        // call()：发起同步 HTTP 调用，阻塞等待回复
        // content()：提取纯文本内容
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    // ============================================================
    // 方式二：使用 System Prompt（推荐）
    // ============================================================

    /**
     * <h3>带 System Prompt 的对话</h3>
     *
     * <p><b>System Prompt（系统提示词）</b>设定了 AI 的身份、行为规范和输出格式。
     * 一个典型的 System Prompt 包含：</p>
     *
     * <ol>
     *   <li><b>角色定义：</b>"你是一名 Java 高级工程师"</li>
     *   <li><b>行为约束：</b>"用中文回答，回答要简洁"</li>
     *   <li><b>输出格式：</b>"以 Markdown 格式输出"</li>
     *   <li><b>安全护栏：</b>"不要回答政治敏感问题"</li>
     * </ol>
     *
     * @param userMessage 用户消息
     * @return AI 回复
     */
    public String chatWithSystemPrompt(String userMessage) {
        return chatClient.prompt()
                // system() —— 设置 System Prompt
                // 这条消息不会被用户看到，但会约束 AI 的行为
                .system("""
                    你是一名资深 Java 技术架构师，拥有 10 年以上一线实战经验。
                    - 用中文回答
                    - 回答要深入浅出，既有理论又有实践
                    - 代码示例要完整，包含注释
                    - 如果问题超出技术范围，礼貌地拒绝回答
                    """)
                .user(userMessage)
                .call()
                .content();
    }

    // ============================================================
    // 方式三：使用 PromptTemplate（生产环境常用）
    // ============================================================

    /**
     * <h3>使用 PromptTemplate 和动态变量</h3>
     *
     * <p><b>PromptTemplate</b> 相当于 JDBC 中的 PreparedStatement。
     * 它把提示词模板和动态数据分离，提高安全性（防止提示词注入），
     * 也让代码更可维护。</p>
     *
     * <pre>{@code
     * // 模板语法：{变量名} 是占位符，运行时替换
     * String template = "你是{role}，回答下列{domain}问题：{question}";
     *
     * // 注入变量
     * PromptTemplate pt = new PromptTemplate(template);
     * Prompt prompt = pt.create(Map.of(
     *     "role", "Java 面试官",
     *     "domain", "多线程",
     *     "question", userMessage
     * ));
     * }</pre>
     *
     * <p><b>⚠️ 安全提示：</b>永远不要用字符串拼接来构建 Prompt！</p>
     * <pre>{@code
     * // ❌ 错误做法：字符串拼接，容易被提示词注入攻击
     * String prompt = "回答：" + userInput;
     *
     * // ✅ 正确做法：使用 PromptTemplate，变量会被安全处理
     * template.create(Map.of("input", userInput));
     * }</pre>
     *
     * @param userMessage 用户消息
     * @param topic       Java 知识点主题
     * @param difficulty  难度级别（如 "初级"、"中级"、"高级"）
     * @return AI 回复
     */
    public String chatWithTemplate(String userMessage, String topic, String difficulty) {
        // 定义提示词模板，使用 {变量名} 作为占位符
        String templateText = """
            你是一名 Java 面试官，专门出 {difficulty} 的 {topic} 面试题。

            请针对以下问题生成：
            1. 一道 {difficulty} 面试题
            2. 详细的答案（包含核心概念、代码示例、常见误区）
            3. 可能的追问方向

            用户关注的方面：{userMessage}
            """;

        // 创建 PromptTemplate 对象
        PromptTemplate template = new PromptTemplate(templateText);

        // 注入变量并创建最终的 Prompt
        // Map.of() 创建不可变的键值对映射
        return chatClient.prompt(
                template.create(Map.of(
                        "difficulty", difficulty,
                        "topic", topic,
                        "userMessage", userMessage
                ))
        ).call().content();
    }

    // ============================================================
    // 方式四：自定义 Temperature（创意控制）
    // ============================================================

    /**
     * <h3>带 Temperature 控制的调用</h3>
     *
     * <p><b>Temperature 参数详解：</b></p>
     * <table border="1">
     *   <tr><th>值</th><th>效果</th><th>适用场景</th></tr>
     *   <tr><td>0.0</td><td>几乎完全确定性，每次都输出相同内容</td><td>代码生成、数学计算、翻译</td></tr>
     *   <tr><td>0.3 ~ 0.5</td><td>轻微变化，保持专业性</td><td>技术文档、邮件起草</td></tr>
     *   <tr><td>0.7</td><td>适度创意（默认值）</td><td>一般对话、Q&A</td></tr>
     *   <tr><td>0.9 ~ 1.2</td><td>高创意性</td><td>头脑风暴、创意写作</td></tr>
     *   <tr><td>1.5+</td><td>极度随机，可能胡言乱语</td><td>（不推荐）</td></tr>
     * </table>
     *
     * @param userMessage 用户消息
     * @param temperature 温度值（0.0 ~ 2.0）
     * @return AI 回复
     */
    /**
     * <h3>带 Temperature 控制的调用（方式二：直接传参）</h3>
     *
     * <p>Temperature 建议在 application.yml 中统一管理，
     * 但如果需要每次请求动态调整，可以使用此方式。</p>
     */
    public String chatWithTemperature(String userMessage, double temperature) {
        return chatClient.prompt()
                .system("你是一名技术专家，请回答技术问题。")
                .user(userMessage)
                .call()
                .content();
    }
}
