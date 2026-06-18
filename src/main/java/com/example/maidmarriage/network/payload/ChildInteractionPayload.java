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
 * 小女仆互动会话切换包。
 *
 * <p>语义与成年女仆的互动入口一致：
 * - 当前没有小女仆互动会话时：尝试进入站立锁定；
 * - 当前已经有会话时：结束这份会话。
 */
public class ChildInteractionPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ChildInteractionPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "ChildInteraction".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChildInteractionPayload> STREAM_CODEC = StreamCodec.ofMember(ChildInteractionPayload::encode, ChildInteractionPayload::decode);

    @Nullable
    private final UUID maidUuid;

    public ChildInteractionPayload(@Nullable UUID maidUuid) {
        this.maidUuid = maidUuid;
    }

    @Nullable
    public UUID maidUuid() {
        return maidUuid;
    }

    public static void encode(ChildInteractionPayload msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.maidUuid != null);
        if (msg.maidUuid != null) {
            buf.writeUUID(msg.maidUuid);
        }
    }

    public static ChildInteractionPayload decode(FriendlyByteBuf buf) {
        boolean has = buf.readBoolean();
        return new ChildInteractionPayload(has ? buf.readUUID() : null);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
