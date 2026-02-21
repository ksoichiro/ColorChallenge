package com.colorchallenge.registry;

import com.colorchallenge.item.InstructionsItem;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ModItems {
    public static Supplier<Item> INSTRUCTIONS;

    public static Item createInstructions() {
        return new InstructionsItem(new Item.Properties().stacksTo(1));
    }
}
