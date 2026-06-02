package com.ailearn.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>🔧 ChatClient 配置类</h1>
 *
 * <p>
 *   创建并配置 ChatClient Bean。ChatClient 是 Spring AI 的核心入口，
 *   所有 AI 对话、流式输出、RAG 检索都通过它实现。
 * </p>
 *
 * <h2>核心概念：ChatModel vs ChatClient</h2>
 * <table border="1">
 *   <tr><th>组件</th><th>角色类比</th><th>职责</th></tr>
 *   <tr>
 *     <td>ChatModel</td>
 *     <td>"发动机"</td>
 *     <td>底层 AI 模型调用接口，直接和大模型通信</td>
 *   </tr>
 *   <tr>
 *     <td>ChatClient</td>
 *     <td>"方向盘"</td>
 *     <td>更高层的 API，提供链式调用、Advisor、模板等高级功能</td>
 *   </tr>
 * </table>
 *
 * <p><b>建议：</b>开发中使用 ChatClient 而非直接使用 ChatModel，
 * ChatClient 提供了更丰富的功能（Advisor 机制、流式、模板）。</p>
 *
 * <h2>Advisor 机制</h2>
 * <p>
 *   Advisor 是 Spring AI 的 AOP 实现，在执行 AI 调用前后添加横切逻辑。<br>
 *   常见场景：对话记忆（ChatMemory）、日志记录、RAG 文档注入、安全检查等。
 * </p>
 *
 * @see org.springframework.ai.chat.client.ChatClient
 * @see org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
 */
@Configuration
public class ChatClientConfig {

    /**
     * 创建默认的 ChatClient Bean
     *
     * <p>这个 Bean 是最基础的 ChatClient，带有日志 Advisor。
     * 在具体的 Service 中，可以通过 {@code ChatClient.Builder} 来复制
     * 并添加业务特定的 Advisor。</p>
     *
     * <h3>使用方式</h3>
     * <pre>{@code
     * @Autowired
     * private ChatClient.Builder chatClientBuilder;
     *
     * // 基础对话
     * String reply = chatClientBuilder.build()
     *     .prompt("你好")
     *     .call()
     *     .content();
     *
     * // 带 Advisor 的对话
     * String reply = chatClientBuilder.build()
     *     .prompt("你好")
     *     .advisors(new MyAdvisor())
     *     .call()
     *     .content();
     * }</pre>
     *
     * @param chatModel Spring Boot 自动注入的 ChatModel 实例
     * @return 配置好的 ChatClient 实例
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                // SimpleLoggerAdvisor：打印请求/响应的详细日志
                // 日志级别达到 DEBUG 可见，生产环境建议关闭
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * 创建 ChatClient.Builder Bean
     *
     * <p>Builder Bean 比直接注入 ChatClient 更灵活。
     * 可以在运行时动态添加 Advisor、修改模型参数等。</p>
     *
     * <p><b>为什么使用 Builder：</b></p>
     * <ul>
     *   <li>每个请求可能需要不同的 Advisor（如 RAG、日志）</li>
     *   <li>每个请求可能需要不同的 System Prompt</li>
     *   <li>Builder 是不可变的——每次 build() 都创建新实例，避免并发问题</li>
     * </ul>
     *
     * @param chatModel Spring Boot 自动注入的 ChatModel 实例
     * @return ChatClient.Builder 实例
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor());
    }
}
