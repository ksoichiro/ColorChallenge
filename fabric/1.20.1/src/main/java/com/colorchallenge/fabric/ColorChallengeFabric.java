package com.colorchallenge.fabric;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.entity.StaffEntity;
import com.colorchallenge.event.GameTickHandler;
import com.colorchallenge.event.PlayerSpawnHandler;
import com.colorchallenge.loot.VillageLootModifier;
import com.colorchallenge.network.GameStateSyncPacket;
import com.colorchallenge.registry.ModBlocks;
import com.colorchallenge.registry.ModCreativeTab;
import com.colorchallenge.registry.ModEntityTypes;
import com.colorchallenge.registry.ModItems;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class ColorChallengeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        registerAll();
        registerNetworking();
        ColorChallenge.init();
        registerEvents();
    }

    private void registerAll() {
        // Blocks
        Block gameStartBlock = ModBlocks.createGameStartBlock();
        Block gameResetBlock = ModBlocks.createGameResetBlock();
        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(ColorChallenge.MOD_ID, "game_start_block"), gameStartBlock);
        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(ColorChallenge.MOD_ID, "game_reset_block"), gameResetBlock);
        ModBlocks.GAME_START_BLOCK = () -> gameStartBlock;
        ModBlocks.GAME_RESET_BLOCK = () -> gameResetBlock;

        // Block items
        Item gameStartBlockItem = ModBlocks.createBlockItem(gameStartBlock);
        Item gameResetBlockItem = ModBlocks.createBlockItem(gameResetBlock);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(ColorChallenge.MOD_ID, "game_start_block"), gameStartBlockItem);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(ColorChallenge.MOD_ID, "game_reset_block"), gameResetBlockItem);
        ModBlocks.GAME_START_BLOCK_ITEM = () -> gameStartBlockItem;
        ModBlocks.GAME_RESET_BLOCK_ITEM = () -> gameResetBlockItem;

        // Items
        Item instructions = ModItems.createInstructions();
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(ColorChallenge.MOD_ID, "instructions"), instructions);
        ModItems.INSTRUCTIONS = () -> instructions;

        // Entity types
        EntityType<StaffEntity> staffEntityType = ModEntityTypes.createStaffEntityType();
        Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation(ColorChallenge.MOD_ID, "staff"), staffEntityType);
        ModEntityTypes.MERCHANT = () -> staffEntityType;
        FabricDefaultAttributeRegistry.register(staffEntityType, Mob.createMobAttributes());

        // Creative tab
        CreativeModeTab tab = FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.colorchallenge"))
                .icon(() -> new ItemStack(Items.YELLOW_DYE))
                .displayItems((params, output) -> {
                    output.accept(ModBlocks.GAME_START_BLOCK_ITEM.get());
                    output.accept(ModBlocks.GAME_RESET_BLOCK_ITEM.get());
                    output.accept(ModItems.INSTRUCTIONS.get());
                })
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(ColorChallenge.MOD_ID, "colorchallenge_tab"), tab);
        ModCreativeTab.COLOR_CHALLENGE_TAB = () -> tab;
    }

    private void registerNetworking() {
        GameStateSyncPacket.packetSender = (player, manager) -> {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            GameStateSyncPacket.encode(buf, player, manager);
            ServerPlayNetworking.send(player, GameStateSyncPacket.ID, buf);
        };
    }

    private void registerEvents() {
        // Lifecycle events
        ServerWorldEvents.LOAD.register((server, world) -> ColorChallenge.onServerLevelLoad(world));
        ServerLifecycleEvents.SERVER_STARTED.register(ColorChallenge::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(ColorChallenge::onServerStopping);

        // Tick events
        ServerTickEvents.START_SERVER_TICK.register(GameTickHandler::onServerTick);

        // Player events
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                PlayerSpawnHandler.onPlayerRespawn(newPlayer, alive));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                PlayerSpawnHandler.onPlayerJoin(handler.getPlayer()));

        // Entity interaction
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ColorChallenge.onInteractEntity(player, entity, hand);
            return InteractionResult.PASS;
        });

        // Loot table modification
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) ->
                VillageLootModifier.modifyLootTable(id, tableBuilder::withPool));
    }
}
