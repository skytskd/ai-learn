package com.ailearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <h1>🎓 AI 应用开发 Java 学习进阶项目 —— 启动类</h1>
 *
 * <p>本项目是一个循序渐进的学习项目，按照以下路径学习：</p>
 *
 * <ol>
 *   <li><b>基础对话</b> —— BasicChatService：最简单的 AI 对话调用</li>
 *   <li><b>流式输出</b> —— StreamChatService：像 ChatGPT 一样逐字输出</li>
 *   <li><b>对话记忆</b> —— MemoryChatService：让 AI 记住上下文</li>
 *   <li><b>Prompt 模板</b> —— 动态构建提示词，注入变量</li>
 *   <li><b>RAG 系统</b> —— RagService：基于私有知识库的问答</li>
 *   <li><b>Agent 工具调用</b> —— AgentService：让 AI 调用外部工具</li>
 *   <li><b>自定义 Advisor</b> —— LoggingAdvisor：AOP 切面拦截</li>
 * </ol>
 *
 *
 * <h2>架构说明</h2>
 *
 * <pre>
 *                      ┌──────────────────────┐
 *                      │    HTTP 请求 / SSE     │
 *                      └──────────┬───────────┘
 *                                 │
 *                      ┌──────────▼───────────┐
 *                      │      Controller      │  ← 接收请求，参数校验
 *                      └──────────┬───────────┘
 *                                 │
 *                      ┌──────────▼───────────┐
 *                      │       Service        │  ← 业务逻辑，编排调用
 *                      └──────────┬───────────┘
 *                                 │
 *              ┌──────────────────┼──────────────────┐
 *              │                  │                  │
 *     ┌────────▼────────┐ ┌──────▼──────┐ ┌────────▼────────┐
 *     │    ChatClient   │ │ VectorStore │ │ @Tool 工具方法   │
 *     │  (对话客户端)     │ │ (向量存储)   │ │  (工具定义)      │
 *     └─────────────────┘ └─────────────┘ └─────────────────┘
 * </pre>
 *
 * <h2>接口汇总</h2>
 *
 * <table border="1">
 *   <tr><th>接口路径</th><th>方法</th><th>说明</th><th>对应 Service</th></tr>
 *   <tr><td>/api/chat/simple</td><td>POST</td><td>基础同步对话</td><td>BasicChatService</td></tr>
 *   <tr><td>/api/chat/stream</td><td>GET</td><td>流式对话（SSE）</td><td>StreamChatService</td></tr>
 *   <tr><td>/api/chat/memory</td><td>POST</td><td>带记忆的对话</td><td>MemoryChatService</td></tr>
 *   <tr><td>/api/rag/ask</td><td>POST</td><td>RAG 知识库问答</td><td>RagService</td></tr>
 *   <tr><td>/api/agent/chat</td><td>POST</td><td>Agent 工具调用</td><td>AgentService</td></tr>
 * </table>
 *
 * @author ai-learn
 * @version 1.0.0
 * @since 2025
 */
@SpringBootApplication
public class AiLearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiLearnApplication.class, args);
        System.out.println("""

                ╔══════════════════════════════════════════════════════╗
                ║  🎓  AI 应用开发 Java 学习进阶项目 启动成功！        ║
                ║                                                      ║
                ║  本地访问：http://localhost:8080                     ║
                ║  Swagger：http://localhost:8080/swagger-ui.html      ║
                ║                                                      ║
                ║  学习路径：                                          ║
                ║  1. POST /api/chat/simple  → 基础对话               ║
                ║  2. GET  /api/chat/stream  → 流式输出               ║
                ║  3. POST /api/chat/memory  → 对话记忆               ║
                ║  4. POST /api/rag/ask       → RAG 知识库            ║
                ║  5. POST /api/agent/chat     → Agent 工具            ║
                ║                                                      ║
                ║  📖 浏览器打开 http://localhost:8080 查看学习指南     ║
                ╚══════════════════════════════════════════════════════╝
                """);
    }
}
