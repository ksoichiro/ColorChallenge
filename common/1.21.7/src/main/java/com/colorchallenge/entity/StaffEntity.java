package com.colorchallenge.entity;

import com.colorchallenge.network.GameStateSyncPacket;
import com.colorchallenge.state.GameState;
import com.colorchallenge.state.GameStateManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class StaffEntity extends Mob {
    private static final EntityDataAccessor<Integer> DATA_UNHAPPY_COUNTER =
            SynchedEntityData.defineId(StaffEntity.class, EntityDataSerializers.INT);
    private int lookAtCounter;
    private float originalYRot;
    private float originalYBodyRot;
    private float originalYHeadRot;

    public StaffEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setNoAi(true);
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_UNHAPPY_COUNTER, 0);
    }

    public int getUnhappyCounter() {
        return this.entityData.get(DATA_UNHAPPY_COUNTER);
    }

    private void setUnhappyCounter(int counter) {
        this.entityData.set(DATA_UNHAPPY_COUNTER, counter);
    }

    @Override
    public void tick() {
        super.tick();
        if (getUnhappyCounter() > 0) {
            setUnhappyCounter(getUnhappyCounter() - 1);
        }
        if (level().isClientSide()) {
            // Stationary noAi mob: always keep body facing same direction as head
            yBodyRot = yHeadRot;
        } else if (lookAtCounter > 0) {
            lookAtCounter--;
            if (lookAtCounter == 0) {
                setYRot(originalYRot);
                yBodyRot = originalYBodyRot;
                yHeadRot = originalYHeadRot;
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Face the player
        if (lookAtCounter <= 0) {
            originalYRot = getYRot();
            originalYBodyRot = yBodyRot;
            originalYHeadRot = yHeadRot;
        }
        double dx = player.getX() - this.getX();
        double dz = player.getZ() - this.getZ();
        float targetYRot = (float) (Mth.atan2(dz, dx) * (180.0F / (float) Math.PI)) - 90.0F;
        this.setYRot(targetYRot);
        this.yBodyRot = targetYRot;
        this.yHeadRot = targetYRot;
        lookAtCounter = 10;

        GameStateManager manager = GameStateManager.getInstance();
        if (manager == null) {
            return InteractionResult.FAIL;
        }

        if (!manager.canDeliver()) {
            player.displayClientMessage(Component.translatable("message.colorchallenge.game_not_started"), false);
            setUnhappyCounter(20);
            this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            return InteractionResult.CONSUME;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof DyeItem dyeItem)) {
            player.displayClientMessage(Component.translatable("message.colorchallenge.hold_dye"), false);
            setUnhappyCounter(20);
            this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            return InteractionResult.CONSUME;
        }

        DyeColor color = dyeItem.getDyeColor();
        ServerPlayer serverPlayer = (ServerPlayer) player;

        if (manager.getDeliveredDyes(player.getUUID()).contains(color)) {
            player.displayClientMessage(Component.translatable("message.colorchallenge.already_delivered", color.getName()), false);
            setUnhappyCounter(20);
            this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            return InteractionResult.CONSUME;
        }

        // Deliver the dye
        heldItem.shrink(1);
        manager.deliverDye(player.getUUID(), color);
        serverPlayer.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);

        // Check if this player just delivered all dyes
        if (manager.hasDeliveredAll(player.getUUID())
                && !manager.hasFinished(player.getUUID())) {
            manager.recordFinish(player.getUUID(), player.getDisplayName().getString());
            if (manager.getState() == GameState.IN_PROGRESS) {
                manager.end();
                manager.broadcastWinner(serverPlayer);
            } else {
                manager.broadcastGoalReached(serverPlayer);
            }
            // Sync ranking to all players
            if (level().getServer() != null) {
                for (ServerPlayer p : level().getServer().getPlayerList().getPlayers()) {
                    GameStateSyncPacket.sendToPlayer(p, manager);
                }
            }
        } else {
            GameStateSyncPacket.sendToPlayer(serverPlayer, manager);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }
}
