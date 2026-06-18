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

public class CarryChildMaidPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CarryChildMaidPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "CarryChildMaid".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, CarryChildMaidPayload> STREAM_CODEC = StreamCodec.ofMember(CarryChildMaidPayload::encode, CarryChildMaidPayload::decode);

    @Nullable
    private final UUID childUuid;

    public CarryChildMaidPayload(@Nullable UUID childUuid) {
        this.childUuid = childUuid;
    }

    @Nullable
    public UUID childUuid() {
        return childUuid;
    }

    public static void encode(CarryChildMaidPayload msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.childUuid != null);
        if (msg.childUuid != null) {
            buf.writeUUID(msg.childUuid);
        }
    }

    public static CarryChildMaidPayload decode(FriendlyByteBuf buf) {
        return new CarryChildMaidPayload(buf.readBoolean() ? buf.readUUID() : null);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
