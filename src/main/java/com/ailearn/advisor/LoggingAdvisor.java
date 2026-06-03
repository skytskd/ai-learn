package com.ailearn.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * <h1>📋 自定义日志 Advisor</h1>
 *
 * <p>Advisor 是 Spring AI 的 AOP 机制，可以在 AI 调用的前后插入自定义逻辑。</p>
 *
 * <h2>适用场景</h2>
 * <table border="1">
 *   <tr><th>场景</th><th>Advisor 类型</th></tr>
 *   <tr><td>对话记忆</td><td>MessageChatMemoryAdvisor（内置）</td></tr>
 *   <tr><td>RAG 检索</td><td>RetrievalAugmentationAdvisor（内置）</td></tr>
 *   <tr><td>日志记录</td><td>SimpleLoggerAdvisor（内置）/ 自定义</td></tr>
 *   <tr><td>内容审核</td><td>自定义 Advisor</td></tr>
 *   <tr><td>Token 用量统计</td><td>自定义 Advisor</td></tr>
 *   <tr><td>请求限流</td><td>自定义 Advisor</td></tr>
 *   <tr><td>Prompt 注入检测</td><td>自定义 Advisor</td></tr>
 * </table>
 *
 * <h2>Advisor 链执行流程</h2>
 * <pre>
 *   请求进入
 *     │
 *     ▼
 *   Advisor A.before() → 前置处理
 *     │
 *     ▼
 *   Advisor B.before() → 前置处理
 *     │
 *     ▼
 *   ChatModel.call()  ← 实际的 AI 调用
 *     │
 *     ▼
 *   Advisor B.after() → 后置处理
 *     │
 *     ▼
 *   Advisor A.after() → 后置处理
 *     │
 *     ▼
 *   返回给客户端
 * </pre>
 *
 * <p>这个 Advisor 会在每次 AI 调用时记录请求和响应信息，包括 Token 用量，
 * 方便监控和调试。</p>
 *
 * @see org.springframework.ai.chat.client.advisor.api.BaseAdvisor
 */
public class LoggingAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LoggingAdvisor.class);
    private static final String ADVISOR_NAME = "LoggingAdvisor";

    @Override
    public String getName() {
        return ADVISOR_NAME;
    }

    @Override
    public int getOrder() {
        // 优先级：数字越小，越先执行
        // 日志 Advisor 应该在最外层（最先执行，最后返回）
        return 0;
    }

    /**
     * 在 AI 调用前执行
     *
     * <p>记录请求详情。</p>
     *
     * @param chatClientRequest 当前请求的上下文
     * @param advisorChain 调用链
     * @return 处理后的请求
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userMessage = extractUserMessage(chatClientRequest);

        log.info("""
                ╔══════════════════ AI 请求 ─═════════════════╗
                ║ [请求] 用户消息：{}
                ║ [请求] 上下文参数数量：{} 个
                ╚══════════════════════════════════════════════╝""",
                userMessage,
                chatClientRequest.context().size()
        );

        return chatClientRequest;
    }

    /**
     * 在 AI 调用后执行
     *
     * <p>记录响应详情和 Token 用量。</p>
     *
     * @param chatClientResponse AI 的响应
     * @param advisorChain 调用链
     * @return 处理后的响应
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        ChatResponse chatResponse = chatClientResponse.chatResponse();

        if (chatResponse != null && chatResponse.getMetadata() != null
                && chatResponse.getMetadata().getUsage() != null) {
            var usage = chatResponse.getMetadata().getUsage();
            log.info("""
                    ╔══════════════════ AI 响应 ─═════════════════╗
                    ║ [响应] Token 用量：{} (输入: {} / 输出: {})
                    ║ [响应] 内容长度：{} 字符
                    ╚══════════════════════════════════════════════╝""",
                    usage,
                    usage.getPromptTokens(),
                    usage.getCompletionTokens(),
                    chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null
                            ? chatResponse.getResult().getOutput().getText().length()
                            : 0
            );
        } else {
            log.info("""
                    ╔══════════════════ AI 响应 ─═════════════════╗
                    ║ [响应] 已收到（无详细元数据）
                    ╚══════════════════════════════════════════════╝""");
        }

        return chatClientResponse;
    }

    /**
     * 从请求中提取用户消息文本
     */
    private String extractUserMessage(ChatClientRequest request) {
        var userMessage = request.prompt().getUserMessage();
        if (userMessage != null) {
            String text = userMessage.getText();
            if (text != null && text.length() > 200) {
                // 截断过长的消息（日志中不要打印太多内容）
                return text.substring(0, 200) + "...";
            }
            return text != null ? text : "(无消息)";
        }
        return "(无消息)";
    }
}
