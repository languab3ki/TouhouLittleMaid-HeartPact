package com.example.maidmarriage.network.payload;

import org.jetbrains.annotations.NotNull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.example.maidmarriage.MaidMarriageMod;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public class SubmitRomanceRhythmPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SubmitRomanceRhythmPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "SubmitRomanceRhythm".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, SubmitRomanceRhythmPayload> STREAM_CODEC = StreamCodec.ofMember(SubmitRomanceRhythmPayload::encode, SubmitRomanceRhythmPayload::decode);

    private final UUID maidUuid;
    private final float rhythmScore;

    public SubmitRomanceRhythmPayload(UUID maidUuid, float rhythmScore) {
        this.maidUuid = maidUuid;
        this.rhythmScore = rhythmScore;
    }

    public UUID maidUuid() {
        return maidUuid;
    }

    public float rhythmScore() {
        return rhythmScore;
    }

    public static void encode(SubmitRomanceRhythmPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.maidUuid);
        buf.writeFloat(msg.rhythmScore);
    }

    public static SubmitRomanceRhythmPayload decode(FriendlyByteBuf buf) {
        return new SubmitRomanceRhythmPayload(buf.readUUID(), buf.readFloat());
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
