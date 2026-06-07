package com.example.maidmarriage.client;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.entity.StarfallMagicCircleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class StarfallMagicCircleRenderer extends EntityRenderer<StarfallMagicCircleEntity> {
    private static final ResourceLocation CIRCLE = new ResourceLocation(MaidMarriageMod.MOD_ID, "textures/entity/starfall_bagua_full.png");
    private static final ResourceLocation BURST = new ResourceLocation(MaidMarriageMod.MOD_ID, "textures/entity/starfall_magic_burst.png");

    public StarfallMagicCircleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(StarfallMagicCircleEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float age = entity.age(partialTick);
        float appear = smooth01(age / 18.0F);
        float impact = smooth01((age - entity.impactTick()) / 10.0F);
        float fade = 1.0F - smooth01((age - 112.0F) / 18.0F);
        float alpha = appear * fade;
        if (alpha <= 0.01F) {
            return;
        }

        float radius = Mth.lerp(appear, 0.45F, 4.75F);
        float pulse = 1.0F + Mth.sin(age * 0.16F) * 0.018F;
        float impactPulse = 1.0F + impact * 0.08F;
        float burst = smooth01((age - entity.impactTick()) / 4.0F) * (1.0F - smooth01((age - entity.impactTick()) / 28.0F));

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.075D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 2.2F));

        Matrix4f matrix = poseStack.last().pose();
        drawTexturedQuad(matrix, buffer.getBuffer(RenderType.entityTranslucent(CIRCLE)),
                radius * pulse * impactPulse, new Color(1.0F, 1.0F, 1.0F), alpha);

        if (impact > 0.01F) {
            drawTexturedQuad(matrix, buffer.getBuffer(RenderType.entityTranslucent(CIRCLE)),
                    radius * (1.0F + impact * 0.14F), new Color(1.0F, 0.72F, 0.22F), alpha * impact * 0.32F);
        }

        if (burst > 0.01F) {
            drawTexturedQuad(matrix, buffer.getBuffer(RenderType.entityTranslucent(BURST)),
                    Mth.lerp(burst, 1.0F, 5.45F), new Color(1.0F, 0.86F, 0.22F), burst * 0.92F);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void drawTexturedQuad(Matrix4f matrix, VertexConsumer consumer, float radius, Color color, float alpha) {
        vertex(matrix, consumer, -radius, -radius, 0.0F, 1.0F, color, alpha);
        vertex(matrix, consumer, radius, -radius, 1.0F, 1.0F, color, alpha);
        vertex(matrix, consumer, radius, radius, 1.0F, 0.0F, color, alpha);
        vertex(matrix, consumer, -radius, radius, 0.0F, 0.0F, color, alpha);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float z, float u, float v, Color color, float alpha) {
        consumer.vertex(matrix, x, 0.0F, z)
                .color(color.r, color.g, color.b, Mth.clamp(alpha, 0.0F, 1.0F))
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static float smooth01(float value) {
        float t = Mth.clamp(value, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    @Override
    public ResourceLocation getTextureLocation(StarfallMagicCircleEntity entity) {
        return CIRCLE;
    }

    private record Color(float r, float g, float b) {
    }
}
