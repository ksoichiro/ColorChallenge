package com.colorchallenge.forge;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.entity.StaffEntity;
import com.colorchallenge.event.GameTickHandler;
import com.colorchallenge.event.PlayerSpawnHandler;
import com.colorchallenge.forge.client.ColorChallengeForgeClient;
import com.colorchallenge.loot.VillageLootModifier;
import com.colorchallenge.network.GameStateSyncPacket;
import com.colorchallenge.registry.ModBlocks;
import com.colorchallenge.registry.ModCreativeTab;
import com.colorchallenge.registry.ModEntityTypes;
import com.colorchallenge.registry.ModItems;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@Mod(ColorChallenge.MOD_ID)
public class ColorChallengeForge {
    private static SimpleChannel CHANNEL;

    public ColorChallengeForge() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();

        registerAll(modBus);
        registerNetworking();
        ColorChallenge.init();
        registerEvents();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ColorChallengeForgeClient.init(modBus);
        }
    }

    private void registerAll(net.minecraftforge.eventbus.api.IEventBus modBus) {
        // Blocks
        DeferredRegister<Block> blocks = DeferredRegister.create(ForgeRegistries.BLOCKS, ColorChallenge.MOD_ID);
        RegistryObject<Block> gameStartBlock = blocks.register("game_start_block", ModBlocks::createGameStartBlock);
        RegistryObject<Block> gameResetBlock = blocks.register("game_reset_block", ModBlocks::createGameResetBlock);
        blocks.register(modBus);
        ModBlocks.GAME_START_BLOCK = gameStartBlock;
        ModBlocks.GAME_RESET_BLOCK = gameResetBlock;

        // Items
        DeferredRegister<Item> items = DeferredRegister.create(ForgeRegistries.ITEMS, ColorChallenge.MOD_ID);
        RegistryObject<Item> gameStartBlockItem = items.register("game_start_block",
                () -> ModBlocks.createBlockItem(gameStartBlock.get()));
        RegistryObject<Item> gameResetBlockItem = items.register("game_reset_block",
                () -> ModBlocks.createBlockItem(gameResetBlock.get()));
        RegistryObject<Item> instructions = items.register("instructions", ModItems::createInstructions);
        items.register(modBus);
        ModBlocks.GAME_START_BLOCK_ITEM = gameStartBlockItem;
        ModBlocks.GAME_RESET_BLOCK_ITEM = gameResetBlockItem;
        ModItems.INSTRUCTIONS = instructions;

        // Entity types
        DeferredRegister<EntityType<?>> entityTypes = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ColorChallenge.MOD_ID);
        RegistryObject<EntityType<StaffEntity>> staffEntityType = entityTypes.register("staff",
                ModEntityTypes::createStaffEntityType);
        entityTypes.register(modBus);
        ModEntityTypes.MERCHANT = staffEntityType;

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

    private void registerNetworking() {
        CHANNEL = NetworkRegistry.ChannelBuilder
                .named(GameStateSyncPacket.ID)
                .networkProtocolVersion(() -> "1")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        CHANNEL.messageBuilder(GameStateMsg.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(GameStateMsg::encode)
                .decoder(GameStateMsg::decode)
                .consumerMainThread(GameStateMsg::handle)
                .add();

        GameStateSyncPacket.packetSender = (player, manager) -> {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            GameStateSyncPacket.encode(buf, player, manager);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            buf.release();
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new GameStateMsg(data));
        };
    }

    private void registerEvents() {
        var bus = MinecraftForge.EVENT_BUS;

        // Lifecycle events
        bus.addListener((LevelEvent.Load event) -> {
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                ColorChallenge.onServerLevelLoad(serverLevel);
            }
        });
        bus.addListener((ServerStartedEvent event) -> ColorChallenge.onServerStarted(event.getServer()));
        bus.addListener((ServerStoppingEvent event) -> ColorChallenge.onServerStopping(event.getServer()));

        // Tick events
        bus.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.START) {
                GameTickHandler.onServerTick(event.getServer());
            }
        });

        // Player events
        bus.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                PlayerSpawnHandler.onPlayerRespawn(serverPlayer, event.isEndConquered());
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

        // Loot table modification
        bus.addListener((LootTableLoadEvent event) ->
                VillageLootModifier.modifyLootTable(event.getName(),
                        pool -> event.getTable().addPool(pool.build())));
    }

    /** Thin wrapper message for Forge SimpleChannel */
    public record GameStateMsg(byte[] data) {
        void encode(FriendlyByteBuf buf) {
            buf.writeBytes(data);
        }

        static GameStateMsg decode(FriendlyByteBuf buf) {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return new GameStateMsg(data);
        }

        void handle(java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> ctxSupplier) {
            ctxSupplier.get().enqueueWork(() -> {
                FriendlyByteBuf readBuf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
                GameStateSyncPacket.applyOnClient(readBuf);
                readBuf.release();
            });
            ctxSupplier.get().setPacketHandled(true);
        }
    }
}
