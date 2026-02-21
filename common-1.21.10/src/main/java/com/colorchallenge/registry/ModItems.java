package com.colorchallenge.registry;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.item.InstructionsItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ModItems {
    public static Supplier<Item> INSTRUCTIONS;

    public static Item createInstructions() {
        return new InstructionsItem(new Item.Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath(ColorChallenge.MOD_ID, "instructions"))));
    }
}
