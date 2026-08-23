package com.promising.jarvis.core.perception;

import net.minecraft.server.command.ServerCommandSource;

/** Bounded environment facts, collected only when the ReAct agent requests them. */
public final class WorldEnvironmentContextTool implements com.promising.jarvis.core.context.ContextTool {
    public String name() { return "world.environment"; }
    public String description() { return "读取玩家当前生物群系、光照、昼夜、天气、朝向、地形和危险状态"; }
    public String execute(ServerCommandSource source, String arguments) {
        if (source == null || source.getPlayer() == null) return "玩家不可用";
        var player = source.getPlayer();
        var world = player.getWorld();
        var pos = player.getBlockPos();
        String biome = world.getBiome(pos).getKey().map(key -> key.getValue().toString()).orElse("unknown");
        long dayTime = world.getTimeOfDay() % 24000;
        String period = dayTime < 12000 ? "白天" : "夜晚";
        return String.format("[环境] 生物群系=%s，维度=%s，时间刻=%d(%s)，降雨=%s，雷暴=%s，光照=%d，天空可见=%s，坐标=(%d,%d,%d)，朝向=%s，水中=%s，熔岩中=%s，着火=%s",
                biome, world.getRegistryKey().getValue(), dayTime, period, world.isRaining(), world.isThundering(),
                world.getLightLevel(pos), world.isSkyVisible(pos), pos.getX(), pos.getY(), pos.getZ(),
                facing(player.getYaw()), player.isSubmergedInWater(), player.isInLava(), player.isOnFire());
    }

    private static String facing(float yaw) {
        int index = Math.floorMod((int) Math.floor(yaw / 90.0f + 0.5f), 4);
        return new String[]{"南", "西", "北", "东"}[index];
    }
}
