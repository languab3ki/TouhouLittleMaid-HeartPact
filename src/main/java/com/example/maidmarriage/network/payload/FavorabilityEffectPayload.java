package com.example.maidmarriage.network.payload;

import org.jetbrains.annotations.NotNull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.example.maidmarriage.MaidMarriageMod;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

/**
 * 好感度变化的客户端表现包。
 *
 * <p>服务端完成真实好感结算后，把“目标女仆 + 实际变化量”同步给客户端，
 * 客户端再负责播放粒子和飘字。
 */
public class FavorabilityEffectPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FavorabilityEffectPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "FavorabilityEffect".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, FavorabilityEffectPayload> STREAM_CODEC = StreamCodec.ofMember(FavorabilityEffectPayload::encode, FavorabilityEffectPayload::decode);

    private final UUID maidUuid;
    private final int delta;

    public FavorabilityEffectPayload(UUID maidUuid, int delta) {
        this.maidUuid = maidUuid;
        this.delta = delta;
    }

    public UUID maidUuid() {
        return maidUuid;
    }

    public int delta() {
        return delta;
    }

    public static void encode(FavorabilityEffectPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.maidUuid);
        buf.writeVarInt(msg.delta);
    }

    public static FavorabilityEffectPayload decode(FriendlyByteBuf buf) {
        return new FavorabilityEffectPayload(buf.readUUID(), buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
