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

/**
 * 膝枕动作请求包（客户端 -> 服务端）。
 */
public class LapPillowActionPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LapPillowActionPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "LapPillowAction".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, LapPillowActionPayload> STREAM_CODEC = StreamCodec.ofMember(LapPillowActionPayload::encode, LapPillowActionPayload::decode);

    public static final int ACTION_START = 0;
    public static final int ACTION_EXIT = 1;
    public static final int ACTION_PET_PLAYER_HEAD = 2;

    private final int action;
    @Nullable
    private final UUID maidUuid;

    public LapPillowActionPayload(int action, @Nullable UUID maidUuid) {
        this.action = action;
        this.maidUuid = maidUuid;
    }

    public int action() {
        return action;
    }

    @Nullable
    public UUID maidUuid() {
        return maidUuid;
    }

    public static void encode(LapPillowActionPayload msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action);
        boolean hasMaid = msg.maidUuid != null;
        buf.writeBoolean(hasMaid);
        if (hasMaid) {
            buf.writeUUID(msg.maidUuid);
        }
    }

    public static LapPillowActionPayload decode(FriendlyByteBuf buf) {
        int action = buf.readVarInt();
        UUID maidUuid = buf.readBoolean() ? buf.readUUID() : null;
        return new LapPillowActionPayload(action, maidUuid);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
