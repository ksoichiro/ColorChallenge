package com.colorchallenge.block;

import com.colorchallenge.state.GameStateManager;
import net.minecraft.core.BlockPos;
import com.colorchallenge.state.GameState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GameStartBlock extends Block {
    public GameStartBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        GameStateManager manager = GameStateManager.getInstance();
        if (manager == null) {
            return InteractionResult.FAIL;
        }
        if (manager.canStart()) {
            manager.startCountdown();
            return InteractionResult.SUCCESS;
        }
        if (manager.isCountdownActive()) {
            player.sendSystemMessage(Component.translatable("message.colorchallenge.countdown_in_progress"));
        } else if (manager.getState() == GameState.IN_PROGRESS) {
            player.sendSystemMessage(Component.translatable("message.colorchallenge.already_in_progress"));
        } else if (manager.getState() == GameState.ENDED) {
            player.sendSystemMessage(Component.translatable("message.colorchallenge.game_ended_reset_required"));
        }
        return InteractionResult.CONSUME;
    }
}
