package com.colorchallenge.state;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.DyeColor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GameStateManager {
    private static final String DATA_ID = "colorchallenge_game_state";
    public static final int TARGET_DYE_COUNT = 16;

    private static final int COUNTDOWN_SECONDS = 5;
    private static final int TICKS_PER_SECOND = 20;
    private static final int TRADER_UNLOCK_TICKS = 40 * 60 * TICKS_PER_SECOND; // 40 minutes

    private static GameStateManager instance;
    private final GameStateSavedData savedData;
    private ServerLevel serverLevel;
    private int countdownTicks = -1;

    private GameStateManager(GameStateSavedData savedData) {
        this.savedData = savedData;
    }

    public static void init(ServerLevel level) {
        GameStateSavedData data = level.getDataStorage().computeIfAbsent(
                GameStateSavedData::load, GameStateSavedData::new,
                DATA_ID
        );
        instance = new GameStateManager(data);
        instance.serverLevel = level;
    }

    public static GameStateManager getInstance() {
        return instance;
    }

    public static void clear() {
        instance = null;
    }

    // State

    public GameState getState() {
        return savedData.state;
    }

    // State transitions

    public void startCountdown() {
        countdownTicks = COUNTDOWN_SECONDS * TICKS_PER_SECOND;
        savedData.deliveredDyes.clear();
        savedData.playTime = 0;
        savedData.setDirty();
    }

    public boolean isCountdownActive() {
        return countdownTicks >= 0;
    }

    private void start() {
        savedData.state = GameState.IN_PROGRESS;
        savedData.setDirty();
    }

    public void end() {
        savedData.state = GameState.ENDED;
        savedData.setDirty();
    }

    public void reset() {
        savedData.state = GameState.NOT_STARTED;
        savedData.playTime = 0;
        savedData.deliveredDyes.clear();
        savedData.finishedPlayers.clear();
        savedData.knownPlayers.clear();
        countdownTicks = -1;
        savedData.setDirty();
    }

    // Dye delivery

    public void deliverDye(UUID playerId, DyeColor color) {
        savedData.deliveredDyes
                .computeIfAbsent(playerId, k -> EnumSet.noneOf(DyeColor.class))
                .add(color);
        savedData.setDirty();
    }

    public Set<DyeColor> getDeliveredDyes(UUID playerId) {
        return Collections.unmodifiableSet(
                savedData.deliveredDyes.getOrDefault(playerId, EnumSet.noneOf(DyeColor.class)));
    }

    public int getDeliveredCount(UUID playerId) {
        Set<DyeColor> dyes = savedData.deliveredDyes.get(playerId);
        return dyes == null ? 0 : dyes.size();
    }

    public boolean hasDeliveredAll(UUID playerId) {
        return getDeliveredCount(playerId) >= TARGET_DYE_COUNT;
    }

    // Play time

    public int getPlayTime() {
        return savedData.playTime;
    }

    public void tick() {
        if (countdownTicks >= 0) {
            tickCountdown();
        }
        if (savedData.state == GameState.IN_PROGRESS || savedData.state == GameState.ENDED) {
            savedData.playTime++;
            savedData.setDirty();
        }
    }

    private void tickCountdown() {
        if (countdownTicks > 0 && countdownTicks % TICKS_PER_SECOND == 0) {
            int secondsLeft = countdownTicks / TICKS_PER_SECOND;
            broadcastTitle(Component.literal(String.valueOf(secondsLeft)), 0, 25, 0);
        } else if (countdownTicks == 0) {
            broadcastTitle(
                    Component.translatable("message.colorchallenge.game_started"),
                    0, 40, 10
            );
            start();
        }
        countdownTicks--;
    }

    private void broadcastTitle(Component title, int fadeIn, int stay, int fadeOut) {
        if (serverLevel == null || serverLevel.getServer() == null) return;
        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
        }
    }

    private void broadcastTitleWithSubtitle(Component title, Component subtitle,
                                              int fadeIn, int stay, int fadeOut) {
        if (serverLevel == null || serverLevel.getServer() == null) return;
        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        }
    }

    private void broadcastMessage(Component message) {
        if (serverLevel == null || serverLevel.getServer() == null) return;
        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }
    }

    public void broadcastWinner(ServerPlayer winner) {
        Component title = Component.translatable("message.colorchallenge.winner_title");
        Component subtitle = Component.translatable("message.colorchallenge.winner_subtitle", winner.getDisplayName());
        if (serverLevel == null || serverLevel.getServer() == null) return;
        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    public void broadcastGoalReached(ServerPlayer finisher) {
        Component title = Component.translatable("message.colorchallenge.goal_reached_title");
        Component subtitle = Component.translatable("message.colorchallenge.goal_reached_subtitle", finisher.getDisplayName());
        if (serverLevel == null || serverLevel.getServer() == null) return;
        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    // State checks

    public boolean isTraderItemsUnlocked() {
        return savedData.playTime >= TRADER_UNLOCK_TICKS;
    }

    public boolean canDeliver() {
        return savedData.state == GameState.IN_PROGRESS || savedData.state == GameState.ENDED;
    }

    public boolean canStart() {
        return savedData.state == GameState.NOT_STARTED && !isCountdownActive();
    }

    public boolean canReset() {
        return savedData.state == GameState.IN_PROGRESS || savedData.state == GameState.ENDED;
    }

    // Finish tracking

    public boolean hasFinished(UUID playerId) {
        return savedData.finishedPlayers.stream()
                .anyMatch(fp -> fp.playerId().equals(playerId));
    }

    public void recordFinish(UUID playerId, String playerName) {
        savedData.finishedPlayers.add(new FinishedPlayer(playerId, playerName, savedData.playTime));
        savedData.setDirty();
    }

    public List<FinishedPlayer> getFinishedPlayers() {
        return savedData.finishedPlayers;
    }

    // Market generation

    public boolean isMarketGenerated() {
        return savedData.marketGenerated;
    }

    public void setMarketGenerated() {
        savedData.marketGenerated = true;
        savedData.setDirty();
    }

    // Known players tracking

    public boolean isKnownPlayer(UUID playerId) {
        return savedData.knownPlayers.contains(playerId);
    }

    public void markPlayerKnown(UUID playerId) {
        savedData.knownPlayers.add(playerId);
        savedData.setDirty();
    }

    public ServerLevel getServerLevel() {
        return serverLevel;
    }
}
