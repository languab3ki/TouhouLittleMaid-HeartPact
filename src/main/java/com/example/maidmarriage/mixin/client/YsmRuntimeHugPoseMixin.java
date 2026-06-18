package com.example.maidmarriage.mixin.client;

import com.example.maidmarriage.client.YsmLiftHeightDebug;
import com.example.maidmarriage.client.YsmRuntimeHugPoseBridge;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * YSM 女仆渲染期的拥抱姿态覆写。
 *
 * <p>注入点选在 YSM 主模型真正绘制前。
 * 到这里时，YSM 当前帧动画已经计算完成，wrapper 内也已经准备好了运行时骨骼对象；
 * 我们只需要在 draw call 前最后覆写一次关键骨骼旋转，
 * 就能把拥抱姿态稳定压在当前帧结果之上。
 *
 * <p>这里现在只保留稳定版本逻辑：
 * 不再夹带调试日志、额外状态试探或姿态链路分叉，
 * 让 YSM 拥抱固定动作长期都走同一条运行时桥接入口。
 */
@Pseudo
@Mixin(targets = "com.elfmcys.yesstevemodel.OO0OOoo0ooooOoO0O0o00Ooo", remap = false)
public abstract class YsmRuntimeHugPoseMixin {
    @Inject(
            method = "oOo0OO0O0o000OO0O000oo0o(Lcom/elfmcys/yesstevemodel/O0OOoooOOoOo0O00O0oOoo0O;Lnet/minecraft/resources/ResourceLocation;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/elfmcys/yesstevemodel/OO0OOoo0ooooOoO0O0o00Ooo;oOo0OO0O0o000OO0O000oo0o(Lcom/elfmcys/yesstevemodel/o0ooO0ooO00oo0o00Oo00000;Lcom/elfmcys/yesstevemodel/OoO0oo0o0o0oOoo0oOOO0Ooo;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void maidmarriage$applyYsmRuntimeHugPose(@Coerce Object animatable,
                                                     ResourceLocation texture,
                                                     float entityYaw,
                                                     float partialTicks,
                                                     com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                     MultiBufferSource bufferSource,
                                                     int packedLight,
                                                     CallbackInfo ci) {
        /*
         * 先做 Y 轴视觉补偿，再覆写最终骨骼。
         *
         * 这里故意只补两种需要“模型路线专属高度”的情况：
         * 1. YSM 举高高；
         * 2. YSM 大女仆抱小女仆里的被抱小女仆。
         *
         * 这样以后调试面板里的高度数值改动，能直接在当前这条 YSM 渲染路线里生效。
         */
        if (YsmRuntimeHugPoseBridge.isLiftState(animatable)) {
            double configuredLiftHeight = YsmRuntimeHugPoseBridge.resolveLiftConfiguredHeight(animatable);
            poseStack.translate(0.0D, YsmLiftHeightDebug.resolveVisualHeight(configuredLiftHeight), 0.0D);
        } else if (YsmRuntimeHugPoseBridge.isCarriedChildState(animatable)) {
            poseStack.translate(
                    YsmRuntimeHugPoseBridge.resolveCarryChildVisualOffsetX(),
                    YsmRuntimeHugPoseBridge.resolveCarryChildVisualHeight(),
                    YsmRuntimeHugPoseBridge.resolveCarryChildVisualOffsetZ()
            );
        }

        YsmRuntimeHugPoseBridge.applyIfNeeded(animatable);
    }
}
