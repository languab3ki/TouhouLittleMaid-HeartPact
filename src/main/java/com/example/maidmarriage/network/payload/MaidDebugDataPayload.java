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
 * 女仆调试面板的数据修改包。
 *
 * <p>这个包只用于测试面板：客户端提交目标女仆 UUID、好感度和心情值，
 * 服务端再校验权限并写入真实数据。
 */
public class MaidDebugDataPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MaidDebugDataPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "MaidDebugData".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, MaidDebugDataPayload> STREAM_CODEC = StreamCodec.ofMember(MaidDebugDataPayload::encode, MaidDebugDataPayload::decode);

    private final UUID maidUuid;
    private final int favorability;
    private final int mood;

    public MaidDebugDataPayload(UUID maidUuid, int favorability, int mood) {
        this.maidUuid = maidUuid;
        this.favorability = favorability;
        this.mood = mood;
    }

    public UUID maidUuid() {
        return maidUuid;
    }

    public int favorability() {
        return favorability;
    }

    public int mood() {
        return mood;
    }

    public static void encode(MaidDebugDataPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.maidUuid);
        buf.writeVarInt(msg.favorability);
        buf.writeVarInt(msg.mood);
    }

    public static MaidDebugDataPayload decode(FriendlyByteBuf buf) {
        UUID maidUuid = buf.readUUID();
        int favorability = buf.readVarInt();
        int mood = buf.readVarInt();
        return new MaidDebugDataPayload(maidUuid, favorability, mood);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
