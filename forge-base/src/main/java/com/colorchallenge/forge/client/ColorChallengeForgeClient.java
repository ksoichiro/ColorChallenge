package com.colorchallenge.forge.client;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.entity.StaffEntityRenderer;
import com.colorchallenge.entity.StaffModel;
import com.colorchallenge.hud.GameHudOverlay;
import com.colorchallenge.registry.ModEntityTypes;
import com.colorchallenge.state.ClientGameState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class ColorChallengeForgeClient {
    public static void init(IEventBus modBus) {
        // Entity rendering
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerEntityRenderer(ModEntityTypes.MERCHANT.get(), StaffEntityRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) ->
                event.registerLayerDefinition(StaffModel.LAYER_LOCATION, StaffModel::createBodyLayer));

        // HUD rendering
        modBus.addListener((RegisterGuiOverlaysEvent event) ->
                event.registerAboveAll("game_hud",
                        (gui, graphics, partialTick, screenWidth, screenHeight) ->
                                GameHudOverlay.render(graphics, partialTick)));

        // Client disconnect
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) ->
                ClientGameState.reset());
    }
}
