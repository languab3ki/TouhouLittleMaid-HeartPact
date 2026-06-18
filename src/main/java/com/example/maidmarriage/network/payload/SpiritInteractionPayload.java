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
 * 灵体交互动作包。
 *
 * <p>客户端只上报“对哪个灵体执行什么动作”，距离、归属和冷却全部交给服务端校验。
 */
public class SpiritInteractionPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpiritInteractionPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "SpiritInteraction".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpiritInteractionPayload> STREAM_CODEC = StreamCodec.ofMember(SpiritInteractionPayload::encode, SpiritInteractionPayload::decode);

    public static final String ACTION_SOOTHE = "soothe";
    public static final String ACTION_REMEMBER = "remember";
    public static final String ACTION_STAY = "stay";
    public static final String ACTION_FAREWELL = "farewell";
    public static final String ACTION_DAILY_SOOTHE = "daily_soothe";

    private final UUID spiritUuid;
    private final String actionId;

    public SpiritInteractionPayload(UUID spiritUuid, String actionId) {
        this.spiritUuid = spiritUuid;
        this.actionId = actionId == null ? "" : actionId;
    }

    public UUID spiritUuid() {
        return spiritUuid;
    }

    public String actionId() {
        return actionId;
    }

    public static void encode(SpiritInteractionPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.spiritUuid);
        buf.writeUtf(msg.actionId, 64);
    }

    public static SpiritInteractionPayload decode(FriendlyByteBuf buf) {
        return new SpiritInteractionPayload(buf.readUUID(), buf.readUtf(64));
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
