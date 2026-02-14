package com.colorchallenge.forge;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.forge.client.ColorChallengeForgeClient;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(ColorChallenge.MOD_ID)
public class ColorChallengeForge {
    public ColorChallengeForge() {
        EventBuses.registerModEventBus(ColorChallenge.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ColorChallenge.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ColorChallengeForgeClient.init();
        }
    }
}
