package com.promising.jarvis.core.companion;

import com.promising.jarvis.core.perception.GamePhaseClassifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.registry.Registries;

public final class MinecraftProactivePerception {
    private final GamePhaseClassifier phaseClassifier = new GamePhaseClassifier();

    public ProactivePerception observe(ServerPlayerEntity player) {
        var world = player.getWorld();
        var pos = player.getBlockPos();
        boolean night = world.getTimeOfDay() % 24000 >= 13000 && world.getTimeOfDay() % 24000 < 23000;
        boolean hostile = !world.getEntitiesByClass(HostileEntity.class, new Box(pos).expand(16), Entity::isAlive).isEmpty();
        Map<String, Integer> nearby = new HashMap<>();
        for (Entity entity : world.getEntitiesByClass(Entity.class, new Box(pos).expand(16), e -> e != player && e.isAlive())) {
            String id = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
            nearby.merge(id, 1, Integer::sum);
        }
        var dangers = new ArrayList<DangerSignal>();
        if (player.isSubmergedInWater() && player.getAir() <= player.getMaxAir() / 2)
            dangers.add(new DangerSignal(DangerType.DROWNING, player.getAir() <= 20 ? 3 : 2,
                    "玩家在水下，氧气余量 " + player.getAir() + "/" + player.getMaxAir()));
        if (player.isInLava()) dangers.add(new DangerSignal(DangerType.LAVA, 3, "玩家位于熔岩中"));
        if (player.isOnFire()) dangers.add(new DangerSignal(DangerType.FIRE, 3, "玩家正在着火"));
        if (player.getHealth() <= 4) dangers.add(new DangerSignal(DangerType.LOW_HEALTH, 3, "生命值 " + player.getHealth()));
        if (player.getHungerManager().getFoodLevel() <= 6) dangers.add(new DangerSignal(DangerType.LOW_HUNGER, 2, "饥饿值 " + player.getHungerManager().getFoodLevel()));
        if (hostile) dangers.add(new DangerSignal(DangerType.HOSTILE_NEARBY, 2, "16格内存在敌对生物"));
        boolean danger = !dangers.isEmpty();
        String biome = world.getBiome(pos).getKey().map(key -> key.getValue().toString()).orElse("unknown");
        var phase = phaseClassifier.classify(player);
        var evidence = new ArrayList<String>(phase.evidence());
        if (night) evidence.add("当前为夜晚");
        if (hostile) evidence.add("附近16格存在敌对生物");
        if (danger) evidence.add("玩家处于需要优先处理的危险状态");
        dangers.stream().map(DangerSignal::evidence).forEach(evidence::add);
        return new ProactivePerception(biome, world.getRegistryKey().getValue().toString(), night, hostile,
                danger, phase.phase().name(), evidence, dangers, world.isRaining(), world.isThundering(),
                world.getLightLevel(pos), world.isSkyVisible(pos), player.isSubmergedInWater(), player.isInLava(),
                player.isOnFire(), player.getAir(), player.getMaxAir(), nearby.toString());
    }
}
