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
 * 举高高请求包（客户端 -> 服务端）。
 * <p>
 * 按键触发时发送，允许携带一个可选女仆 UUID（准星命中时）。
 */
public class LiftMaidPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LiftMaidPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "LiftMaid".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, LiftMaidPayload> STREAM_CODEC = StreamCodec.ofMember(LiftMaidPayload::encode, LiftMaidPayload::decode);

    @Nullable
    private final UUID maidUuid;

    public LiftMaidPayload(@Nullable UUID maidUuid) {
        this.maidUuid = maidUuid;
    }

    @Nullable
    public UUID maidUuid() {
        return maidUuid;
    }

    public static void encode(LiftMaidPayload msg, FriendlyByteBuf buf) {
        boolean has = msg.maidUuid != null;
        buf.writeBoolean(has);
        if (has) {
            buf.writeUUID(msg.maidUuid);
        }
    }

    public static LiftMaidPayload decode(FriendlyByteBuf buf) {
        boolean has = buf.readBoolean();
        return new LiftMaidPayload(has ? buf.readUUID() : null);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
