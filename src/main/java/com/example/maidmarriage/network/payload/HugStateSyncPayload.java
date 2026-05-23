package com.example.maidmarriage.network.payload;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;

public class HugStateSyncPayload {
    private final UUID playerUuid;
    @Nullable
    private final UUID maidUuid;
    private final boolean hugging;
    private final boolean childNameRequired;
    private final boolean childLossGrief;

    public HugStateSyncPayload(UUID playerUuid, @Nullable UUID maidUuid, boolean hugging) {
        this(playerUuid, maidUuid, hugging, false, false);
    }

    public HugStateSyncPayload(UUID playerUuid, @Nullable UUID maidUuid, boolean hugging, boolean childNameRequired) {
        this(playerUuid, maidUuid, hugging, childNameRequired, false);
    }

    public HugStateSyncPayload(UUID playerUuid,
                               @Nullable UUID maidUuid,
                               boolean hugging,
                               boolean childNameRequired,
                               boolean childLossGrief) {
        this.playerUuid = playerUuid;
        this.maidUuid = maidUuid;
        this.hugging = maidUuid != null && hugging;
        this.childNameRequired = maidUuid != null && childNameRequired;
        this.childLossGrief = maidUuid != null && childLossGrief;
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    @Nullable
    public UUID maidUuid() {
        return maidUuid;
    }

    public boolean hugging() {
        return hugging;
    }

    public boolean childNameRequired() {
        return childNameRequired;
    }

    public boolean childLossGrief() {
        return childLossGrief;
    }

    public static void encode(HugStateSyncPayload msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUuid);
        buf.writeBoolean(msg.maidUuid != null);
        if (msg.maidUuid != null) {
            buf.writeUUID(msg.maidUuid);
        }
        buf.writeBoolean(msg.hugging);
        buf.writeBoolean(msg.childNameRequired);
        buf.writeBoolean(msg.childLossGrief);
    }

    public static HugStateSyncPayload decode(FriendlyByteBuf buf) {
        UUID playerUuid = buf.readUUID();
        boolean hasMaid = buf.readBoolean();
        UUID maidUuid = hasMaid ? buf.readUUID() : null;
        boolean hugging = buf.readBoolean();
        boolean childNameRequired = buf.readBoolean();
        boolean childLossGrief = buf.readBoolean();
        return new HugStateSyncPayload(playerUuid, maidUuid, hugging, childNameRequired, childLossGrief);
    }
}
