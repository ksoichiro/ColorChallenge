package com.colorchallenge.neoforge.client;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.entity.StaffEntityRenderer;
import com.colorchallenge.entity.StaffModel;
import com.colorchallenge.hud.GameHudOverlay;
import com.colorchallenge.registry.ModEntityTypes;
import com.colorchallenge.state.ClientGameState;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class ColorChallengeNeoForgeClient {
    public static void init(IEventBus modBus) {
        // Entity rendering
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerEntityRenderer(ModEntityTypes.MERCHANT.get(), StaffEntityRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) ->
                event.registerLayerDefinition(StaffModel.LAYER_LOCATION, StaffModel::createBodyLayer));

        // HUD rendering
        modBus.addListener((RegisterGuiLayersEvent event) ->
                event.registerAboveAll(
                        Identifier.fromNamespaceAndPath(ColorChallenge.MOD_ID, "game_hud"),
                        GameHudOverlay::render));

        // Client disconnect
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) ->
                ClientGameState.reset());
    }
}
