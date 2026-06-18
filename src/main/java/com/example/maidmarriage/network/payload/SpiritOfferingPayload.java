package com.example.maidmarriage.network.payload;

import org.jetbrains.annotations.NotNull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.example.maidmarriage.MaidMarriageMod;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public class SpiritOfferingPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpiritOfferingPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "SpiritOffering".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpiritOfferingPayload> STREAM_CODEC = StreamCodec.ofMember(SpiritOfferingPayload::encode, SpiritOfferingPayload::decode);

    private final UUID spiritUuid;
    private final int slotIndex;

    public SpiritOfferingPayload(UUID spiritUuid, int slotIndex) {
        this.spiritUuid = spiritUuid;
        this.slotIndex = slotIndex;
    }

    public UUID spiritUuid() {
        return spiritUuid;
    }

    public int slotIndex() {
        return slotIndex;
    }

    public static void encode(SpiritOfferingPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.spiritUuid);
        buf.writeVarInt(msg.slotIndex);
    }

    public static SpiritOfferingPayload decode(FriendlyByteBuf buf) {
        return new SpiritOfferingPayload(buf.readUUID(), buf.readVarInt());
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
