package com.example.maidmarriage.network.payload;

import org.jetbrains.annotations.NotNull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.example.maidmarriage.MaidMarriageMod;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public class StartRomanceRhythmPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StartRomanceRhythmPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "StartRomanceRhythm".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, StartRomanceRhythmPayload> STREAM_CODEC = StreamCodec.ofMember(StartRomanceRhythmPayload::encode, StartRomanceRhythmPayload::decode);

    private final UUID maidUuid;

    public StartRomanceRhythmPayload(UUID maidUuid) {
        this.maidUuid = maidUuid;
    }

    public UUID maidUuid() {
        return maidUuid;
    }

    public static void encode(StartRomanceRhythmPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.maidUuid);
    }

    public static StartRomanceRhythmPayload decode(FriendlyByteBuf buf) {
        return new StartRomanceRhythmPayload(buf.readUUID());
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}