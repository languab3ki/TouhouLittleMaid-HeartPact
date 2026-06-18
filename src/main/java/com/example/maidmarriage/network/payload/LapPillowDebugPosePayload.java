package com.example.maidmarriage.network.payload;

import org.jetbrains.annotations.NotNull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.example.maidmarriage.MaidMarriageMod;
import net.minecraft.network.FriendlyByteBuf;

/**
 * 膝枕调试姿态同步包（客户端 -> 服务端）。
 *
 * <p>这个包只服务于 F9 调试面板：玩家在客户端调整数值后，
 * 服务端用这些临时偏移重新锁定膝枕位置，方便现场校准。
 */
public class LapPillowDebugPosePayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LapPillowDebugPosePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "LapPillowDebugPose".replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT)));
    public static final StreamCodec<RegistryFriendlyByteBuf, LapPillowDebugPosePayload> STREAM_CODEC = StreamCodec.ofMember(LapPillowDebugPosePayload::encode, LapPillowDebugPosePayload::decode);

    private final double sideOffset;
    private final double heightOffset;
    private final double forwardOffset;
    private final float yawOffset;

    public LapPillowDebugPosePayload(double sideOffset, double heightOffset, double forwardOffset, float yawOffset) {
        this.sideOffset = sideOffset;
        this.heightOffset = heightOffset;
        this.forwardOffset = forwardOffset;
        this.yawOffset = yawOffset;
    }

    public double sideOffset() {
        return sideOffset;
    }

    public double heightOffset() {
        return heightOffset;
    }

    public double forwardOffset() {
        return forwardOffset;
    }

    public float yawOffset() {
        return yawOffset;
    }

    public static void encode(LapPillowDebugPosePayload msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.sideOffset);
        buf.writeDouble(msg.heightOffset);
        buf.writeDouble(msg.forwardOffset);
        buf.writeFloat(msg.yawOffset);
    }

    public static LapPillowDebugPosePayload decode(FriendlyByteBuf buf) {
        return new LapPillowDebugPosePayload(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat());
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
