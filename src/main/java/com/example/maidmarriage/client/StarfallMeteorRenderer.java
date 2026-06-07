package com.example.maidmarriage.client;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.entity.StarfallMeteorEntity;
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

public final class StarfallMeteorRenderer extends EntityRenderer<StarfallMeteorEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(MaidMarriageMod.MOD_ID, "textures/entity/starfall_meteor.png");
    private static final ResourceLocation RING = new ResourceLocation(MaidMarriageMod.MOD_ID, "textures/entity/starfall_magic_outer.png");

    public StarfallMeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(StarfallMeteorEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float speed = (float) entity.getDeltaMovement().length();
        float spin = (entity.tickCount + partialTick) * (24.0F + speed * 6.0F);
        float pulse = 1.0F + Mth.sin((entity.tickCount + partialTick) * 0.32F) * 0.06F;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) (Mth.atan2(entity.getDeltaMovement().x, entity.getDeltaMovement().z) * Mth.RAD_TO_DEG)));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) (Mth.atan2(entity.getDeltaMovement().y, entity.getDeltaMovement().horizontalDistance()) * Mth.RAD_TO_DEG)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin));

        VertexConsumerWrapper.renderTrail(buffer, poseStack.last().pose());

        poseStack.scale(0.78F * pulse, 0.78F * pulse, 0.78F * pulse);
        drawMagicRing(buffer, poseStack.last().pose(), pulse);
        drawMeteorRock(buffer, poseStack.last().pose(), 1.0F, 0.88F, 0.62F, 1.0F);
        drawBillboard(buffer, poseStack.last().pose(), 0.0F, 0.0F, 0.0F, 1.0F, 0.72F, 0.22F, 0.42F);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void drawMagicRing(MultiBufferSource buffer, Matrix4f matrix, float pulse) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(RING));
        float radius = 1.25F + pulse * 0.16F;
        vc.vertex(matrix, -radius, 0.0F, -radius).color(1.0F, 0.76F, 0.22F, 0.44F).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
        vc.vertex(matrix, radius, 0.0F, -radius).color(1.0F, 0.76F, 0.22F, 0.44F).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
        vc.vertex(matrix, radius, 0.0F, radius).color(1.0F, 0.76F, 0.22F, 0.44F).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
        vc.vertex(matrix, -radius, 0.0F, radius).color(1.0F, 0.76F, 0.22F, 0.44F).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
    }

    private void drawMeteorRock(MultiBufferSource buffer, Matrix4f matrix, float r, float g, float b, float a) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        float s = 0.42F;
        face(vc, matrix, -s, -s, -s, s, -s, -s, s, s, -s, -s, s, -s, r * 0.70F, g * 0.62F, b * 0.56F, a);
        face(vc, matrix, s, -s, s, -s, -s, s, -s, s, s, s, s, s, r * 0.92F, g * 0.72F, b * 0.38F, a);
        face(vc, matrix, -s, s, -s, s, s, -s, s, s, s, -s, s, s, r, g * 0.86F, b * 0.52F, a);
        face(vc, matrix, -s, -s, s, s, -s, s, s, -s, -s, -s, -s, -s, r * 0.44F, g * 0.36F, b * 0.34F, a);
        face(vc, matrix, -s, -s, s, -s, -s, -s, -s, s, -s, -s, s, s, r * 0.56F, g * 0.42F, b * 0.62F, a);
        face(vc, matrix, s, -s, -s, s, -s, s, s, s, s, s, s, -s, r, g * 0.54F, b * 0.24F, a);
    }

    private void face(VertexConsumer vc, Matrix4f matrix,
                      float x0, float y0, float z0, float x1, float y1, float z1,
                      float x2, float y2, float z2, float x3, float y3, float z3,
                      float r, float g, float b, float a) {
        vc.vertex(matrix, x0, y0, z0).color(r, g, b, a).uv(0.12F, 0.88F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
        vc.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(0.88F, 0.88F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
        vc.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(0.88F, 0.12F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
        vc.vertex(matrix, x3, y3, z3).color(r, g, b, a).uv(0.12F, 0.12F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
    }

    private void drawBillboard(MultiBufferSource buffer, Matrix4f matrix, float x, float y, float z,
                               float r, float g, float b, float a) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        vc.vertex(matrix, x - 0.58F, y - 0.58F, z).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
        vc.vertex(matrix, x + 0.58F, y - 0.58F, z).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
        vc.vertex(matrix, x + 0.58F, y + 0.58F, z).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
        vc.vertex(matrix, x - 0.58F, y + 0.58F, z).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(StarfallMeteorEntity entity) {
        return TEXTURE;
    }

    private static final class VertexConsumerWrapper {
        private static void renderTrail(MultiBufferSource buffer, Matrix4f matrix) {
            VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
            for (int i = 0; i < 10; i++) {
                float length = i * 0.36F;
                float width = 0.54F + i * 0.11F;
                float alpha = 0.58F * (1.0F - i / 10.0F);
                float y0 = -length - 0.20F;
                float y1 = -length - 0.74F;
                float r = i < 2 ? 1.0F : 0.85F;
                float g = i < 4 ? 0.72F : 0.36F;
                float b = i < 4 ? 0.18F : 0.96F;
                vc.vertex(matrix, -width * 0.35F, y0, -width).color(r, g, b, alpha).uv(0, 0.2F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
                vc.vertex(matrix, width * 0.35F, y0, -width).color(r, g, b, alpha).uv(1, 0.2F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
                vc.vertex(matrix, width * 0.12F, y1, width * 0.22F).color(r, g, b, alpha * 0.42F).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
                vc.vertex(matrix, -width * 0.12F, y1, width * 0.22F).color(r, g, b, alpha * 0.42F).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
            }
        }
    }
}
