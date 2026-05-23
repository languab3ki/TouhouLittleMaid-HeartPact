package com.example.maidmarriage.network.payload;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

/**
 * 灵体交互动作包。
 *
 * <p>客户端只上报“对哪个灵体执行什么动作”，距离、归属和冷却全部交给服务端校验。
 */
public class SpiritInteractionPayload {
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
}
