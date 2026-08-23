package com.promising.jarvis.core.world;

import net.minecraft.server.command.ServerCommandSource;

/** Current world time and weather facts. */
public final class TimeWeatherComponent implements WorldInfoComponent {
    public String id() { return "world.time-weather"; }

    public boolean supports(String request) {
        return containsAny(request, "天气", "下雨", "雷暴", "时间", "白天", "夜晚", "晴天", "weather", "time", "rain");
    }

    public String collect(ServerCommandSource source) {
        var world = source.getWorld();
        return String.format("[世界时间与天气] 时间刻度: %d，正在下雨: %s，正在雷暴: %s",
                world.getTimeOfDay(), world.isRaining(), world.isThundering());
    }

    private static boolean containsAny(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }
}
