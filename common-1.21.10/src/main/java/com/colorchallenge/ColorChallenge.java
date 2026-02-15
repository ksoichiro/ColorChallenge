package com.colorchallenge;

import com.colorchallenge.entity.StaffEntityRenderer;
import com.colorchallenge.entity.StaffModel;
import com.colorchallenge.event.GameTickHandler;
import com.colorchallenge.event.PlayerSpawnHandler;
import com.colorchallenge.hud.GameHudOverlay;
import com.colorchallenge.loot.VillageLootModifier;
import com.colorchallenge.network.GameStateSyncPacket;
import com.colorchallenge.registry.ModBlocks;
import com.colorchallenge.registry.ModCreativeTab;
import com.colorchallenge.registry.ModEntityTypes;
import com.colorchallenge.registry.ModItems;
import com.colorchallenge.state.ClientGameState;
import com.colorchallenge.state.GameStateManager;
import com.colorchallenge.structure.DeliveryAreaGenerator;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorChallenge {
    public static final String MOD_ID = "colorchallenge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        // ModBlocks must be registered before ModItems because ModBlocks
        // adds block items to ModItems.ITEMS during class loading
        ModBlocks.register();
        ModItems.register();
        ModEntityTypes.register();
        ModCreativeTab.register();

        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            if (level.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
                GameStateManager.init(level);
            }
        });
        LifecycleEvent.SERVER_STARTED.register(server -> {
            ServerLevel overworld = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
            if (overworld != null) {
                generateMarketIfNeeded(overworld);
            }
        });
        LifecycleEvent.SERVER_STOPPING.register(server -> GameStateManager.clear());
        TickEvent.SERVER_PRE.register(GameTickHandler::onServerTick);
        PlayerEvent.PLAYER_RESPAWN.register(PlayerSpawnHandler::onPlayerRespawn);
        PlayerEvent.PLAYER_JOIN.register(PlayerSpawnHandler::onPlayerJoin);

        VillageLootModifier.register();
        InteractionEvent.INTERACT_ENTITY.register(ColorChallenge::onInteractEntity);

        LOGGER.info("Color Challenge initialized");
    }

    private static EventResult onInteractEntity(net.minecraft.world.entity.player.Player player,
                                                 net.minecraft.world.entity.Entity entity,
                                                 InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return EventResult.pass();
        if (!(entity instanceof WanderingTrader trader)) return EventResult.pass();
        if (entity.level().isClientSide()) return EventResult.pass();

        GameStateManager manager = GameStateManager.getInstance();
        if (manager == null || !manager.isTraderItemsUnlocked()) return EventResult.pass();

        // Use scoreboard tag to prevent adding trades multiple times
        if (trader.getTags().contains("colorchallenge_trades")) return EventResult.pass();
        trader.addTag("colorchallenge_trades");

        MerchantOffers offers = trader.getOffers();
        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 1), new ItemStack(Items.JUNGLE_SAPLING), 16, 1, 0.05f));
        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 1), new ItemStack(Items.CACTUS), 16, 1, 0.05f));
        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 1), new ItemStack(Items.SEA_PICKLE), 16, 1, 0.05f));

        return EventResult.pass();
    }

    private static void generateMarketIfNeeded(ServerLevel level) {
        GameStateManager manager = GameStateManager.getInstance();
        if (manager == null || manager.isMarketGenerated()) return;

        BlockPos spawnPos = level.getRespawnData().pos();
        if (DeliveryAreaGenerator.generate(level, spawnPos)) {
            manager.setMarketGenerated();
        }
    }

    public static void initClient() {
        GameStateSyncPacket.registerClientReceiver();
        EntityModelLayerRegistry.register(StaffModel.LAYER_LOCATION, StaffModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.MERCHANT, StaffEntityRenderer::new);
        ClientGuiEvent.RENDER_HUD.register(GameHudOverlay::render);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            ClientGameState.reset();
        });
    }
}
