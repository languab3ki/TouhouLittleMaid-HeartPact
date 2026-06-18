package com.example.maidmarriage.client;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.client.interaction.GenericMaidInteractionScreen;
import com.example.maidmarriage.config.ModConfigs;
import com.example.maidmarriage.network.ModNetworking;
import com.example.maidmarriage.network.payload.UpdateMaidAddressingPayload;
import com.example.maidmarriage.network.payload.UpdatePlayerSettingsPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = MaidMarriageMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ClientPlayerSyncEvents {
    private ClientPlayerSyncEvents() {
    }

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        LapPillowClientState.clear();
        ModNetworking.sendUpdateMaidAddressing(new UpdateMaidAddressingPayload(
                ModConfigs.maidAddressing(),
                ModConfigs.childMaidAddressing()));
        ModNetworking.sendUpdatePlayerSettings(new UpdatePlayerSettingsPayload(
                ModConfigs.liftHeight(),
                ModConfigs.hugDistance(),
                ModConfigs.haremMode()));
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        HugClientState.clear();
        ChildInteractionClientState.clear();
        LapPillowClientState.clear();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
                net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        HugClientState.tick(minecraft);
        ChildInteractionClientState.tick(minecraft);
        HugActionScreen.tickCompactLookHotkey(minecraft);
        GenericMaidInteractionScreen.tickHiddenHotkeys(minecraft);
        HugClientState.ensureActionScreen(minecraft);
        ChildInteractionClientState.ensureActionScreen(minecraft);
    }
}
