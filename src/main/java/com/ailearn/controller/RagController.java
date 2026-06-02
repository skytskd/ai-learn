package com.ailearn.controller;

import com.ailearn.model.ChatRequest;
import com.ailearn.model.ChatResponse;
import com.ailearn.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * <h1>🔍 RAG 知识库问答 Controller（第 4 课配套）</h1>
 *
 * <p>基于检索增强生成（RAG）的问答接口。
 * 在回答问题前，先从向量数据库中检索相关文档。</p>
 *
 * <h2>接口列表</h2>
 * <table border="1">
 *   <tr><th>接口</th><th>方法</th><th>说明</th></tr>
 *   <tr><td>/api/rag/ask</td><td>POST</td><td>RAG 问答</td></tr>
 *   <tr><td>/api/rag/ask-bare</td><td>POST</td><td>不用 RAG 的对比接口</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private static final Logger log = LoggerFactory.getLogger(RagController.class);
    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * <h3>RAG 知识库问答</h3>
     *
     * <p>从项目内置的 rag-docs 目录中检索相关内容来回答问题。</p>
     *
     * <pre>{@code
     * POST /api/rag/ask
     * { "message": "HashMap 的扩容机制是什么？" }
     *
     * // 响应（基于知识库文档）：
     * // HashMap 的扩容机制是：当元素数量超过 阈值（capacity * loadFactor）时...
     * //
     * // 📚 参考文档：java-interview-1.txt
     * }</pre>
     *
     * @param request 用户请求（message = 问题）
     * @param topK    检索最相关的文档块数量（默认 5）
     */
    @PostMapping("/ask")
    public ChatResponse askWithRag(
            @RequestBody ChatRequest request,
            @RequestParam(defaultValue = "5") int topK) {

        log.info("🔍 RAG 问答请求 [topK={}]：{}", topK, request.getMessage());

        try {
            String answer = ragService.ask(request.getMessage(), topK);
            return ChatResponse.success(answer, request.getConversationId());
        } catch (Exception e) {
            log.error("❌ RAG 问答失败", e);
            return ChatResponse.error("RAG 问答失败：" + e.getMessage());
        }
    }

    /**
     * <h3>不使用 RAG 的对比接口</h3>
     *
     * <p>同样的 API Key / 同样的模型，但不使用 RAG。
     * 用于对比"有 RAG"和"无 RAG"的回答差异。</p>
     *
     * <p><b>对比实验：</b></p>
     * <ol>
     *   <li>先问一个 rag-docs 文档中有答案的问题（如知识库中提到的技术点）</li>
     *   <li>对比 ask 和 ask-bare 的回答</li>
     *   <li>观察 RAG 带来的信息增量</li>
     * </ol>
     */
    @PostMapping("/ask-bare")
    public ChatResponse askWithoutRag(@RequestBody ChatRequest request) {
        log.info("📝 无 RAG 问答请求：{}", request.getMessage());

        try {
            String answer = ragService.askWithoutRag(request.getMessage());
            return ChatResponse.success(answer, request.getConversationId());
        } catch (Exception e) {
            log.error("❌ 问答失败", e);
            return ChatResponse.error("问答失败：" + e.getMessage());
        }
    }
}
