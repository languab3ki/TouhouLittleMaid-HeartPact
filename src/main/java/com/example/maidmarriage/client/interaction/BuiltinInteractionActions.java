package com.example.maidmarriage.client.interaction;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.client.GiftScreen;
import com.example.maidmarriage.entity.MaidSpiritEntity;
import com.example.maidmarriage.network.ModNetworking;
import com.example.maidmarriage.network.payload.SpiritInteractionPayload;
import net.minecraft.resources.ResourceLocation;

public final class BuiltinInteractionActions {
    public static final ResourceLocation SPIRIT_SOOTHE = ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "spirit_soothe");
    public static final ResourceLocation SPIRIT_REMEMBER = ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "spirit_remember");
    public static final ResourceLocation SPIRIT_STAY = ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "spirit_stay");
    public static final ResourceLocation SPIRIT_FAREWELL = ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "spirit_farewell");
    public static final ResourceLocation SPIRIT_DAILY_SOOTHE = ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "spirit_daily_soothe");
    public static final ResourceLocation OPEN_SPIRIT_OFFERING = ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "open_spirit_offering");
    public static final ResourceLocation CLOSE_INTERACTION = ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "close_interaction");

    private BuiltinInteractionActions() {
    }

    public static void register() {
        InteractionActionRegistry.register(SPIRIT_SOOTHE, context -> {
            sendSpiritAction(context, SpiritInteractionPayload.ACTION_SOOTHE);
            InteractionActionRegistry.debug(context.debugSink(), "spirit_soothe");
        });
        registerSpiritAction(SPIRIT_REMEMBER, SpiritInteractionPayload.ACTION_REMEMBER, "spirit_remember");
        registerSpiritAction(SPIRIT_STAY, SpiritInteractionPayload.ACTION_STAY, "spirit_stay");
        registerSpiritAction(SPIRIT_FAREWELL, SpiritInteractionPayload.ACTION_FAREWELL, "spirit_farewell");
        registerSpiritAction(SPIRIT_DAILY_SOOTHE, SpiritInteractionPayload.ACTION_DAILY_SOOTHE, "spirit_daily_soothe");
        InteractionActionRegistry.register(OPEN_SPIRIT_OFFERING, context -> {
            if (context.minecraft() != null && context.targetUuid() != null) {
                context.minecraft().setScreen(GiftScreen.openSpiritOffering(context.minecraft().screen, context.targetUuid()));
            }
            InteractionActionRegistry.debug(context.debugSink(), "open_spirit_offering");
        });
        InteractionActionRegistry.register(CLOSE_INTERACTION, context -> {
            if (context.closeScreen() != null) {
                context.closeScreen().run();
            }
        });
    }

    private static void registerSpiritAction(ResourceLocation id, String actionId, String debugName) {
        InteractionActionRegistry.register(id, context -> {
            sendSpiritAction(context, actionId);
            InteractionActionRegistry.debug(context.debugSink(), debugName);
        });
    }

    private static void sendSpiritAction(InteractionActionContext context, String actionId) {
        if (context == null || context.targetUuid() == null) {
            return;
        }
        if (!(context.target() instanceof MaidSpiritEntity)) {
            InteractionActionRegistry.debug(context.debugSink(), "Target is not a maid spirit");
            return;
        }
        ModNetworking.sendSpiritInteraction(new SpiritInteractionPayload(context.targetUuid(), actionId));
    }
}
