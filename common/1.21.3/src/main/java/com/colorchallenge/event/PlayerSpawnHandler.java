package com.colorchallenge.event;

import com.colorchallenge.registry.ModItems;
import com.colorchallenge.state.GameStateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PlayerSpawnHandler {
    public static void onPlayerRespawn(ServerPlayer player, boolean conqueredEnd, Entity.RemovalReason removalReason) {
        if (conqueredEnd) return;
        // Teleport to world spawn to bypass Minecraft's safe-spawn search
        // that places players on the roof of the market structure
        if (player.getRespawnPosition() == null) {
            teleportToWorldSpawn(player);
        }
        grantEquipment(player);
        applyNightVision(player);
    }

    public static void onPlayerJoin(ServerPlayer player) {
        GameStateManager gsm = GameStateManager.getInstance();
        if (gsm != null && !gsm.isKnownPlayer(player.getUUID())) {
            teleportToWorldSpawn(player);
            gsm.markPlayerKnown(player.getUUID());
        }
        grantEquipment(player);
        applyNightVision(player);
    }

    private static void teleportToWorldSpawn(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        BlockPos spawnPos = overworld.getSharedSpawnPos();
        player.teleportTo(overworld,
                spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                java.util.Set.of(), player.getYRot(), player.getXRot(), true);
    }

    public static void resetPlayer(ServerPlayer player) {
        player.getInventory().clearContent();
        grantEquipment(player);
        applyNightVision(player);
    }

    private static void grantEquipment(ServerPlayer player) {
        // Instructions item (only if player doesn't already have one)
        if (!hasInstructions(player)) {
            player.getInventory().add(new ItemStack(ModItems.INSTRUCTIONS.get()));
        }
        // 1 stack of Bread
        player.getInventory().add(new ItemStack(Items.BREAD, 64));
    }

    private static boolean hasInstructions(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.INSTRUCTIONS.get())) {
                return true;
            }
        }
        return false;
    }

    private static void applyNightVision(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                Integer.MAX_VALUE,
                0,
                true,  // ambient (no swirling particles)
                false, // visible (no particles)
                false  // showIcon (hide from HUD)
        ));
    }
}
