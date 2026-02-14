package com.colorchallenge.registry;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.item.InstructionsItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ColorChallenge.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> INSTRUCTIONS = ITEMS.register("instructions",
            () -> new InstructionsItem(new Item.Properties().stacksTo(1)));

    public static void register() {
        ITEMS.register();
    }
}
