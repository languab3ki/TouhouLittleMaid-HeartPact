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
 * 亲吻请求数据包（客户端 -> 服务端）。
 * 客户端只负责提交当前想交互的女仆 UUID，
 * 服务端会再次核对所有权与拥抱状态，防止状态不同步。
 */
public class KissMaidPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<KissMaidPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "KissMaid".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, KissMaidPayload> STREAM_CODEC = StreamCodec.ofMember(KissMaidPayload::encode, KissMaidPayload::decode);

    @Nullable
    private final UUID maidUuid;

    public KissMaidPayload(@Nullable UUID maidUuid) {
        this.maidUuid = maidUuid;
    }

    @Nullable
    public UUID maidUuid() {
        return maidUuid;
    }

    public static void encode(KissMaidPayload msg, FriendlyByteBuf buf) {
        boolean hasMaid = msg.maidUuid != null;
        buf.writeBoolean(hasMaid);
        if (hasMaid) {
            buf.writeUUID(msg.maidUuid);
        }
    }

    public static KissMaidPayload decode(FriendlyByteBuf buf) {
        boolean hasMaid = buf.readBoolean();
        return new KissMaidPayload(hasMaid ? buf.readUUID() : null);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
