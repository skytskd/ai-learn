package com.ailearn.model;

/**
 * <h1>📤 通用聊天响应模型</h1>
 *
 * <p>统一的 API 响应格式，方便前端解析。</p>
 *
 * <h2>响应 JSON 示例</h2>
 * <pre>{@code
 * {
 *   "success": true,
 *   "message": "处理成功",
 *   "data": "HashMap 的扩容机制是...",
 *   "conversationId": "conv_001"
 * }
 * }</pre>
 *
 * <h2>与流式响应的区别</h2>
 * <p>
 *   <b>同步响应（本类）：</b>返回完整回答，简单直接，适合短回答场景。<br>
 *   <b>流式响应（text/event-stream）：</b>逐字推送，用户体验好，适合长回答场景。
 *   流式响应不使用本类，而是直接返回 ServerSentEvent。
 * </p>
 */
public class ChatResponse {

    /** 请求是否成功 */
    private boolean success;

    /** 响应消息（成功时为 "处理成功"，失败时为错误信息） */
    private String message;

    /** 响应的数据内容（AI 的文本回答） */
    private String data;

    /** 会话 ID（用于后续对话的上下文关联） */
    private String conversationId;

    // ==================== 构造方法 ====================

    public ChatResponse() {
    }

    private ChatResponse(boolean success, String message, String data, String conversationId) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.conversationId = conversationId;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建成功响应
     *
     * @param data            AI 的回答内容
     * @param conversationId  会话 ID
     * @return 成功的 ChatResponse 实例
     */
    public static ChatResponse success(String data, String conversationId) {
        return new ChatResponse(true, "处理成功", data, conversationId);
    }

    /**
     * 创建失败响应
     *
     * @param errorMsg 错误描述信息
     * @return 失败的 ChatResponse 实例
     */
    public static ChatResponse error(String errorMsg) {
        return new ChatResponse(false, errorMsg, null, null);
    }

    // ==================== Getter / Setter ====================

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public String toString() {
        return "ChatResponse{success=" + success
                + ", conversationId='" + conversationId + '\''
                + ", data='" + (data != null ? data.substring(0, Math.min(data.length(), 100)) : "null") + "...'}";
    }
}
