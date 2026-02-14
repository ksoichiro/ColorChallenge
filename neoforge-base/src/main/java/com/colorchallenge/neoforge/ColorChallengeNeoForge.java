package com.colorchallenge.neoforge;

import com.colorchallenge.ColorChallenge;
import com.colorchallenge.neoforge.client.ColorChallengeNeoForgeClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(ColorChallenge.MOD_ID)
public class ColorChallengeNeoForge {
    public ColorChallengeNeoForge() {
        ColorChallenge.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ColorChallengeNeoForgeClient.init();
        }
    }
}
