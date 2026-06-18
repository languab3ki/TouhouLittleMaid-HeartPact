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

public class HugMaidPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HugMaidPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "HugMaid".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, HugMaidPayload> STREAM_CODEC = StreamCodec.ofMember(HugMaidPayload::encode, HugMaidPayload::decode);

    @Nullable
    private final UUID maidUuid;

    public HugMaidPayload(@Nullable UUID maidUuid) {
        this.maidUuid = maidUuid;
    }

    @Nullable
    public UUID maidUuid() {
        return maidUuid;
    }

    public static void encode(HugMaidPayload msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.maidUuid != null);
        if (msg.maidUuid != null) {
            buf.writeUUID(msg.maidUuid);
        }
    }

    public static HugMaidPayload decode(FriendlyByteBuf buf) {
        boolean has = buf.readBoolean();
        return new HugMaidPayload(has ? buf.readUUID() : null);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
