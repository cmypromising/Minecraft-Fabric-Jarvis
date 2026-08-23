package com.promising.jarvis;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "jarvis")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    public static ModConfig INSTANCE;

    public static void init()
    {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    // === LLM 客户端配置 ===
    @ConfigEntry.Category("llm")
    @ConfigEntry.Gui.Tooltip()
    @Comment("DeepSeek API的基础URL")
    public String deepSeekBaseUrl = "https://api.deepseek.com/chat/completions";

    @ConfigEntry.Category("llm")
    @ConfigEntry.Gui.Tooltip()
    @Comment("HTTP连接超时时间（秒）")
    @ConfigEntry.BoundedDiscrete(min = 5, max = 120)
    public int connectionTimeoutSeconds = 30;

    @ConfigEntry.Category("llm")
    @ConfigEntry.Gui.Tooltip()
    @Comment("HTTP请求超时时间（秒）")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 300)
    public int requestTimeoutSeconds = 60;

    @ConfigEntry.Category("llm")
    @ConfigEntry.Gui.Tooltip()
    @Comment("LLM响应的创造性程度 (0.0-2.0, 越高越有创造性)")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 200)
    public int temperatureInt = 70; // 存储为整数，使用时除以100

    @ConfigEntry.Category("llm")
    @ConfigEntry.Gui.Tooltip()
    @Comment("LLM响应的最大Token数量")
    @ConfigEntry.BoundedDiscrete(min = 100, max = 4096)
    public int maxTokens = 1024;

    // === API 认证配置 ===
    @ConfigEntry.Category("api")
    @ConfigEntry.Gui.Tooltip()
    @Comment("DeepSeek API Token（必需）")
    public String deepSeekApiToken = "";

    // === 辅助方法 ===

    /**
     * 获取温度值（转换为小数）
     */
    public double getTemperature() {
        return temperatureInt / 100.0;
    }

    /**
     * 检查 API Token 是否有效
     */
    public boolean isApiTokenValid() {
        return deepSeekApiToken != null && !deepSeekApiToken.trim().isEmpty();
    }

    /**
     * 获取处理后的 API Token
     */
    public String getApiToken() {
        return deepSeekApiToken != null ? deepSeekApiToken.trim() : "";
    }

}
