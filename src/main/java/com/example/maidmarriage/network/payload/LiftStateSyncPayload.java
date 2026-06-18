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
 * 举高高状态同步包（服务端 -> 客户端）。
 */
public class LiftStateSyncPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LiftStateSyncPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "LiftStateSync".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, LiftStateSyncPayload> STREAM_CODEC = StreamCodec.ofMember(LiftStateSyncPayload::encode, LiftStateSyncPayload::decode);

    private final UUID playerUuid;
    @Nullable
    private final UUID maidUuid;
    @Nullable
    private final UUID proxyUuid;
    private final double liftHeight;

    public LiftStateSyncPayload(UUID playerUuid, @Nullable UUID maidUuid, @Nullable UUID proxyUuid, double liftHeight) {
        this.playerUuid = playerUuid;
        this.maidUuid = maidUuid;
        this.proxyUuid = proxyUuid;
        this.liftHeight = liftHeight;
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    @Nullable
    public UUID maidUuid() {
        return maidUuid;
    }

    @Nullable
    public UUID proxyUuid() {
        return proxyUuid;
    }

    public double liftHeight() {
        return liftHeight;
    }

    public static void encode(LiftStateSyncPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUuid);
        boolean hasMaid = msg.maidUuid != null;
        buf.writeBoolean(hasMaid);
        if (hasMaid) {
            buf.writeUUID(msg.maidUuid);
        }
        boolean hasProxy = msg.proxyUuid != null;
        buf.writeBoolean(hasProxy);
        if (hasProxy) {
            buf.writeUUID(msg.proxyUuid);
        }
        buf.writeDouble(msg.liftHeight);
    }

    public static LiftStateSyncPayload decode(FriendlyByteBuf buf) {
        UUID playerUuid = buf.readUUID();
        boolean hasMaid = buf.readBoolean();
        UUID maidUuid = hasMaid ? buf.readUUID() : null;
        boolean hasProxy = buf.readBoolean();
        UUID proxyUuid = hasProxy ? buf.readUUID() : null;
        double liftHeight = buf.readDouble();
        return new LiftStateSyncPayload(playerUuid, maidUuid, proxyUuid, liftHeight);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
