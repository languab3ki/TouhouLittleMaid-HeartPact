package com.example.maidmarriage.client;

import com.example.maidmarriage.MaidMarriageMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MaidMarriageMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class HeartPactOverlaySuppressor {
    private static final String HEART_PACT_CLIENT_PACKAGE = "com.example.maidmarriage.client";
    private static final ResourceLocation TLM_MAID_TIPS = new ResourceLocation("touhou_little_maid", "tlm_maid_tips");

    private HeartPactOverlaySuppressor() {
    }

    @SubscribeEvent
    public static void hideExternalTipsWhenHeartPactScreenActive(RenderGuiOverlayEvent.Pre event) {
        if (!TLM_MAID_TIPS.equals(event.getOverlay().id())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (isHeartPactScreen(minecraft == null ? null : minecraft.screen)) {
            event.setCanceled(true);
        }
    }

    private static boolean isHeartPactScreen(Screen screen) {
        return screen != null && screen.getClass().getName().startsWith(HEART_PACT_CLIENT_PACKAGE);
    }
}
