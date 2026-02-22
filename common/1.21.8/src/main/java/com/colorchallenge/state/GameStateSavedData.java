package com.colorchallenge.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameStateSavedData extends SavedData {
    GameState state = GameState.NOT_STARTED;
    final Map<UUID, Set<DyeColor>> deliveredDyes = new HashMap<>();
    final List<FinishedPlayer> finishedPlayers = new ArrayList<>();
    final Set<UUID> knownPlayers = new HashSet<>();
    int playTime = 0;
    boolean marketGenerated = false;

    public static final Codec<GameStateSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("state", 0).forGetter(d -> d.state.ordinal()),
            Codec.INT.optionalFieldOf("playTime", 0).forGetter(d -> d.playTime),
            Codec.BOOL.optionalFieldOf("marketGenerated", false).forGetter(d -> d.marketGenerated),
            Codec.unboundedMap(Codec.STRING, Codec.INT_STREAM).optionalFieldOf("deliveredDyes", Map.of())
                    .forGetter(d -> {
                        Map<String, java.util.stream.IntStream> map = new HashMap<>();
                        d.deliveredDyes.forEach((uuid, colors) ->
                                map.put(uuid.toString(), colors.stream().mapToInt(DyeColor::ordinal)));
                        return map;
                    }),
            FinishedPlayer.CODEC.listOf().optionalFieldOf("finishedPlayers", List.of())
                    .forGetter(d -> d.finishedPlayers),
            Codec.STRING.listOf().optionalFieldOf("knownPlayers", List.of())
                    .forGetter(d -> d.knownPlayers.stream().map(UUID::toString).collect(Collectors.toList()))
    ).apply(instance, GameStateSavedData::fromCodec));

    public GameStateSavedData() {
    }

    private static GameStateSavedData fromCodec(int stateOrdinal, int playTime, boolean marketGenerated,
                                                Map<String, java.util.stream.IntStream> dyesMap,
                                                List<FinishedPlayer> finishedPlayers,
                                                List<String> knownPlayersList) {
        GameStateSavedData data = new GameStateSavedData();
        data.state = GameState.values()[stateOrdinal];
        data.playTime = playTime;
        data.marketGenerated = marketGenerated;
        dyesMap.forEach((uuidStr, ordinals) -> {
            UUID uuid = UUID.fromString(uuidStr);
            Set<DyeColor> colors = EnumSet.noneOf(DyeColor.class);
            ordinals.forEach(ordinal -> {
                if (ordinal >= 0 && ordinal < DyeColor.values().length) {
                    colors.add(DyeColor.values()[ordinal]);
                }
            });
            data.deliveredDyes.put(uuid, colors);
        });
        data.finishedPlayers.addAll(finishedPlayers);
        knownPlayersList.forEach(uuidStr -> data.knownPlayers.add(UUID.fromString(uuidStr)));
        return data;
    }
}
