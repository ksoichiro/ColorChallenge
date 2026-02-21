package com.colorchallenge.fabric.client;

import com.colorchallenge.entity.StaffEntityRenderer;
import com.colorchallenge.entity.StaffModel;
import com.colorchallenge.hud.GameHudOverlay;
import com.colorchallenge.network.GameStateSyncPacket;
import com.colorchallenge.registry.ModEntityTypes;
import com.colorchallenge.state.ClientGameState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ColorChallengeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Client network receiver
        ClientPlayNetworking.registerGlobalReceiver(GameStateSyncPacket.TYPE,
                (payload, context) -> context.client().execute(() -> GameStateSyncPacket.applyOnClient(payload)));

        // Entity rendering
        EntityModelLayerRegistry.registerModelLayer(StaffModel.LAYER_LOCATION, StaffModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.MERCHANT.get(), StaffEntityRenderer::new);

        // HUD
        HudRenderCallback.EVENT.register(GameHudOverlay::render);

        // Client disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientGameState.reset());
    }
}
