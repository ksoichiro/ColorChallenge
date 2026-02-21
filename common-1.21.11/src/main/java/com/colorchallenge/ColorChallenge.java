package com.colorchallenge;

import com.colorchallenge.state.GameStateManager;
import com.colorchallenge.structure.DeliveryAreaGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorChallenge {
    public static final String MOD_ID = "colorchallenge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("Color Challenge initialized");
    }

    public static void onServerLevelLoad(ServerLevel level) {
        if (level.dimension() == Level.OVERWORLD) {
            GameStateManager.init(level);
        }
    }

    public static void onServerStarted(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld != null) {
            generateMarketIfNeeded(overworld);
        }
    }

    public static void onServerStopping(MinecraftServer server) {
        GameStateManager.clear();
    }

    /**
     * Returns true if the interaction was handled (trades were added).
     */
    public static boolean onInteractEntity(net.minecraft.world.entity.player.Player player,
                                            net.minecraft.world.entity.Entity entity,
                                            InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return false;
        if (!(entity instanceof WanderingTrader trader)) return false;
        if (entity.level().isClientSide()) return false;

        GameStateManager manager = GameStateManager.getInstance();
        if (manager == null || !manager.isTraderItemsUnlocked()) return false;

        // Use scoreboard tag to prevent adding trades multiple times
        if (trader.getTags().contains("colorchallenge_trades")) return false;
        trader.addTag("colorchallenge_trades");

        MerchantOffers offers = trader.getOffers();
        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 1), new ItemStack(Items.JUNGLE_SAPLING), 16, 1, 0.05f));
        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 1), new ItemStack(Items.CACTUS), 16, 1, 0.05f));
        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 1), new ItemStack(Items.SEA_PICKLE), 16, 1, 0.05f));

        return false;
    }

    private static void generateMarketIfNeeded(ServerLevel level) {
        GameStateManager manager = GameStateManager.getInstance();
        if (manager == null || manager.isMarketGenerated()) return;

        BlockPos spawnPos = level.getRespawnData().pos();
        if (DeliveryAreaGenerator.generate(level, spawnPos)) {
            manager.setMarketGenerated();
        }
    }
}
