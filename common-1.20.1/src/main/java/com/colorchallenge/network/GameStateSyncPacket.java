package com.colorchallenge.network;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.state.ClientGameState;
import com.colorchallenge.state.FinishedPlayer;
import com.colorchallenge.state.GameState;
import com.colorchallenge.state.GameStateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class GameStateSyncPacket {
    public static final ResourceLocation ID =
            new ResourceLocation(ColorChallenge.MOD_ID, "game_state_sync");

    /** Platform-specific packet sender, set during initialization */
    public static BiConsumer<ServerPlayer, GameStateManager> packetSender;

    public static void encode(FriendlyByteBuf buf, ServerPlayer player, GameStateManager manager) {
        buf.writeInt(manager.getState().ordinal());
        buf.writeInt(manager.getPlayTime());
        Set<DyeColor> dyes = manager.getDeliveredDyes(player.getUUID());
        buf.writeInt(dyes.size());
        for (DyeColor color : dyes) {
            buf.writeInt(color.ordinal());
        }
        List<FinishedPlayer> finished = manager.getFinishedPlayers();
        buf.writeInt(finished.size());
        for (FinishedPlayer fp : finished) {
            buf.writeUtf(fp.playerName());
            buf.writeInt(fp.finishTimeTicks());
        }
    }

    public static void applyOnClient(FriendlyByteBuf buf) {
        int stateOrdinal = buf.readInt();
        int playTime = buf.readInt();
        int dyeCount = buf.readInt();
        Set<DyeColor> deliveredDyes = EnumSet.noneOf(DyeColor.class);
        for (int i = 0; i < dyeCount; i++) {
            int ordinal = buf.readInt();
            if (ordinal >= 0 && ordinal < DyeColor.values().length) {
                deliveredDyes.add(DyeColor.values()[ordinal]);
            }
        }
        int finishedCount = buf.readInt();
        List<ClientGameState.FinishedEntry> finishedEntries = new ArrayList<>();
        for (int i = 0; i < finishedCount; i++) {
            String name = buf.readUtf();
            int finishTime = buf.readInt();
            finishedEntries.add(new ClientGameState.FinishedEntry(name, finishTime));
        }
        ClientGameState.update(GameState.values()[stateOrdinal], playTime,
                deliveredDyes, finishedEntries);
    }

    public static void sendToPlayer(ServerPlayer player, GameStateManager manager) {
        if (packetSender != null) {
            packetSender.accept(player, manager);
        }
    }
}
