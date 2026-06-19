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
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "textures/entity/starfall_meteor.png");
    private static final ResourceLocation RING = ResourceLocation.fromNamespaceAndPath(MaidMarriageMod.MOD_ID, "textures/entity/starfall_magic_outer.png");

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

        renderTrail(buffer, poseStack.last().pose());

        poseStack.scale(0.78F * pulse, 0.78F * pulse, 0.78F * pulse);
        drawMagicRing(buffer, poseStack.last().pose(), pulse);
        drawMeteorRock(buffer, poseStack.last().pose(), 1.0F, 0.88F, 0.62F, 1.0F);
        drawBillboard(buffer, poseStack.last().pose(), 0.0F, 0.0F, 0.0F, 1.0F, 0.72F, 0.22F, 0.42F);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void drawMagicRing(MultiBufferSource buffer, Matrix4f matrix, float pulse) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(RING));
        float radius = 1.25F + pulse * 0.16F;
        vertex(consumer, matrix, -radius, 0.0F, -radius, 1.0F, 0.76F, 0.22F, 0.44F, 0.0F, 1.0F);
        vertex(consumer, matrix, radius, 0.0F, -radius, 1.0F, 0.76F, 0.22F, 0.44F, 1.0F, 1.0F);
        vertex(consumer, matrix, radius, 0.0F, radius, 1.0F, 0.76F, 0.22F, 0.44F, 1.0F, 0.0F);
        vertex(consumer, matrix, -radius, 0.0F, radius, 1.0F, 0.76F, 0.22F, 0.44F, 0.0F, 0.0F);
    }

    private void drawMeteorRock(MultiBufferSource buffer, Matrix4f matrix, float red, float green, float blue, float alpha) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        float size = 0.42F;
        face(consumer, matrix, -size, -size, -size, size, -size, -size, size, size, -size, -size, size, -size, red * 0.70F, green * 0.62F, blue * 0.56F, alpha);
        face(consumer, matrix, size, -size, size, -size, -size, size, -size, size, size, size, size, size, red * 0.92F, green * 0.72F, blue * 0.38F, alpha);
        face(consumer, matrix, -size, size, -size, size, size, -size, size, size, size, -size, size, size, red, green * 0.86F, blue * 0.52F, alpha);
        face(consumer, matrix, -size, -size, size, size, -size, size, size, -size, -size, -size, -size, -size, red * 0.44F, green * 0.36F, blue * 0.34F, alpha);
        face(consumer, matrix, -size, -size, size, -size, -size, -size, -size, size, -size, -size, size, size, red * 0.56F, green * 0.42F, blue * 0.62F, alpha);
        face(consumer, matrix, size, -size, -size, size, -size, size, size, size, size, size, size, -size, red, green * 0.54F, blue * 0.24F, alpha);
    }

    private void face(VertexConsumer consumer, Matrix4f matrix,
                      float x0, float y0, float z0, float x1, float y1, float z1,
                      float x2, float y2, float z2, float x3, float y3, float z3,
                      float red, float green, float blue, float alpha) {
        vertex(consumer, matrix, x0, y0, z0, red, green, blue, alpha, 0.12F, 0.88F);
        vertex(consumer, matrix, x1, y1, z1, red, green, blue, alpha, 0.88F, 0.88F);
        vertex(consumer, matrix, x2, y2, z2, red, green, blue, alpha, 0.88F, 0.12F);
        vertex(consumer, matrix, x3, y3, z3, red, green, blue, alpha, 0.12F, 0.12F);
    }

    private void drawBillboard(MultiBufferSource buffer, Matrix4f matrix, float x, float y, float z,
                               float red, float green, float blue, float alpha) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        vertex(consumer, matrix, x - 0.58F, y - 0.58F, z, red, green, blue, alpha, 0.0F, 1.0F);
        vertex(consumer, matrix, x + 0.58F, y - 0.58F, z, red, green, blue, alpha, 1.0F, 1.0F);
        vertex(consumer, matrix, x + 0.58F, y + 0.58F, z, red, green, blue, alpha, 1.0F, 0.0F);
        vertex(consumer, matrix, x - 0.58F, y + 0.58F, z, red, green, blue, alpha, 0.0F, 0.0F);
    }

    private static void renderTrail(MultiBufferSource buffer, Matrix4f matrix) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        for (int i = 0; i < 10; i++) {
            float length = i * 0.36F;
            float width = 0.54F + i * 0.11F;
            float alpha = 0.58F * (1.0F - i / 10.0F);
            float y0 = -length - 0.20F;
            float y1 = -length - 0.74F;
            float red = i < 2 ? 1.0F : 0.85F;
            float green = i < 4 ? 0.72F : 0.36F;
            float blue = i < 4 ? 0.18F : 0.96F;
            vertex(consumer, matrix, -width * 0.35F, y0, -width, red, green, blue, alpha, 0.0F, 0.2F);
            vertex(consumer, matrix, width * 0.35F, y0, -width, red, green, blue, alpha, 1.0F, 0.2F);
            vertex(consumer, matrix, width * 0.12F, y1, width * 0.22F, red, green, blue, alpha * 0.42F, 1.0F, 1.0F);
            vertex(consumer, matrix, -width * 0.12F, y1, width * 0.22F, red, green, blue, alpha * 0.42F, 0.0F, 1.0F);
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                               float red, float green, float blue, float alpha, float u, float v) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(StarfallMeteorEntity entity) {
        return TEXTURE;
    }
}
