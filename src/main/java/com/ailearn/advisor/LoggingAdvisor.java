package com.ailearn.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * <h1>📋 自定义日志 Advisor</h1>
 *
 * <p>Advisor 是 Spring AI 的 AOP 机制，可以在 AI 调用的前后插入自定义逻辑。</p>
 *
 * <h2>适用场景</h2>
 * <table border="1">
 *   <tr><th>场景</th><th>Advisor 类型</th></tr>
 *   <tr><td>对话记忆</td><td>MessageChatMemoryAdvisor（内置）</td></tr>
 *   <tr><td>RAG 检索</td><td>QuestionAnswerAdvisor（内置）</td></tr>
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
 *   Advisor A.aroundCall() → before
 *     │
 *     ▼
 *   Advisor B.aroundCall() → before
 *     │
 *     ▼
 *   ChatModel.call()  ← 实际的 AI 调用
 *     │
 *     ▼
 *   Advisor B.aroundCall() → after
 *     │
 *     ▼
 *   Advisor A.aroundCall() → after
 *     │
 *     ▼
 *   返回给客户端
 * </pre>
 *
 * <p>这个 Advisor 会在每次 AI 调用时记录请求和响应信息，包括 Token 用量，
 * 方便监控和调试。</p>
 *
 * @see org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor
 */
public class LoggingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LoggingAdvisor.class);
    private static final String ADVISOR_NAME = "LoggingAdvisor";

    // ============================================================
    // CallAroundAdvisor 接口 —— 处理同步调用
    // ============================================================

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
     * 环绕同步调用
     *
     * <p>在 AI 调用前后记录请求详情和 Token 用量。</p>
     *
     * @param chain 调用链（执行下一个 Advisor 或实际的 AI 调用）
     * @param request 当前请求的上下文
     * @return AI 的响应
     */
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        long startTime = System.currentTimeMillis();
        String userMessage = extractUserMessage(request);

        // ---------- 请求前 ----------
        log.info("""
                ╔══════════════════ AI 请求 ─═════════════════╗
                ║ [请求] 用户消息：{}
                ║ [请求] 参数数量：{} 个
                ║ [请求] 已注册 Advisor：{} 个
                ╚══════════════════════════════════════════════╝""",
                userMessage,
                request.adviseContext().size(),
                request.advisorParams().size()
        );

        // ---------- 执行实际的 AI 调用 ----------
        AdvisedResponse response = chain.nextAroundCall(request);

        // ---------- 请求后 ----------
        long duration = System.currentTimeMillis() - startTime;
        ChatResponse chatResponse = response.response();

        log.info("""
                ╔══════════════════ AI 响应 ─═════════════════╗
                ║ [响应] 耗时：{} ms
                ║ [响应] Token 用量：{} (输入: {} / 输出: {})
                ║ [响应] 内容长度：{} 字符
                ╚══════════════════════════════════════════════╝""",
                duration,
                chatResponse.getMetadata().getUsage(),
                chatResponse.getMetadata().getUsage() != null ?
                        chatResponse.getMetadata().getUsage().getPromptTokens() : "N/A",
                chatResponse.getMetadata().getUsage() != null ?
                        chatResponse.getMetadata().getUsage().getCompletionTokens() : "N/A",
                chatResponse.getResult().getOutput().getText().length()
        );

        return response;
    }

    /**
     * 环绕流式调用
     *
     * <p>流式调用的 Advisor 更复杂一些，但原理相同。</p>
     *
     * @param chain 流式调用链
     * @param request 请求上下文
     * @return 流式响应
     */
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest request, StreamAroundAdvisorChain chain) {
        long startTime = System.currentTimeMillis();

        log.info("[流式请求开始] 用户消息：{}", extractUserMessage(request));

        return chain.nextAroundStream(request)
                .doOnComplete(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("[流式请求完成] 耗时：{} ms", duration);
                })
                .doOnError(error ->
                        log.error("[流式请求出错] {}", error.getMessage()));
    }

    /**
     * 从请求中提取用户消息文本
     */
    private String extractUserMessage(AdvisedRequest request) {
        String userMessage = request.userText();
        if (userMessage != null && userMessage.length() > 200) {
            // 截断过长的消息（日志中不要打印太多内容）
            return userMessage.substring(0, 200) + "...";
        }
        return userMessage != null ? userMessage : "(无消息)";
    }
}
