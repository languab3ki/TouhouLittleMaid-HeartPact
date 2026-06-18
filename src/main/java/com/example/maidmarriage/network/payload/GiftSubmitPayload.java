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
 * 送礼提交包。
 *
 * <p>客户端只提交“目标女仆 + 背包槽位”，
 * 服务端再按当前真实库存和礼物表完成最终结算。
 */
public class GiftSubmitPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GiftSubmitPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "GiftSubmit".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, GiftSubmitPayload> STREAM_CODEC = StreamCodec.ofMember(GiftSubmitPayload::encode, GiftSubmitPayload::decode);

    private final UUID maidUuid;
    private final int slotIndex;

    public GiftSubmitPayload(UUID maidUuid, int slotIndex) {
        this.maidUuid = maidUuid;
        this.slotIndex = slotIndex;
    }

    public UUID maidUuid() {
        return maidUuid;
    }

    public int slotIndex() {
        return slotIndex;
    }

    public static void encode(GiftSubmitPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.maidUuid);
        buf.writeVarInt(msg.slotIndex);
    }

    public static GiftSubmitPayload decode(FriendlyByteBuf buf) {
        return new GiftSubmitPayload(buf.readUUID(), buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
