package com.colorchallenge.registry;

import com.colorchallenge.ColorChallenge;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(ColorChallenge.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> COLOR_CHALLENGE_TAB = TABS.register(
            "colorchallenge_tab",
            () -> CreativeTabRegistry.create(builder ->
                    builder.title(Component.translatable("itemGroup.colorchallenge"))
                            .icon(() -> new ItemStack(Items.YELLOW_DYE))
                            .displayItems((params, output) -> {
                                output.accept(ModBlocks.GAME_START_BLOCK_ITEM.get());
                                output.accept(ModBlocks.GAME_RESET_BLOCK_ITEM.get());
                                output.accept(ModItems.INSTRUCTIONS.get());
                            })
            )
    );

    public static void register() {
        TABS.register();
    }
}
