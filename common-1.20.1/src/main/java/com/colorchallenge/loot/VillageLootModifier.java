package com.colorchallenge.loot;

import dev.architectury.event.events.common.LootEvent;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class VillageLootModifier {
    public static void register() {
        LootEvent.MODIFY_LOOT_TABLE.register((lootDataManager, id, context, builtin) -> {
            String path = id.getPath();
            if (!path.startsWith("chests/village/")) return;
            context.addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.JUNGLE_SAPLING)));
            context.addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.CACTUS)));
            context.addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.SEA_PICKLE)));
        });
    }
}
