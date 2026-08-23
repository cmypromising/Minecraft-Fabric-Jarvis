package com.promising.jarvis.core.perception;

import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

/** Deterministic first approximation; every result carries its evidence. */
public final class GamePhaseClassifier {
    public GamePhaseObservation classify(ServerPlayerEntity player) {
        if (player == null) return new GamePhaseObservation(GamePhase.UNKNOWN, 0, List.of("玩家不可用"));
        String dimension = player.getWorld().getRegistryKey().getValue().toString();
        var evidence = new ArrayList<String>();
        if (dimension.equals("minecraft:the_end")) {
            boolean dragonEgg = has(player, Items.DRAGON_EGG);
            evidence.add("所在维度: minecraft:the_end");
            if (dragonEgg) evidence.add("背包包含龙蛋");
            return new GamePhaseObservation(dragonEgg ? GamePhase.POST_DRAGON : GamePhase.END,
                    dragonEgg ? 0.98 : 0.95, evidence);
        }
        if (dimension.equals("minecraft:the_nether")) {
            evidence.add("所在维度: minecraft:the_nether");
            return new GamePhaseObservation(GamePhase.NETHER, 0.96, evidence);
        }
        if (player.isDead() || player.getHealth() <= 0) {
            return new GamePhaseObservation(GamePhase.RECOVERY, 0.9, List.of("玩家处于死亡/恢复状态"));
        }
        int score = 0;
        if (has(player, Items.IRON_PICKAXE) || has(player, Items.IRON_INGOT)) { score += 1; evidence.add("拥有铁制工具或铁锭"); }
        if (has(player, Items.DIAMOND) || has(player, Items.DIAMOND_PICKAXE)) { score += 2; evidence.add("拥有钻石资源"); }
        if (has(player, Items.NETHERITE_INGOT) || has(player, Items.NETHERITE_PICKAXE)) { score += 3; evidence.add("拥有下界合金资源"); }
        if (player.experienceLevel >= 10) { score += 1; evidence.add("经验等级较高: " + player.experienceLevel); }
        if (score >= 3) return new GamePhaseObservation(GamePhase.ESTABLISHED_SURVIVAL, 0.75, evidence);
        evidence.add("缺少明确的高级进度信号");
        return new GamePhaseObservation(GamePhase.EARLY_SURVIVAL, 0.65, evidence);
    }

    private static boolean has(ServerPlayerEntity player, net.minecraft.item.Item item) {
        return player.getInventory().contains(stack -> stack.isOf(item));
    }
}
