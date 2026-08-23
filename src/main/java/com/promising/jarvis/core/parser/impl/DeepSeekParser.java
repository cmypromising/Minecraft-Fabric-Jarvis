package com.promising.jarvis.core.parser.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.promising.jarvis.Jarvis;
import com.promising.jarvis.core.parser.NLParser;
import com.promising.jarvis.llm.client.impl.DeepSeekLLMClient;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import com.promising.jarvis.llm.deepseek.DeepSeekResponseBody;

/**
 * 通过大模型解析用户需求
 */
public class DeepSeekParser implements NLParser {
    private static final Gson gson = new GsonBuilder().create();
    private final DeepSeekLLMClient deepSeekClient;

    public DeepSeekParser() {
        this.deepSeekClient = new DeepSeekLLMClient();
    }

    @Override
    public ContentResponseBody parse(String context, String currentPlayerInfo) {
        try {
            String jsonResponse = deepSeekClient.getResponse(context, currentPlayerInfo);
            DeepSeekResponseBody deepSeekResponseBody = gson.fromJson(jsonResponse, DeepSeekResponseBody.class);

            if (deepSeekResponseBody == null ||
                    deepSeekResponseBody.getChoices() == null ||
                    deepSeekResponseBody.getChoices().isEmpty()) {
                Jarvis.LOGGER.error("Invalid API response structure");
                return null;
            }

            return deepSeekResponseBody.getChoices().getFirst().getMessage().getContent();
        } catch (Exception e) {
            Jarvis.LOGGER.error("Error processing DeepSeek API response", e);
            throw new RuntimeException(e);
        }
    }

}
