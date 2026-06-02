# 🎓 ai-learn — AI 应用开发 Java 学习进阶项目

基于 **Spring Boot 3.4 + Spring AI 1.0** 的 AI 应用开发教学项目，从入门到实战。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.4.5 | 基础框架 |
| Spring AI | 1.0.0 | AI 集成核心框架 |
| Spring WebFlux | 3.4.5 | 响应式编程 / SSE 流式接口 |
| Lombok | latest | 减少样板代码 |
| H2 | latest | 内存数据库（学习用） |

## 学习路线

| 课程 | 模块 | 核心知识点 | 接口 |
|------|------|-----------|------|
| 第 1 课 | BasicChatService | ChatClient、PromptTemplate、System Prompt | `POST /api/chat/simple` |
| 第 2 课 | StreamChatService | Flux、SSE、流式输出 | `GET /api/chat/stream` |
| 第 3 课 | MemoryChatService | ChatMemory、MessageChatMemoryAdvisor | `POST /api/chat/memory` |
| 第 4 课 | RagService | 文档加载→分块→Embedding→向量检索 | `POST /api/rag/ask` |
| 第 5 课 | AgentService | @Tool、ReAct 模式、工具调用 | `POST /api/agent/chat` |

## 项目结构

```
ai-learn/
├── pom.xml                          # Maven 配置
├── src/main/java/com/ailearn/
│   ├── AiLearnApplication.java      # 启动类
│   ├── config/
│   │   └── ChatClientConfig.java    # ChatClient Bean 配置
│   ├── controller/
│   │   ├── BasicChatController.java # 基础对话接口
│   │   ├── StreamChatController.java# 流式对话接口
│   │   ├── MemoryChatController.java# 记忆对话接口
│   │   ├── RagController.java       # RAG 问答接口
│   │   └── AgentController.java     # Agent 接口
│   ├── service/
│   │   ├── BasicChatService.java    # 基础对话（3种调用方式）
│   │   ├── StreamChatService.java   # 流式输出（SSE）
│   │   ├── MemoryChatService.java   # 对话记忆
│   │   ├── RagService.java          # RAG 系统（完整流程）
│   │   └── AgentService.java        # Agent 工具调用
│   ├── tool/
│   │   ├── WeatherTool.java         # 天气查询工具
│   │   ├── CalculatorTool.java      # 计算器工具
│   │   └── DatabaseQueryTool.java   # 数据库查询工具
│   ├── model/
│   │   ├── ChatRequest.java         # 请求模型
│   │   └── ChatResponse.java        # 响应模型
│   └── advisor/
│       └── LoggingAdvisor.java      # 自定义 Advisor
├── src/main/resources/
│   ├── application.yml              # 核心配置
│   ├── static/index.html            # 学习指南页面
│   ├── rag-docs/
│   │   ├── java-interview-1.txt     # RAG 测试文档1
│   │   └── java-interview-2.txt     # RAG 测试文档2
│   └── prompt-templates/
│       └── code-review.st           # 代码审查模板
├── README.md
```

## 快速开始

### 1. 配置 API Key

编辑 `src/main/resources/application.yml`，将 OpenAI API Key 填入：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:sk-your-api-key-here}
```

或者设置环境变量：

```bash
export OPENAI_API_KEY=sk-your-api-key-here
```

### 2. 启动项目

```bash
cd ai-learn
./mvnw spring-boot:run
```

### 3. 打开学习指南

浏览器访问：`http://localhost:8080`

### 4. 测试接口

```bash
# 基础对话
curl -X POST http://localhost:8080/api/chat/simple \
  -H "Content-Type: application/json" \
  -d '{"message":"什么是Java的HashMap扩容机制？"}'

# 流式输出
curl -N http://localhost:8080/api/chat/stream?message=你好

# RAG 问答
curl -X POST http://localhost:8080/api/rag/ask \
  -H "Content-Type: application/json" \
  -d '{"message":"HashMap的底层数据结构是什么？"}'

# Agent 对话
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"今天杭州天气如何？123*456等于多少？"}'
```

## 学习建议

1. **按顺序学**：第1课 → 第2课 → 第3课 → 第4课 → 第5课
2. **先看注释**：每个类的 Javadoc 都是精心编写的教学文档
3. **动手改代码**：修改 System Prompt、Temperature 参数，观察变化
4. **做对比实验**：RAG vs 无RAG、有记忆 vs 无记忆
5. **读懂架构**：Config → Service → Controller → Tool 的分层设计
