package com.colorchallenge.neoforge;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.event.GameTickHandler;
import com.colorchallenge.event.PlayerSpawnHandler;
import com.colorchallenge.network.GameStateSyncPacket;
import com.colorchallenge.neoforge.client.ColorChallengeNeoForgeClient;
import com.colorchallenge.registry.ModBlocks;
import com.colorchallenge.registry.ModCreativeTab;
import com.colorchallenge.registry.ModEntityTypes;
import com.colorchallenge.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(ColorChallenge.MOD_ID)
public class ColorChallengeNeoForge {
    public ColorChallengeNeoForge(IEventBus modBus) {
        registerAll(modBus);
        registerNetworking(modBus);
        ColorChallenge.init();
        registerEvents();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ColorChallengeNeoForgeClient.init(modBus);
        }
    }

    private void registerAll(IEventBus modBus) {
        // Blocks (use factory methods from common for version-specific Properties)
        DeferredRegister<Block> blocks = DeferredRegister.create(Registries.BLOCK, ColorChallenge.MOD_ID);
        Supplier<Block> gameStartBlock = blocks.register("game_start_block", ModBlocks::createGameStartBlock);
        Supplier<Block> gameResetBlock = blocks.register("game_reset_block", ModBlocks::createGameResetBlock);
        blocks.register(modBus);
        ModBlocks.GAME_START_BLOCK = gameStartBlock;
        ModBlocks.GAME_RESET_BLOCK = gameResetBlock;

        // Items
        DeferredRegister<Item> items = DeferredRegister.create(Registries.ITEM, ColorChallenge.MOD_ID);
        Supplier<Item> gameStartBlockItem = items.register("game_start_block",
                () -> ModBlocks.createBlockItem(gameStartBlock.get()));
        Supplier<Item> gameResetBlockItem = items.register("game_reset_block",
                () -> ModBlocks.createBlockItem(gameResetBlock.get()));
        Supplier<Item> instructions = items.register("instructions", ModItems::createInstructions);
        items.register(modBus);
        ModBlocks.GAME_START_BLOCK_ITEM = gameStartBlockItem;
        ModBlocks.GAME_RESET_BLOCK_ITEM = gameResetBlockItem;
        ModItems.INSTRUCTIONS = instructions;

        // Entity types
        DeferredRegister<EntityType<?>> entityTypes = DeferredRegister.create(Registries.ENTITY_TYPE, ColorChallenge.MOD_ID);
        Supplier<EntityType<?>> staffEntityType = entityTypes.register("staff",
                ModEntityTypes::createStaffEntityType);
        entityTypes.register(modBus);
        ModEntityTypes.MERCHANT = () -> (EntityType) staffEntityType.get();

        // Entity attributes
        modBus.addListener((EntityAttributeCreationEvent event) ->
                event.put(ModEntityTypes.MERCHANT.get(), Mob.createMobAttributes().build()));

        // Creative tab
        DeferredRegister<CreativeModeTab> tabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ColorChallenge.MOD_ID);
        Supplier<CreativeModeTab> tab = tabs.register("colorchallenge_tab",
                () -> CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.colorchallenge"))
                        .icon(() -> new ItemStack(Items.YELLOW_DYE))
                        .displayItems((params, output) -> {
                            output.accept(ModBlocks.GAME_START_BLOCK_ITEM.get());
                            output.accept(ModBlocks.GAME_RESET_BLOCK_ITEM.get());
                            output.accept(ModItems.INSTRUCTIONS.get());
                        })
                        .build());
        tabs.register(modBus);
        ModCreativeTab.COLOR_CHALLENGE_TAB = tab;
    }

    private void registerNetworking(IEventBus modBus) {
        modBus.addListener((RegisterPayloadHandlersEvent event) -> {
            event.registrar(ColorChallenge.MOD_ID).versioned("1")
                    .playToClient(GameStateSyncPacket.TYPE, GameStateSyncPacket.STREAM_CODEC,
                            (payload, context) -> context.enqueueWork(() -> GameStateSyncPacket.applyOnClient(payload)));
        });
        GameStateSyncPacket.packetSender = (player, manager) -> {
            GameStateSyncPacket.Payload payload = GameStateSyncPacket.createPayload(player, manager);
            PacketDistributor.sendToPlayer(player, payload);
        };
    }

    private void registerEvents() {
        IEventBus bus = NeoForge.EVENT_BUS;

        // Lifecycle events
        bus.addListener((LevelEvent.Load event) -> {
            if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                ColorChallenge.onServerLevelLoad(serverLevel);
            }
        });
        bus.addListener((ServerStartedEvent event) -> ColorChallenge.onServerStarted(event.getServer()));
        bus.addListener((ServerStoppingEvent event) -> ColorChallenge.onServerStopping(event.getServer()));

        // Tick events
        bus.addListener((ServerTickEvent.Pre event) -> GameTickHandler.onServerTick(event.getServer()));

        // Player events
        bus.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                PlayerSpawnHandler.onPlayerRespawn(serverPlayer, event.isEndConquered(), null);
            }
        });
        bus.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                PlayerSpawnHandler.onPlayerJoin(serverPlayer);
            }
        });

        // Entity interaction
        bus.addListener((PlayerInteractEvent.EntityInteract event) ->
                ColorChallenge.onInteractEntity(event.getEntity(), event.getTarget(), event.getHand()));
    }
}
