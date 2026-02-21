package com.colorchallenge.network;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.state.ClientGameState;
import com.colorchallenge.state.FinishedPlayer;
import com.colorchallenge.state.GameState;
import com.colorchallenge.state.GameStateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class GameStateSyncPacket {
    public static final CustomPacketPayload.Type<Payload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ColorChallenge.MOD_ID, "game_state_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, Payload> STREAM_CODEC =
            StreamCodec.of(GameStateSyncPacket::encode, GameStateSyncPacket::decode);

    /** Platform-specific packet sender, set during initialization */
    public static BiConsumer<ServerPlayer, GameStateManager> packetSender;

    public record Payload(
            int stateOrdinal,
            int playTime,
            Set<DyeColor> deliveredDyes,
            List<ClientGameState.FinishedEntry> finishedPlayers
    ) implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static Payload createPayload(ServerPlayer player, GameStateManager manager) {
        Set<DyeColor> originalDyes = manager.getDeliveredDyes(player.getUUID());
        Set<DyeColor> dyes = originalDyes.isEmpty() ? EnumSet.noneOf(DyeColor.class) : EnumSet.copyOf(originalDyes);
        List<ClientGameState.FinishedEntry> entries = new ArrayList<>();
        for (FinishedPlayer fp : manager.getFinishedPlayers()) {
            entries.add(new ClientGameState.FinishedEntry(fp.playerName(), fp.finishTimeTicks()));
        }
        return new Payload(manager.getState().ordinal(), manager.getPlayTime(), dyes, entries);
    }

    private static void encode(RegistryFriendlyByteBuf buf, Payload payload) {
        buf.writeInt(payload.stateOrdinal());
        buf.writeInt(payload.playTime());
        buf.writeInt(payload.deliveredDyes().size());
        for (DyeColor color : payload.deliveredDyes()) {
            buf.writeInt(color.ordinal());
        }
        buf.writeInt(payload.finishedPlayers().size());
        for (ClientGameState.FinishedEntry entry : payload.finishedPlayers()) {
            buf.writeUtf(entry.playerName());
            buf.writeInt(entry.finishTimeTicks());
        }
    }

    private static Payload decode(RegistryFriendlyByteBuf buf) {
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
        return new Payload(stateOrdinal, playTime, deliveredDyes, finishedEntries);
    }

    public static void applyOnClient(Payload payload) {
        ClientGameState.update(
                GameState.values()[payload.stateOrdinal()],
                payload.playTime(),
                payload.deliveredDyes(),
                payload.finishedPlayers()
        );
    }

    public static void sendToPlayer(ServerPlayer player, GameStateManager manager) {
        if (packetSender != null) {
            packetSender.accept(player, manager);
        }
    }
}
