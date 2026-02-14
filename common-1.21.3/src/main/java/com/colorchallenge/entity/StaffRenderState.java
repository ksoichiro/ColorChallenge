package com.colorchallenge.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

@Environment(EnvType.CLIENT)
public class StaffRenderState extends LivingEntityRenderState {
    public int unhappyCounter;
}
