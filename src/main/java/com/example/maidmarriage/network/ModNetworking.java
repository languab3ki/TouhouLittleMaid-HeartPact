package com.example.maidmarriage.network;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.compat.ChildInteractionManager;
import com.example.maidmarriage.compat.ChildNameManager;
import com.example.maidmarriage.compat.GiftManager;
import com.example.maidmarriage.compat.MaidCarryChildManager;
import com.example.maidmarriage.compat.MaidDebugDataManager;
import com.example.maidmarriage.compat.MaidDialogueInteractionManager;
import com.example.maidmarriage.compat.MaidLiftManager;
import com.example.maidmarriage.compat.MaidHugManager;
import com.example.maidmarriage.compat.MaidKissManager;
import com.example.maidmarriage.compat.LapPillowManager;
import com.example.maidmarriage.compat.MaidStoryInteractionManager;
import com.example.maidmarriage.compat.PetHeadManager;
import com.example.maidmarriage.compat.RomanceSleepManager;
import com.example.maidmarriage.compat.SpiritInteractionManager;
import com.example.maidmarriage.network.payload.CarryChildMaidPayload;
import com.example.maidmarriage.network.payload.CarryChildStateSyncPayload;
import com.example.maidmarriage.network.payload.ChildInteractionPayload;
import com.example.maidmarriage.network.payload.ChildInteractionStateSyncPayload;
import com.example.maidmarriage.network.payload.ChildNameSubmitPayload;
import com.example.maidmarriage.network.payload.DialogueChoiceResultPayload;
import com.example.maidmarriage.network.payload.FavorabilityEffectPayload;
import com.example.maidmarriage.network.payload.GiftResultPayload;
import com.example.maidmarriage.network.payload.GiftSubmitPayload;
import com.example.maidmarriage.network.payload.HugMaidPayload;
import com.example.maidmarriage.network.payload.HugStateSyncPayload;
import com.example.maidmarriage.network.payload.KissEffectPayload;
import com.example.maidmarriage.network.payload.KissMaidPayload;
import com.example.maidmarriage.network.payload.LapPillowActionPayload;
import com.example.maidmarriage.network.payload.LapPillowDebugPosePayload;
import com.example.maidmarriage.network.payload.LapPillowStateSyncPayload;
import com.example.maidmarriage.network.payload.LiftMaidPayload;
import com.example.maidmarriage.network.payload.LiftStateSyncPayload;
import com.example.maidmarriage.network.payload.MaidDebugDataPayload;
import com.example.maidmarriage.network.payload.PetHeadPayload;
import com.example.maidmarriage.network.payload.StartRomanceRhythmPayload;
import com.example.maidmarriage.network.payload.StoryProgressActionPayload;
import com.example.maidmarriage.network.payload.SpiritInteractionPayload;
import com.example.maidmarriage.network.payload.SpiritOfferingPayload;
import com.example.maidmarriage.network.payload.ToggleHugPosePayload;
import com.example.maidmarriage.network.payload.SubmitRomanceRhythmPayload;
import com.example.maidmarriage.network.payload.UpdateMaidAddressingPayload;
import com.example.maidmarriage.network.payload.UpdatePlayerSettingsPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private static final String PROTOCOL = "22";
    private ModNetworking() {
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        registrar.playToClient(StartRomanceRhythmPayload.TYPE, StartRomanceRhythmPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> handleStartRomanceRhythmClient(msg)));
        registrar.playToServer(SubmitRomanceRhythmPayload.TYPE, SubmitRomanceRhythmPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                RomanceSleepManager.onRhythmPanelResult(sender, msg.maidUuid(), msg.rhythmScore());
            }
        }));
        registrar.playToServer(UpdateMaidAddressingPayload.TYPE, UpdateMaidAddressingPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                RomanceSleepManager.updatePlayerMaidAddressing(sender, msg.addressing(), msg.childAddressing());
            }
        }));
        registrar.playToServer(UpdatePlayerSettingsPayload.TYPE, UpdatePlayerSettingsPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                MaidLiftManager.updatePlayerLiftSettings(sender, msg.liftHeight());
                            MaidHugManager.updatePlayerHugSettings(sender, msg.hugDistance());
                            RomanceSleepManager.updatePlayerHaremMode(sender, msg.haremMode());
            }
        }));
        registrar.playToServer(PetHeadPayload.TYPE, PetHeadPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                PetHeadManager.handlePetHeadRequest(sender, msg.maidUuid());
            }
        }));
        registrar.playToServer(LiftMaidPayload.TYPE, LiftMaidPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                MaidLiftManager.handleLiftToggle(sender, msg.maidUuid());
            }
        }));
        registrar.playToClient(LiftStateSyncPayload.TYPE, LiftStateSyncPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> handleLiftStateSyncClient(msg)));
        registrar.playToServer(HugMaidPayload.TYPE, HugMaidPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                MaidHugManager.handleInteractionToggle(sender, msg.maidUuid());
            }
        }));
        registrar.playToServer(ToggleHugPosePayload.TYPE, ToggleHugPosePayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                MaidHugManager.handleHugPoseToggle(sender, msg.maidUuid());
            }
        }));
        registrar.playToClient(HugStateSyncPayload.TYPE, HugStateSyncPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> handleHugStateSyncClient(msg)));
        registrar.playToServer(ChildInteractionPayload.TYPE, ChildInteractionPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                ChildInteractionManager.handleInteractionToggle(sender, msg.maidUuid());
            }
        }));
        registrar.playToClient(ChildInteractionStateSyncPayload.TYPE, ChildInteractionStateSyncPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> handleChildInteractionStateSyncClient(msg)));
        registrar.playToServer(DialogueChoiceResultPayload.TYPE, DialogueChoiceResultPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                MaidDialogueInteractionManager.handleDialogueChoiceResult(sender, msg.maidUuid(), msg.positiveFavor(), msg.neutralFavor(), msg.negativeFavor(), msg.positiveMoodDelta(), msg.neutralMoodDelta(), msg.negativeMoodDelta(), msg.resultKey());
            }
        }));
        registrar.playToServer(GiftSubmitPayload.TYPE, GiftSubmitPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                GiftManager.handleGiftSubmit(sender, msg.maidUuid(), msg.slotIndex());
            }
        }));
        registrar.playToClient(GiftResultPayload.TYPE, GiftResultPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> handleGiftResultClient(msg)));
        registrar.playToServer(KissMaidPayload.TYPE, KissMaidPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                MaidKissManager.handleKissRequest(sender, msg.maidUuid());
            }
        }));
        registrar.playToClient(KissEffectPayload.TYPE, KissEffectPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> handleKissEffectClient(msg)));
        registrar.playToClient(FavorabilityEffectPayload.TYPE, FavorabilityEffectPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> handleFavorabilityEffectClient(msg)));
        registrar.playToServer(CarryChildMaidPayload.TYPE, CarryChildMaidPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                MaidCarryChildManager.handleCarryToggle(sender, msg.childUuid());
            }
        }));
        registrar.playToClient(CarryChildStateSyncPayload.TYPE, CarryChildStateSyncPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> handleCarryChildStateSyncClient(msg)));
        registrar.playToServer(LapPillowActionPayload.TYPE, LapPillowActionPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                handleLapPillowAction(sender, msg);
            }
        }));
        registrar.playToClient(LapPillowStateSyncPayload.TYPE, LapPillowStateSyncPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> handleLapPillowStateSyncClient(msg)));
        registrar.playToServer(LapPillowDebugPosePayload.TYPE, LapPillowDebugPosePayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                LapPillowManager.handleDebugPose(sender, msg.sideOffset(), msg.heightOffset(), msg.forwardOffset(), msg.yawOffset());
            }
        }));
        registrar.playToServer(MaidDebugDataPayload.TYPE, MaidDebugDataPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                MaidDebugDataManager.handleDebugData(sender, msg.maidUuid(), msg.favorability(), msg.mood());
            }
        }));
        registrar.playToServer(StoryProgressActionPayload.TYPE, StoryProgressActionPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                MaidStoryInteractionManager.handleStoryAction(sender, msg.maidUuid(), msg.actionId());
            }
        }));
        registrar.playToServer(ChildNameSubmitPayload.TYPE, ChildNameSubmitPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                ChildNameManager.handleNameSubmit(sender, msg.motherUuid(), msg.name());
            }
        }));
        registrar.playToServer(SpiritInteractionPayload.TYPE, SpiritInteractionPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                SpiritInteractionManager.handleAction(sender, msg.spiritUuid(), msg.actionId());
            }
        }));
        registrar.playToServer(SpiritOfferingPayload.TYPE, SpiritOfferingPayload.STREAM_CODEC, (msg, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                SpiritInteractionManager.handleOffering(sender, msg.spiritUuid(), msg.slotIndex());
            }
        }));
    }
    public static void sendStart(ServerPlayer player, StartRomanceRhythmPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendSubmit(SubmitRomanceRhythmPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendUpdateMaidAddressing(UpdateMaidAddressingPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendUpdatePlayerSettings(UpdatePlayerSettingsPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendPetHead(PetHeadPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendLiftMaid(LiftMaidPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendLiftState(ServerPlayer player, LiftStateSyncPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendHugMaid(HugMaidPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendToggleHugPose(ToggleHugPosePayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendHugState(ServerPlayer player, HugStateSyncPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
    }

    public static void sendChildInteraction(ChildInteractionPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendChildInteractionState(ServerPlayer player, ChildInteractionStateSyncPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
    }

    public static void sendDialogueChoiceResult(DialogueChoiceResultPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendGiftSubmit(GiftSubmitPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendGiftResult(ServerPlayer player, GiftResultPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendKissMaid(KissMaidPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendKissEffect(ServerPlayer player, KissEffectPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendFavorabilityEffect(net.minecraft.world.entity.Entity entity, FavorabilityEffectPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    public static void sendCarryChildMaid(CarryChildMaidPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendLapPillowAction(LapPillowActionPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendLapPillowDebugPose(LapPillowDebugPosePayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendMaidDebugData(MaidDebugDataPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendStoryProgressAction(StoryProgressActionPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendChildNameSubmit(ChildNameSubmitPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendSpiritInteraction(SpiritInteractionPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    public static void sendSpiritOffering(SpiritOfferingPayload payload) {
        if (!canSendToServer()) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }

    private static boolean canSendToServer() {
        return FMLEnvironment.dist.isClient() && com.example.maidmarriage.client.ClientNetworkState.canSendToServer();
    }

    public static void sendCarryChildState(ServerPlayer player, CarryChildStateSyncPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
    }

    public static void sendLapPillowState(ServerPlayer player, LapPillowStateSyncPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
    }

    private static void handleLapPillowAction(ServerPlayer sender, LapPillowActionPayload msg) {
        switch (msg.action()) {
            case LapPillowActionPayload.ACTION_START -> LapPillowManager.handleStart(sender, msg.maidUuid());
            case LapPillowActionPayload.ACTION_EXIT -> LapPillowManager.handleExit(sender);
            case LapPillowActionPayload.ACTION_PET_PLAYER_HEAD -> LapPillowManager.handlePetPlayerHead(sender, msg.maidUuid());
            default -> {
            }
        }
    }

    private static void handleStartRomanceRhythmClient(StartRomanceRhythmPayload msg) {
        if (FMLEnvironment.dist.isClient()) {
            com.example.maidmarriage.client.RomanceRhythmHud.start(msg.maidUuid());
        }
    }

    private static void handleLiftStateSyncClient(LiftStateSyncPayload msg) {
        if (FMLEnvironment.dist.isClient()) {
            MaidLiftManager.handleClientLiftStateSync(msg.playerUuid(), msg.maidUuid(), msg.proxyUuid(), msg.liftHeight());
        }
    }

    private static void handleHugStateSyncClient(HugStateSyncPayload msg) {
        if (FMLEnvironment.dist.isClient()) {
            MaidHugManager.handleClientHugStateSync(msg.playerUuid(), msg.maidUuid(), msg.hugging());
            com.example.maidmarriage.client.HugClientState.handleSync(
                    msg.playerUuid(),
                    msg.maidUuid(),
                    msg.hugging(),
                    msg.childNameRequired(),
                    msg.childLossGrief()
            );
        }
    }

    private static void handleChildInteractionStateSyncClient(ChildInteractionStateSyncPayload msg) {
        if (FMLEnvironment.dist.isClient()) {
            ChildInteractionManager.handleClientInteractionStateSync(msg.playerUuid(), msg.maidUuid());
            com.example.maidmarriage.client.ChildInteractionClientState.handleSync(msg.playerUuid(), msg.maidUuid());
        }
    }

    private static void handleKissEffectClient(KissEffectPayload msg) {
        if (FMLEnvironment.dist.isClient()) {
            com.example.maidmarriage.client.HugCameraZoom.playKissZoom();
            com.example.maidmarriage.client.HugClientState.startPostKissShyTurn(
                    msg.maidUuid(),
                    msg.shyDelayTicks(),
                    msg.shyDurationTicks(),
                    msg.shyHeadYawDegrees(),
                    msg.shyHeadPitchDegrees(),
                    msg.shyDirectionSign()
            );
        }
    }

    private static void handleFavorabilityEffectClient(FavorabilityEffectPayload msg) {
        if (FMLEnvironment.dist.isClient()) {
            com.example.maidmarriage.client.FavorabilityPopupClient.show(msg.maidUuid(), msg.delta());
        }
    }

    private static void handleGiftResultClient(GiftResultPayload msg) {
        if (FMLEnvironment.dist.isClient()) {
            com.example.maidmarriage.client.HugActionScreen.handleGiftResult(msg);
        }
    }

    private static void handleCarryChildStateSyncClient(CarryChildStateSyncPayload msg) {
        if (FMLEnvironment.dist.isClient()) {
            MaidCarryChildManager.handleClientCarryStateSync(
                    msg.ownerUuid(),
                    msg.adultUuid(),
                    msg.childUuid(),
                    msg.proxyUuid()
            );
        }
    }

    private static void handleLapPillowStateSyncClient(LapPillowStateSyncPayload msg) {
        if (FMLEnvironment.dist.isClient()) {
            com.example.maidmarriage.client.LapPillowClientState.handleSync(
                    msg.playerUuid(),
                    msg.maidUuid(),
                    msg.active(),
                    msg.sleepYaw(),
                    msg.petTicks(),
                    msg.recoveryStatus()
            );
        }
    }
}
