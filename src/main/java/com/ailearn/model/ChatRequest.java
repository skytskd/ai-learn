package com.ailearn.model;

/**
 * <h1>📥 通用聊天请求模型</h1>
 *
 * <p>用于接收前端发送的对话请求，统一请求格式。</p>
 *
 * <h2>使用示例（JSON 格式）</h2>
 * <pre>{@code
 * {
 *   "message": "请解释 Java 的 HashMap 扩容机制",
 *   "conversationId": "conv_001"
 * }
 * }</pre>
 *
 * <p><b>注意：</b>conversationId 用于标识对话会话，MemoryChatService 依赖它来
 * 区分不同用户的对话历史。如果传 null，系统会自动生成一个新的 ID。</p>
 */
public class ChatRequest {

    /**
     * 用户发送的消息文本（必填）
     */
    private String message;

    /**
     * 会话 ID（可选）
     * 用于区分不同的对话会话。
     * 如果不传，系统会自动生成（通过 UUID）。
     */
    private String conversationId;

    // ==================== 构造方法 ====================

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public ChatRequest(String message, String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
    }

    // ==================== Getter / Setter ====================

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
