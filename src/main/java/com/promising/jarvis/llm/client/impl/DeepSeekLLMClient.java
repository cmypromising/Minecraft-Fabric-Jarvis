package com.promising.jarvis.llm.client.impl;

import com.promising.jarvis.Jarvis;
import com.promising.jarvis.ModConfig;
import com.promising.jarvis.llm.client.LLMClient;
import com.promising.jarvis.llm.deepseek.DeepSeekRequestBody;
import joptsimple.internal.Strings;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DeepSeekLLMClient implements LLMClient {

    private final HttpClient client;
    private static final String JSON_MEDIA_TYPE = "application/json";

    public DeepSeekLLMClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(ModConfig.INSTANCE.connectionTimeoutSeconds))
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    public String getResponse(String userMessage, String currentPlayerInfo) throws IOException, InterruptedException {
        // 检查 API Token 是否配置
        if (!ModConfig.INSTANCE.isApiTokenValid()) {
            throw new IllegalStateException("DeepSeek API Token 未配置！请在模组配置中设置 API Token");
        }

        String formattedMessage = formatMessage(userMessage, currentPlayerInfo);

        Jarvis.LOGGER.info("formattedUserMessage: {}", formattedMessage);

        DeepSeekRequestBody requestBody = DeepSeekRequestBody.builder()
                .addSystemPrompt("minecraft_assistant")
                .addUserMessage(formattedMessage)
                .setTemperature(ModConfig.INSTANCE.getTemperature())
                .setMaxTokens(ModConfig.INSTANCE.maxTokens)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ModConfig.INSTANCE.deepSeekBaseUrl))
                .header("Content-Type", JSON_MEDIA_TYPE)
                .header("Accept", JSON_MEDIA_TYPE)
                .header("Authorization", "Bearer " + ModConfig.INSTANCE.getApiToken())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toJson()))
                .timeout(Duration.ofSeconds(ModConfig.INSTANCE.requestTimeoutSeconds))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Jarvis.LOGGER.debug("Response Code: {}", response.statusCode());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            String errorDetails = response.body();
            Jarvis.LOGGER.error("API调用失败. 状态码: {}, 错误信息: {}",
                    response.statusCode(),
                    errorDetails != null ? errorDetails : "无错误详情");
            throw new IOException("API调用失败. 状态码: " + response.statusCode());
        }
    }

    /**
     * 格式化用户消息，包含玩家信息
     */
    private String formatMessage(String userMessage, String currentPlayerInfo) {
        if (Strings.isNullOrEmpty(currentPlayerInfo)) {
            return userMessage;
        }

        return String.format("当前时刻主人基本信息{%s},主人需求{\n%s\n}",
                currentPlayerInfo,
                userMessage);
    }
}