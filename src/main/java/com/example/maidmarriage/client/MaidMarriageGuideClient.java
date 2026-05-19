package com.example.maidmarriage.client;

import net.minecraft.client.Minecraft;

public final class MaidMarriageGuideClient {
    private MaidMarriageGuideClient() {
    }

    public static void openGuide() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            minecraft.setScreen(new MaidMarriageGuideScreen(minecraft.screen));
        }
    }
}
