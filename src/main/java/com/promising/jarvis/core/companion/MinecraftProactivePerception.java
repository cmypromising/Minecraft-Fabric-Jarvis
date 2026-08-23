package com.promising.jarvis.core.companion;

import com.promising.jarvis.core.perception.GamePhaseClassifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.util.ArrayList;

public final class MinecraftProactivePerception {
    private final GamePhaseClassifier phaseClassifier = new GamePhaseClassifier();

    public ProactivePerception observe(ServerPlayerEntity player) {
        var world = player.getWorld();
        var pos = player.getBlockPos();
        boolean night = world.getTimeOfDay() % 24000 >= 13000 && world.getTimeOfDay() % 24000 < 23000;
        boolean hostile = !world.getEntitiesByClass(HostileEntity.class, new Box(pos).expand(16), Entity::isAlive).isEmpty();
        boolean danger = player.getHealth() <= 4 || player.getHungerManager().getFoodLevel() <= 6
                || player.isSubmergedInWater() || player.isInLava() || player.isOnFire() || hostile;
        String biome = world.getBiome(pos).getKey().map(key -> key.getValue().toString()).orElse("unknown");
        var phase = phaseClassifier.classify(player);
        var evidence = new ArrayList<String>(phase.evidence());
        if (night) evidence.add("当前为夜晚");
        if (hostile) evidence.add("附近16格存在敌对生物");
        if (danger) evidence.add("玩家处于需要优先处理的危险状态");
        return new ProactivePerception(biome, world.getRegistryKey().getValue().toString(), night, hostile,
                danger, phase.phase().name(), evidence);
    }
}
