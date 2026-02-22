package com.colorchallenge.fabric.client;

import com.colorchallenge.entity.StaffEntityRenderer;
import com.colorchallenge.entity.StaffModel;
import com.colorchallenge.hud.GameHudOverlay;
import com.colorchallenge.network.GameStateSyncPacket;
import com.colorchallenge.registry.ModEntityTypes;
import com.colorchallenge.state.ClientGameState;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.network.FriendlyByteBuf;

public class ColorChallengeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Network packet receiver
        ClientPlayNetworking.registerGlobalReceiver(GameStateSyncPacket.ID,
                (client, handler, buf, responseSender) -> {
                    FriendlyByteBuf copy = new FriendlyByteBuf(Unpooled.buffer());
                    copy.writeBytes(buf);
                    client.execute(() -> {
                        GameStateSyncPacket.applyOnClient(copy);
                        copy.release();
                    });
                });

        // Entity rendering
        EntityModelLayerRegistry.registerModelLayer(StaffModel.LAYER_LOCATION, StaffModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.MERCHANT.get(), StaffEntityRenderer::new);

        // HUD rendering
        HudRenderCallback.EVENT.register(GameHudOverlay::render);

        // Client disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientGameState.reset());
    }
}
