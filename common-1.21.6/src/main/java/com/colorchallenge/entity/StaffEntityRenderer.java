package com.colorchallenge.entity;

import com.colorchallenge.ColorChallenge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class StaffEntityRenderer extends MobRenderer<StaffEntity, StaffRenderState, StaffModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ColorChallenge.MOD_ID, "textures/entity/staff.png");

    public StaffEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new StaffModel(context.bakeLayer(StaffModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public StaffRenderState createRenderState() {
        return new StaffRenderState();
    }

    @Override
    public void extractRenderState(StaffEntity entity, StaffRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.unhappyCounter = entity.getUnhappyCounter();
    }

    @Override
    public ResourceLocation getTextureLocation(StaffRenderState state) {
        return TEXTURE;
    }
}
