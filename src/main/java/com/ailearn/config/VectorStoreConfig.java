package com.ailearn.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量存储配置类
 *
 * <p>Spring AI 1.0.0 中 SimpleVectorStore 没有自动配置 Starter，
 * 需要手动创建 Bean。</p>
 */
@Configuration
public class VectorStoreConfig {

    /**
     * 创建基于内存的 SimpleVectorStore
     *
     * @param embeddingModel OpenAI EmbeddingModel（由 spring-ai-starter-model-openai 自动配置）
     * @return VectorStore 实例
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
