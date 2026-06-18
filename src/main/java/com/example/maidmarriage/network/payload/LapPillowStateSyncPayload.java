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

/**
 * 膝枕状态同步包（服务端 -> 客户端）。
 */
public class LapPillowStateSyncPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LapPillowStateSyncPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "LapPillowStateSync".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, LapPillowStateSyncPayload> STREAM_CODEC = StreamCodec.ofMember(LapPillowStateSyncPayload::encode, LapPillowStateSyncPayload::decode);

    private final UUID playerUuid;
    @Nullable
    private final UUID maidUuid;
    @Nullable
    private final UUID anchorUuid;
    private final boolean active;
    private final float sleepYaw;
    private final int petTicks;
    private final RecoveryStatus recoveryStatus;

    public LapPillowStateSyncPayload(UUID playerUuid, @Nullable UUID maidUuid,
                                     @Nullable UUID anchorUuid, boolean active,
                                     float sleepYaw, int petTicks,
                                     RecoveryStatus recoveryStatus) {
        this.playerUuid = playerUuid;
        this.maidUuid = maidUuid;
        this.anchorUuid = anchorUuid;
        this.active = active;
        this.sleepYaw = sleepYaw;
        this.petTicks = petTicks;
        this.recoveryStatus = recoveryStatus;
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    @Nullable
    public UUID maidUuid() {
        return maidUuid;
    }

    @Nullable
    public UUID anchorUuid() {
        return anchorUuid;
    }

    public boolean active() {
        return active;
    }

    public float sleepYaw() {
        return sleepYaw;
    }

    public int petTicks() {
        return petTicks;
    }

    public RecoveryStatus recoveryStatus() {
        return recoveryStatus;
    }

    public static void encode(LapPillowStateSyncPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUuid);
        buf.writeBoolean(msg.maidUuid != null);
        if (msg.maidUuid != null) {
            buf.writeUUID(msg.maidUuid);
        }
        buf.writeBoolean(msg.anchorUuid != null);
        if (msg.anchorUuid != null) {
            buf.writeUUID(msg.anchorUuid);
        }
        buf.writeBoolean(msg.active);
        buf.writeFloat(msg.sleepYaw);
        buf.writeVarInt(msg.petTicks);
        msg.recoveryStatus.encode(buf);
    }

    public static LapPillowStateSyncPayload decode(FriendlyByteBuf buf) {
        UUID playerUuid = buf.readUUID();
        UUID maidUuid = buf.readBoolean() ? buf.readUUID() : null;
        UUID anchorUuid = buf.readBoolean() ? buf.readUUID() : null;
        boolean active = buf.readBoolean();
        float sleepYaw = buf.readFloat();
        int petTicks = buf.readVarInt();
        RecoveryStatus recoveryStatus = RecoveryStatus.decode(buf);
        return new LapPillowStateSyncPayload(playerUuid, maidUuid, anchorUuid, active, sleepYaw, petTicks, recoveryStatus);
    }

    /**
     * 膝枕每日恢复状态。
     *
     * <p>单位说明：回血统一用 HP 计数，所有语言界面也直接显示 HP。
     */
    public record RecoveryStatus(int healUsedHp,
                                 int healLimitHp,
                                 int lastHealHp,
                                 int cleanseUsed,
                                 int cleanseLimit,
                                 int resistanceUsed,
                                 int resistanceLimit) {
        public static final RecoveryStatus EMPTY = new RecoveryStatus(0, 0, 0, 0, 0, 0, 0);

        private void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(healUsedHp);
            buf.writeVarInt(healLimitHp);
            buf.writeVarInt(lastHealHp);
            buf.writeVarInt(cleanseUsed);
            buf.writeVarInt(cleanseLimit);
            buf.writeVarInt(resistanceUsed);
            buf.writeVarInt(resistanceLimit);
        }

        private static RecoveryStatus decode(FriendlyByteBuf buf) {
            return new RecoveryStatus(
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt()
            );
        }
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
