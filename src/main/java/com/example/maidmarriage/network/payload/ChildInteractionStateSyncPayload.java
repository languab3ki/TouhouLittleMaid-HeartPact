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
 * 小女仆互动会话的客户端同步包。
 *
 * <p>这里只同步“是否正在和某只小女仆保持站立锁定”。
 * 这层不带拥抱标记，因为小女仆互动页没有“hugging / not hugging”二级状态。
 */
public class ChildInteractionStateSyncPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ChildInteractionStateSyncPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "ChildInteractionStateSync".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChildInteractionStateSyncPayload> STREAM_CODEC = StreamCodec.ofMember(ChildInteractionStateSyncPayload::encode, ChildInteractionStateSyncPayload::decode);

    private final UUID playerUuid;
    @Nullable
    private final UUID maidUuid;

    public ChildInteractionStateSyncPayload(UUID playerUuid, @Nullable UUID maidUuid) {
        this.playerUuid = playerUuid;
        this.maidUuid = maidUuid;
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    @Nullable
    public UUID maidUuid() {
        return maidUuid;
    }

    public static void encode(ChildInteractionStateSyncPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUuid);
        buf.writeBoolean(msg.maidUuid != null);
        if (msg.maidUuid != null) {
            buf.writeUUID(msg.maidUuid);
        }
    }

    public static ChildInteractionStateSyncPayload decode(FriendlyByteBuf buf) {
        UUID playerUuid = buf.readUUID();
        boolean hasMaid = buf.readBoolean();
        UUID maidUuid = hasMaid ? buf.readUUID() : null;
        return new ChildInteractionStateSyncPayload(playerUuid, maidUuid);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
