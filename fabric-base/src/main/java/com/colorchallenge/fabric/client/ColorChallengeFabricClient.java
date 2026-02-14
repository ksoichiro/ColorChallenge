package com.colorchallenge.fabric.client;

import com.colorchallenge.ColorChallenge;
import net.fabricmc.api.ClientModInitializer;

public class ColorChallengeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorChallenge.initClient();
    }
}
