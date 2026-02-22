package com.colorchallenge.state;

import net.minecraft.world.item.DyeColor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ClientGameState {
    private static GameState state = GameState.NOT_STARTED;
    private static int playTime = 0;
    private static Set<DyeColor> deliveredDyes = EnumSet.noneOf(DyeColor.class);
    private static List<FinishedEntry> finishedPlayers = Collections.emptyList();

    public record FinishedEntry(String playerName, int finishTimeTicks) {
    }

    public static void update(GameState state, int playTime,
                              Set<DyeColor> deliveredDyes,
                              List<FinishedEntry> finishedPlayers) {
        ClientGameState.state = state;
        ClientGameState.playTime = playTime;
        ClientGameState.deliveredDyes = deliveredDyes;
        ClientGameState.finishedPlayers = finishedPlayers;
    }

    public static GameState getState() {
        return state;
    }

    public static int getPlayTime() {
        return playTime;
    }

    public static Set<DyeColor> getDeliveredDyes() {
        return Collections.unmodifiableSet(deliveredDyes);
    }

    public static int getDeliveredCount() {
        return deliveredDyes.size();
    }

    public static List<FinishedEntry> getFinishedPlayers() {
        return finishedPlayers;
    }

    public static void reset() {
        state = GameState.NOT_STARTED;
        playTime = 0;
        deliveredDyes = EnumSet.noneOf(DyeColor.class);
        finishedPlayers = Collections.emptyList();
    }
}
