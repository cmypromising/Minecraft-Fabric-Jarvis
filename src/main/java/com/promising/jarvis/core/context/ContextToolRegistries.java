package com.promising.jarvis.core.context;

import com.promising.jarvis.core.world.DifficultyComponent;
import com.promising.jarvis.core.world.LocationComponent;
import com.promising.jarvis.core.world.TimeWeatherComponent;

/** Composition root for the built-in read-only context tools. */
public final class ContextToolRegistries {
    private ContextToolRegistries() { }

    public static ContextToolRegistry defaults() {
        return new ContextToolRegistry()
                .register(componentTool("world.location", "当前玩家所在维度和方块坐标", new LocationComponent()))
                .register(componentTool("world.time_weather", "当前世界时间、降雨和雷暴状态", new TimeWeatherComponent()))
                .register(componentTool("world.rules", "当前世界难度和玩家游戏模式", new DifficultyComponent()))
                .register(componentTool("player.status", "当前玩家生命、饥饿和经验等级", new PlayerStatusComponent()));
    }

    private static ContextTool componentTool(String name, String description, ContextComponent component) {
        return new ContextTool() {
            public String name() { return name; }
            public String description() { return description; }
            public String execute(net.minecraft.server.command.ServerCommandSource source, String arguments) {
                return component.collect(source);
            }
        };
    }
}
