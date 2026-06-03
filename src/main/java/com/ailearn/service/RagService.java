package com.ailearn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1>🔍 第 4 课：RAG（检索增强生成）服务</h1>
 *
 * <p><b>RAG = Retrieval Augmented Generation（检索增强生成）</b></p>
 *
 * <p>这是 2025 年 AI 应用开发最火的技术。核心思想是：
 * 在让大模型回答问题之前，<b>先从知识库中检索相关信息</b>，
 * 把检索结果和用户问题一起发给大模型。</p>
 *
 * <h2>为什么需要 RAG？</h2>
 * <ol>
 *   <li><b>知识时效性</b> —— 大模型的训练数据有截止日期</li>
 *   <li><b>私有知识</b> —— 公司内部文档、代码库等大模型不知道</li>
 *   <li><b>幻觉控制</b> —— RAG 让回答有据可查，减少胡说</li>
 *   <li><b>可审计</b> —— 可以追溯答案来自哪篇文档</li>
 * </ol>
 *
 * <h2>RAG 工作流程</h2>
 * <pre>
 *  ┌──────────────────────────────────────────────────┐
 *  │                离线阶段（数据准备）                 │
 *  ├──────────────────────────────────────────────────┤
 *  │  txt/pdf/doc → 文档加载 → 文本分块 → 向量嵌入 →   │
 *  │  → 存入向量数据库                                  │
 *  └──────────────────────────────────────────────────┘
 *
 *  ┌──────────────────────────────────────────────────┐
 *  │                在线阶段（问答查询）                  │
 *  ├──────────────────────────────────────────────────┤
 *  │  用户问题 → Embedding → 向量相似度检索 → Top-K文档  │
 *  │  → 拼接 Prompt → 发给 LLM → 返回答案              │
 *  └──────────────────────────────────────────────────┘
 * </pre>
 *
 * <h2>核心概念解析</h2>
 * <table border="1">
 *   <tr><th>概念</th><th>说明</th></tr>
 *   <tr><td>分块（Chunking）</td><td>把长文档切成小块，每块 300-500 字，便于检索</td></tr>
 *   <tr><td>向量嵌入（Embedding）</td><td>把文本转成向量（一串数字），语义相近的向量距离近</td></tr>
 *   <tr><td>相似度检索</td><td>用余弦相似度找最相关的文档块</td></tr>
 *   <tr><td>Top-K</td><td>检索最相关的 K 个文档块（通常 3~8）</td></tr>
 * </table>
 *
 * @see org.springframework.ai.vectorstore.VectorStore
 * @see org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor
 * @see org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourceResolver;

    /**
     * RAG 文档的路径（从 application.yml 读取）
     */
    @Value("${spring.ai.rag.docs-path:classpath:/rag-docs/}")
    private String ragDocsPath;

    /**
     * 构造器注入
     *
     * @param chatClientBuilder ChatClient 构建器
     * @param vectorStore       Spring Boot 自动装配的向量存储
     *                          （这里用的是 SimpleVectorStore - 内存实现）
     * @param resourceResolver  用于扫描 classpath 下的文件
     */
    public RagService(ChatClient.Builder chatClientBuilder,
                      VectorStore vectorStore,
                      ResourcePatternResolver resourceResolver) {
        this.chatClientBuilder = chatClientBuilder;
        this.vectorStore = vectorStore;
        this.resourceResolver = resourceResolver;
    }

    // ============================================================
    // 初始化：项目启动时加载文档到向量库
    // ============================================================

    /**
     * <h3>@PostConstruct：项目启动后自动执行</h3>
     *
     * <p>扫描 rag-docs 目录下的所有 .txt 文件，
     * 分块、嵌入化、存入向量数据库。</p>
     *
     * <p><b>注意：</b>生产环境中，文档加载通常是一个独立的管理流程，
     * 而不是每次启动都重新加载。这里为了学习方便，简化了流程。</p>
     */
    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("🚀 开始初始化 RAG 知识库...");
        log.info("========================================");

        try {
            // 1. 扫描文档目录
            Resource[] resources = resourceResolver.getResources(
                    ragDocsPath + "*.txt"
            );
            log.info("📂 发现 {} 个文档文件", resources.length);

            List<Document> allDocs = new ArrayList<>();

            for (Resource resource : resources) {
                log.info("  📄 加载文档：{}", resource.getFilename());

                // 2. 用 TextReader 读取文档
                TextReader textReader = new TextReader(resource);
                List<Document> docs = textReader.read();
                log.info("    原始文档大小：{} 字符", docs.get(0).getText().length());

                // 3. 分块（Chunking）
                //    TokenTextSplitter：按 Token 数分块
                //    参数：每个块最多 500 tokens，块与块之间重叠 50 tokens
                //    重叠的目的是防止语义被截断
                TokenTextSplitter splitter = new TokenTextSplitter(500, 50, 5, 10000, true);
                List<Document> chunks = splitter.split(docs);
                log.info("    分块数量：{}", chunks.size());

                allDocs.addAll(chunks);
            }

            // 4. 将分块存入向量数据库
            //    Spring AI 会自动调用 EmbeddingModel 将文本转为向量
            //    然后存入 VectorStore（这里是内存中的 SimpleVectorStore）
            if (!allDocs.isEmpty()) {
                vectorStore.add(allDocs);
                log.info("✅ 成功将 {} 个文档块存入向量数据库", allDocs.size());
            } else {
                log.warn("⚠️ 没有文档块需要存储（文档目录为空？）");
            }
        } catch (IOException e) {
            log.error("❌ RAG 知识库初始化失败", e);
        }

        log.info("========================================");
    }

    /**
     * <h3>RAG 问答（核心方法）</h3>
     *
     * <p>这个方法展示了 RAG 的完整在线流程。</p>
     *
     * <p><b>关键：RetrievalAugmentationAdvisor + VectorStoreDocumentRetriever</b></p>
     * <p>RetrievalAugmentationAdvisor 是 Spring AI 1.0.0 中 Modular RAG Architecture 的实现，它会：</p>
     * <ol>
     *   <li>把用户问题转为 Embedding</li>
     *   <li>通过 VectorStoreDocumentRetriever 从 VectorStore 中检索最相关的文档块（Top-K）</li>
     *   <li>把检索结果注入到 Prompt 的上下文窗口中</li>
     *   <li>最后让 LLM 基于这些资料回答问题</li>
     * </ol>
     *
     * @param question 用户的问题
     * @param topK     检索最相关的几条文档（建议 3-8）
     * @return AI 基于检索资料的回答
     */
    public String ask(String question, int topK) {
        // 创建 VectorStoreDocumentRetriever
        // 配置：相似度阈值 0.7，返回 topK 个文档
        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.7)
                .topK(topK)
                .build();

        // 创建 RetrievalAugmentationAdvisor
        RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();

        return chatClientBuilder.build()
                .prompt()
                .system("""
                    你是一名基于知识库的 AI 助手。请严格遵守以下规则：
                    1. 只根据提供的参考资料回答
                    2. 如果资料中没有相关信息，明确说明"该问题超出了我的知识范围"
                    3. 回答要引用具体的文档来源
                    4. 用中文回答
                    """)
                .user(question)
                .advisors(ragAdvisor)  // 关键：注入 RAG Advisor
                .call()
                .content();
    }

    /**
     * <h3>对比实验：不用 RAG 直接问</h3>
     *
     * <p>这个方法不注入 RAG Advisor，让 LLM 直接回答。
     * 用来对比"有 RAG"和"无 RAG"的回答差异。</p>
     *
     * <p><b>学习任务：</b>同一个问题，分别调用 ask() 和 askWithoutRag()，
     * 观察答案的区别。</p>
     */
    public String askWithoutRag(String question) {
        return chatClientBuilder.build()
                .prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * 获取向量库中已索引的文档数量
     */
    public long getDocumentCount() {
        // SimpleVectorStore 的文档量可以通过 search 间接获取
        // 具体 API 因实现而异
        return -1; // 依赖于具体的 VectorStore 实现
    }
}
