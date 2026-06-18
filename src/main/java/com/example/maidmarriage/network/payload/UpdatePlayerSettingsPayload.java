package com.example.maidmarriage.network.payload;

import org.jetbrains.annotations.NotNull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.example.maidmarriage.MaidMarriageMod;
import net.minecraft.network.FriendlyByteBuf;

public class UpdatePlayerSettingsPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdatePlayerSettingsPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "UpdatePlayerSettings".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePlayerSettingsPayload> STREAM_CODEC = StreamCodec.ofMember(UpdatePlayerSettingsPayload::encode, UpdatePlayerSettingsPayload::decode);

    private final double liftHeight;
    private final double hugDistance;
    private final boolean haremMode;

    public UpdatePlayerSettingsPayload(double liftHeight, double hugDistance, boolean haremMode) {
        this.liftHeight = liftHeight;
        this.hugDistance = hugDistance;
        this.haremMode = haremMode;
    }

    public double liftHeight() {
        return liftHeight;
    }

    public double hugDistance() {
        return hugDistance;
    }

    public boolean haremMode() {
        return haremMode;
    }

    public static void encode(UpdatePlayerSettingsPayload msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.liftHeight);
        buf.writeDouble(msg.hugDistance);
        buf.writeBoolean(msg.haremMode);
    }

    public static UpdatePlayerSettingsPayload decode(FriendlyByteBuf buf) {
        return new UpdatePlayerSettingsPayload(
                buf.readDouble(),
                buf.readDouble(),
                buf.readBoolean());
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
