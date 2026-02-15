package com.colorchallenge.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameStateSavedData extends SavedData {
    GameState state = GameState.NOT_STARTED;
    final Map<UUID, Set<DyeColor>> deliveredDyes = new HashMap<>();
    final List<FinishedPlayer> finishedPlayers = new ArrayList<>();
    final Set<UUID> knownPlayers = new HashSet<>();
    int playTime = 0;
    boolean marketGenerated = false;

    public GameStateSavedData() {
    }

    public static GameStateSavedData load(CompoundTag tag) {
        GameStateSavedData data = new GameStateSavedData();
        data.state = GameState.values()[tag.getInt("state")];
        data.playTime = tag.getInt("playTime");
        data.marketGenerated = tag.getBoolean("marketGenerated");
        if (tag.contains("deliveredDyes")) {
            CompoundTag dyesTag = tag.getCompound("deliveredDyes");
            for (String uuidStr : dyesTag.getAllKeys()) {
                UUID uuid = UUID.fromString(uuidStr);
                int[] ordinals = dyesTag.getIntArray(uuidStr);
                Set<DyeColor> colors = EnumSet.noneOf(DyeColor.class);
                for (int ordinal : ordinals) {
                    if (ordinal >= 0 && ordinal < DyeColor.values().length) {
                        colors.add(DyeColor.values()[ordinal]);
                    }
                }
                data.deliveredDyes.put(uuid, colors);
            }
        }
        if (tag.contains("knownPlayers")) {
            CompoundTag knownTag = tag.getCompound("knownPlayers");
            int knownCount = knownTag.getInt("count");
            for (int i = 0; i < knownCount; i++) {
                data.knownPlayers.add(UUID.fromString(knownTag.getString("uuid_" + i)));
            }
        }
        if (tag.contains("finishedPlayers")) {
            CompoundTag finishedTag = tag.getCompound("finishedPlayers");
            int count = finishedTag.getInt("count");
            for (int i = 0; i < count; i++) {
                CompoundTag entry = finishedTag.getCompound("entry_" + i);
                UUID uuid = UUID.fromString(entry.getString("uuid"));
                String name = entry.getString("name");
                int finishTime = entry.getInt("finishTime");
                data.finishedPlayers.add(new FinishedPlayer(uuid, name, finishTime));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("state", state.ordinal());
        tag.putInt("playTime", playTime);
        tag.putBoolean("marketGenerated", marketGenerated);
        CompoundTag dyesTag = new CompoundTag();
        deliveredDyes.forEach((uuid, colors) -> {
            int[] ordinals = colors.stream().mapToInt(DyeColor::ordinal).toArray();
            dyesTag.putIntArray(uuid.toString(), ordinals);
        });
        tag.put("deliveredDyes", dyesTag);
        CompoundTag finishedTag = new CompoundTag();
        finishedTag.putInt("count", finishedPlayers.size());
        for (int i = 0; i < finishedPlayers.size(); i++) {
            FinishedPlayer fp = finishedPlayers.get(i);
            CompoundTag entry = new CompoundTag();
            entry.putString("uuid", fp.playerId().toString());
            entry.putString("name", fp.playerName());
            entry.putInt("finishTime", fp.finishTimeTicks());
            finishedTag.put("entry_" + i, entry);
        }
        tag.put("finishedPlayers", finishedTag);
        CompoundTag knownTag = new CompoundTag();
        knownTag.putInt("count", knownPlayers.size());
        int ki = 0;
        for (UUID uuid : knownPlayers) {
            knownTag.putString("uuid_" + ki++, uuid.toString());
        }
        tag.put("knownPlayers", knownTag);
        return tag;
    }
}
