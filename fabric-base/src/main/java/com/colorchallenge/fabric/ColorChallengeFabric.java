package com.colorchallenge.fabric;

import com.colorchallenge.ColorChallenge;
import net.fabricmc.api.ModInitializer;

public class ColorChallengeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ColorChallenge.init();
    }
}
