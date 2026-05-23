package com.example.maidmarriage.network.payload;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public class SpiritOfferingPayload {
    private final UUID spiritUuid;
    private final int slotIndex;

    public SpiritOfferingPayload(UUID spiritUuid, int slotIndex) {
        this.spiritUuid = spiritUuid;
        this.slotIndex = slotIndex;
    }

    public UUID spiritUuid() {
        return spiritUuid;
    }

    public int slotIndex() {
        return slotIndex;
    }

    public static void encode(SpiritOfferingPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.spiritUuid);
        buf.writeVarInt(msg.slotIndex);
    }

    public static SpiritOfferingPayload decode(FriendlyByteBuf buf) {
        return new SpiritOfferingPayload(buf.readUUID(), buf.readVarInt());
    }
}
