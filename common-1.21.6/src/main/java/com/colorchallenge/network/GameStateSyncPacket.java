package com.colorchallenge.network;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.state.ClientGameState;
import com.colorchallenge.state.FinishedPlayer;
import com.colorchallenge.state.GameState;
import com.colorchallenge.state.GameStateManager;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class GameStateSyncPacket {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ColorChallenge.MOD_ID, "game_state_sync");

    public static void registerClientReceiver() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ID, (buf, context) -> {
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
            context.queue(() -> {
                ClientGameState.update(GameState.values()[stateOrdinal], playTime,
                        deliveredDyes, finishedEntries);
            });
        });
    }

    public static void sendToPlayer(ServerPlayer player, GameStateManager manager) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
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
        NetworkManager.sendToPlayer(player, ID, buf);
    }
}
