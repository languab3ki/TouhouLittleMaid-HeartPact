package com.example.maidmarriage.network.payload;

import org.jetbrains.annotations.NotNull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.example.maidmarriage.MaidMarriageMod;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;

public class HugStateSyncPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HugStateSyncPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "HugStateSync".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, HugStateSyncPayload> STREAM_CODEC = StreamCodec.ofMember(HugStateSyncPayload::encode, HugStateSyncPayload::decode);
    private static final ResourceLocation DEFAULT_SCENARIO_ID = ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "hug_menu_v2");

    private final UUID playerUuid;
    @Nullable
    private final UUID maidUuid;
    private final boolean hugging;
    private final boolean childNameRequired;
    private final boolean childLossGrief;
    private final ResourceLocation scenarioId;

    public HugStateSyncPayload(UUID playerUuid, @Nullable UUID maidUuid, boolean hugging) {
        this(playerUuid, maidUuid, hugging, false, false);
    }

    public HugStateSyncPayload(UUID playerUuid, @Nullable UUID maidUuid, boolean hugging, boolean childNameRequired) {
        this(playerUuid, maidUuid, hugging, childNameRequired, false);
    }

    public HugStateSyncPayload(UUID playerUuid,
                               @Nullable UUID maidUuid,
                               boolean hugging,
                               boolean childNameRequired,
                               boolean childLossGrief) {
        this(playerUuid, maidUuid, hugging, childNameRequired, childLossGrief, DEFAULT_SCENARIO_ID);
    }

    public HugStateSyncPayload(UUID playerUuid,
                               @Nullable UUID maidUuid,
                               boolean hugging,
                               boolean childNameRequired,
                               boolean childLossGrief,
                               ResourceLocation scenarioId) {
        this.playerUuid = playerUuid;
        this.maidUuid = maidUuid;
        this.hugging = maidUuid != null && hugging;
        this.childNameRequired = maidUuid != null && childNameRequired;
        this.childLossGrief = maidUuid != null && childLossGrief;
        this.scenarioId = maidUuid == null || scenarioId == null ? DEFAULT_SCENARIO_ID : scenarioId;
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    @Nullable
    public UUID maidUuid() {
        return maidUuid;
    }

    public boolean hugging() {
        return hugging;
    }

    public boolean childNameRequired() {
        return childNameRequired;
    }

    public boolean childLossGrief() {
        return childLossGrief;
    }

    public ResourceLocation scenarioId() {
        return scenarioId;
    }

    public static void encode(HugStateSyncPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUuid);
        buf.writeBoolean(msg.maidUuid != null);
        if (msg.maidUuid != null) {
            buf.writeUUID(msg.maidUuid);
        }
        buf.writeBoolean(msg.hugging);
        buf.writeBoolean(msg.childNameRequired);
        buf.writeBoolean(msg.childLossGrief);
        buf.writeResourceLocation(msg.scenarioId);
    }

    public static HugStateSyncPayload decode(FriendlyByteBuf buf) {
        UUID playerUuid = buf.readUUID();
        boolean hasMaid = buf.readBoolean();
        UUID maidUuid = hasMaid ? buf.readUUID() : null;
        boolean hugging = buf.readBoolean();
        boolean childNameRequired = buf.readBoolean();
        boolean childLossGrief = buf.readBoolean();
        ResourceLocation scenarioId = buf.readResourceLocation();
        return new HugStateSyncPayload(playerUuid, maidUuid, hugging, childNameRequired, childLossGrief, scenarioId);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
