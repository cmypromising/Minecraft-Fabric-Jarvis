package com.promising.jarvis.core.observation;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Captures a bounded Minecraft player snapshot; callers must invoke it on the server thread. */
public final class MinecraftPlayerStateObserver implements PlayerStateObserver {
    private static final int MAX_RESOURCE_TYPES = 128;

    @Override
    public Optional<PlayerStateSnapshot> observe(ServerCommandSource source) {
        if (source == null) return Optional.empty();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return Optional.empty();

        Map<String, Integer> resources = new HashMap<>();
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isEmpty()) continue;
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            if (resources.containsKey(itemId) || resources.size() < MAX_RESOURCE_TYPES) {
                resources.merge(itemId, stack.getCount(), Integer::sum);
            }
        }

        var position = player.getBlockPos();
        return Optional.of(new PlayerStateSnapshot(
                player.getUuid(), source.getName(), player.getHealth(),
                player.getHungerManager().getFoodLevel(), player.experienceLevel,
                position.getX(), position.getY(), position.getZ(),
                player.getWorld().getRegistryKey().getValue().toString(),
                player.getWorld().getDifficulty(), player.interactionManager.getGameMode(), resources));
    }
}
