package com.promising.jarvis.core.perception;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Box;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/** Aggregates nearby entities to keep hostile surroundings visible without context explosion. */
public final class NearbyEntitiesContextTool implements com.promising.jarvis.core.context.ContextTool {
    private static final double RADIUS = 16;
    private static final int MAX_TYPES = 20;
    public String name() { return "world.nearby_entities"; }
    public String description() { return "读取玩家16格范围内的生物，按类型聚合数量、最近距离和敌对分类"; }
    public String execute(ServerCommandSource source, String arguments) {
        if (source == null || source.getPlayer() == null) return "玩家不可用";
        var player = source.getPlayer();
        Map<String, Summary> summaries = new LinkedHashMap<>();
        for (Entity entity : player.getWorld().getEntitiesByClass(Entity.class,
                new Box(player.getBlockPos()).expand(RADIUS), e -> e != player && !e.isSpectator())) {
            String id = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
            Summary old = summaries.get(id);
            double distance = entity.distanceTo(player);
            String kind = kind(entity);
            summaries.put(id, old == null ? new Summary(1, distance, kind) : new Summary(old.count + 1, Math.min(old.nearest, distance), kind));
        }
        if (summaries.isEmpty()) return "[附近生物] 16格内没有发现其他实体";
        return summaries.entrySet().stream().sorted(Comparator.comparingDouble(e -> e.getValue().nearest)).limit(MAX_TYPES)
                .map(e -> String.format("%s x%d(最近%.1f格,%s)", e.getKey(), e.getValue().count, e.getValue().nearest, e.getValue().kind))
                .collect(java.util.stream.Collectors.joining("；", "[附近生物] ", ""));
    }
    private static String kind(Entity entity) {
        if (entity instanceof HostileEntity) return "敌对";
        if (entity instanceof AnimalEntity) return "被动";
        if (entity instanceof PlayerEntity) return "玩家";
        return "其他";
    }
    private record Summary(int count, double nearest, String kind) { }
}
