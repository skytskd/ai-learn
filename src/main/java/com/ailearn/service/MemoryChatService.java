package com.ailearn.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h1>🧠 第 3 课：对话记忆服务</h1>
 *
 * <p>默认情况下，AI 模型是"无状态"的——每次对话都是全新的，不记得之前聊过什么。
 * MessageChatMemoryAdvisor 解决了这个问题。</p>
 *
 * <h2>核心概念</h2>
 * <ol>
 *   <li><b>ChatMemory（对话记忆）</b> —— 存储历史消息的仓库</li>
 *   <li><b>MessageChatMemoryAdvisor</b> —— 自动将历史消息注入到每次请求中</li>
 *   <li><b>MessageWindowChatMemory</b> —— 基于内存的实现（学习环境用）</li>
 *   <li><b>Token 消耗</b> —— 历史消息越长，每次请求成本越高</li>
 * </ol>
 *
 * <h2>工作原理</h2>
 * <pre>
 *   用户: "我叫张三"
 *   →
 *   Advisor 存储消息到 ChatMemory
 *   AI: "你好，张三！"
 *
 *   用户: "我叫什么名字？"
 *   →
 *   Advisor 从 ChatMemory 取出之前的对话
 *   → 拼接："[user]: 我叫张三\n[assistant]: 你好，张三！\n[user]: 我叫什么名字？"
 *   → 发送给 AI
 *   AI: "你叫张三。"
 * </pre>
 *
 * <h2>Token 管理策略</h2>
 * <table border="1">
 *   <tr><th>策略</th><th>方式</th><th>适用场景</th></tr>
 *   <tr><td>窗口截断</td><td>只保留最近 N 条消息</td><td>通用对话</td></tr>
 *   <tr><td>Token 限制</td><td>总 Token 数不超过阈值</td><td>长对话</td></tr>
 *   <tr><td>摘要压缩</td><td>对历史做摘要，不保留原文</td><td>超长对话</td></tr>
 * </table>
 *
 * <p>本项目使用窗口截断策略（默认保留 20 条），通过
 * {@code MessageWindowChatMemory.builder().maxMessages(20).build()} 控制。</p>
 *
 * @see org.springframework.ai.chat.memory.ChatMemory
 * @see org.springframework.ai.chat.memory.MessageWindowChatMemory
 * @see org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
 */
@Service
public class MemoryChatService {

    private final ChatClient.Builder chatClientBuilder;

    /**
     * 存储每个会话的 ChatMemory 实例
     * Key: conversationId (会话 ID)
     * Value: ChatMemory (该会话的历史消息)
     *
     * <p>使用 ConcurrentHashMap 保证线程安全（多个用户同时聊天）。</p>
     */
    private final Map<String, ChatMemory> sessionMemoryMap = new ConcurrentHashMap<>();

    /**
     * 最大保留的历史消息数
     * 超过这个数量，最旧的消息会被丢弃（窗口截断策略）
     */
    private static final int MAX_HISTORY_SIZE = 20;

    public MemoryChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    /**
     * <h3>带记忆的对话</h3>
     *
     * <p>同一个 conversationId 下的多次调用，AI 会记住之前的对话内容。</p>
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * // 第 1 轮
     * chat("conv_001", "我叫张三，是一名 Java 程序员");
     * // AI: "你好张三！有什么可以帮你的？"
     *
     * // 第 2 轮（同一 conversationId）
     * chat("conv_001", "我的名字是什么？我的职业是什么？");
     * // AI: "你叫张三，是 Java 程序员" ← 记住了！
     *
     * // 换个 conversationId 就是全新的对话
     * chat("conv_002", "我的名字是什么？");
     * // AI: "我不知道你的名字" ← 没有记忆
     * }</pre>
     *
     * @param conversationId 会话 ID（同一个 ID 共享对话历史）
     * @param userMessage    用户消息
     * @return AI 回复
     */
    public String chat(String conversationId, String userMessage) {
        // 1. 获取或创建该会话的 ChatMemory
        ChatMemory memory = sessionMemoryMap.computeIfAbsent(
                conversationId,
                id -> MessageWindowChatMemory.builder()
                        .maxMessages(MAX_HISTORY_SIZE)
                        .build()
        );

        // 2. 创建 MessageChatMemoryAdvisor
        //    使用 builder 模式，绑定 conversationId
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(memory)
                .conversationId(conversationId)
                .build();

        // 3. 发起对话
        //    注意：这里每次都用 chatClientBuilder.build() 创建新实例
        //    因为 Advisor 是有状态的（绑定了特定 session 的 memory），
        //    不能跨 session 共享
        return chatClientBuilder.build()
                .prompt()
                .system("你是一名友好的 AI 助手，请记住用户之前告诉你的信息。")
                .user(userMessage)
                .advisors(memoryAdvisor)  // 关键：添加记忆 Advisor
                .call()
                .content();
    }

    /**
     * 清除指定会话的记忆
     *
     * @param conversationId 要清除的会话 ID
     */
    public void clearMemory(String conversationId) {
        ChatMemory memory = sessionMemoryMap.remove(conversationId);
        if (memory != null) {
            memory.clear(conversationId);
        }
    }

    /**
     * 获取指定会话的历史消息数量
     *
     * @param conversationId 会话 ID
     * @return 消息数量
     */
    public int getMemorySize(String conversationId) {
        ChatMemory memory = sessionMemoryMap.get(conversationId);
        if (memory == null) {
            return 0;
        }
        // get(conversationId) 获取该会话的所有消息
        return memory.get(conversationId).size();
    }
}
