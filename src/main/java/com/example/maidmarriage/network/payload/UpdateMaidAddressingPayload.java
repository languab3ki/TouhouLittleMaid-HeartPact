package com.example.maidmarriage.network.payload;

import org.jetbrains.annotations.NotNull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.example.maidmarriage.MaidMarriageMod;
import net.minecraft.network.FriendlyByteBuf;

public class UpdateMaidAddressingPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateMaidAddressingPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "UpdateMaidAddressing".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateMaidAddressingPayload> STREAM_CODEC = StreamCodec.ofMember(UpdateMaidAddressingPayload::encode, UpdateMaidAddressingPayload::decode);

    private final String addressing;
    private final String childAddressing;

    public UpdateMaidAddressingPayload(String addressing) {
        this(addressing, "");
    }

    public UpdateMaidAddressingPayload(String addressing, String childAddressing) {
        this.addressing = addressing == null ? "" : addressing;
        this.childAddressing = childAddressing == null ? "" : childAddressing;
    }

    public String addressing() {
        return addressing;
    }

    public String childAddressing() {
        return childAddressing;
    }

    public static void encode(UpdateMaidAddressingPayload msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.addressing, 64);
        buf.writeUtf(msg.childAddressing, 64);
    }

    public static UpdateMaidAddressingPayload decode(FriendlyByteBuf buf) {
        return new UpdateMaidAddressingPayload(buf.readUtf(64), buf.readUtf(64));
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
