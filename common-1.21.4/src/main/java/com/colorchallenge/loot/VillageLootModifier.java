package com.colorchallenge.loot;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class VillageLootModifier {
    public static void modifyLootTable(ResourceKey<LootTable> key, LootTable.Builder tableBuilder) {
        String path = key.location().getPath();
        if (!path.startsWith("chests/village/")) return;

        tableBuilder.withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.JUNGLE_SAPLING)));
        tableBuilder.withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.CACTUS)));
        tableBuilder.withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.SEA_PICKLE)));
    }
}
