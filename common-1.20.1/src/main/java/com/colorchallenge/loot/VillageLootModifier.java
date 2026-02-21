package com.colorchallenge.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.function.Consumer;

public class VillageLootModifier {
    public static void modifyLootTable(ResourceLocation id, Consumer<LootPool.Builder> poolAdder) {
        String path = id.getPath();
        if (!path.startsWith("chests/village/")) return;

        poolAdder.accept(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.JUNGLE_SAPLING)));
        poolAdder.accept(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.CACTUS)));
        poolAdder.accept(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.SEA_PICKLE)));
    }
}
