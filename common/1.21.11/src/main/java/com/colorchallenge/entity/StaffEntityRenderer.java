package com.colorchallenge.entity;

import com.colorchallenge.ColorChallenge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class StaffEntityRenderer extends MobRenderer<StaffEntity, StaffRenderState, StaffModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(ColorChallenge.MOD_ID, "textures/entity/staff.png");

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
    public Identifier getTextureLocation(StaffRenderState state) {
        return TEXTURE;
    }
}
