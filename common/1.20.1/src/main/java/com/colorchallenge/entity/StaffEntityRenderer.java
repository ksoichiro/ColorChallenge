package com.colorchallenge.entity;

import com.colorchallenge.ColorChallenge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class StaffEntityRenderer extends MobRenderer<StaffEntity, StaffModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ColorChallenge.MOD_ID, "textures/entity/staff.png");

    public StaffEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new StaffModel(context.bakeLayer(StaffModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(StaffEntity entity) {
        return TEXTURE;
    }
}
