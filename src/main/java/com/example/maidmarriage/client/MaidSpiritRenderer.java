package com.example.maidmarriage.client;

import com.example.maidmarriage.entity.MaidSpiritEntity;
import com.example.maidmarriage.entity.MaidChildEntity;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;

public class MaidSpiritRenderer extends EntityMaidRenderer {
    private static final String DEFAULT_ORIENTAL_MODEL = "touhou_little_maid:hakurei_reimu";
    private static final float SPIRIT_ALPHA = 0.45F;
    private static final float SPIRIT_READY_ALPHA = 0.80F;
    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final float INFANT_RENDER_SCALE = 0.56F;
    private static final float JUVENILE_RENDER_SCALE = 0.72F;
    private static final float CHILD_RENDER_SCALE = 0.86F;

    public MaidSpiritRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(Mob entity,
                       float entityYaw,
                       float partialTick,
                       PoseStack poseStack,
                       MultiBufferSource buffer,
                       int packedLight) {
        poseStack.pushPose();
        if (entity instanceof MaidSpiritEntity spirit) {
            float floatOffset = (float) Math.sin((entity.tickCount + partialTick + entity.getId()) * 0.08F) * 0.03F;
            poseStack.translate(0.0D, floatOffset, 0.0D);
            float scale = renderScale(spirit.getGrowthStage());
            poseStack.scale(scale, scale, scale);
        } else {
            float floatOffset = (float) Math.sin((entity.tickCount + partialTick + entity.getId()) * 0.08F) * 0.03F;
            poseStack.translate(0.0D, floatOffset, 0.0D);
        }
        String originalModelId = null;
        if (entity instanceof EntityMaid maid && maid.isYsmModel()) {
            originalModelId = maid.getModelId();
            maid.setModelId(DEFAULT_ORIENTAL_MODEL);
        }
        try {
            float alpha = entity instanceof MaidSpiritEntity spirit && spirit.getLonging() >= 100
                    ? SPIRIT_READY_ALPHA
                    : SPIRIT_ALPHA;
            super.render(entity, entityYaw, partialTick, poseStack, new SpiritBufferSource(buffer, alpha), FULL_BRIGHT);
        } finally {
            if (originalModelId != null && entity instanceof EntityMaid maid) {
                maid.setModelId(originalModelId);
            }
        }
        poseStack.popPose();
    }

    private static float renderScale(MaidChildEntity.GrowthStage stage) {
        return switch (stage) {
            case INFANT -> INFANT_RENDER_SCALE;
            case JUVENILE -> JUVENILE_RENDER_SCALE;
            case CHILD -> CHILD_RENDER_SCALE;
            case ADULT -> 1.0F;
        };
    }

    @Override
    protected RenderType getRenderType(Mob entity,
                                       boolean bodyVisible,
                                       boolean translucent,
                                       boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }

    private static class SpiritBufferSource implements MultiBufferSource {
        private final MultiBufferSource delegate;
        private final float alpha;

        private SpiritBufferSource(MultiBufferSource delegate, float alpha) {
            this.delegate = delegate;
            this.alpha = alpha;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new AlphaVertexConsumer(delegate.getBuffer(renderType), alpha);
        }
    }

    private static class AlphaVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float alphaMultiplier;

        private AlphaVertexConsumer(VertexConsumer delegate, float alphaMultiplier) {
            this.delegate = delegate;
            this.alphaMultiplier = alphaMultiplier;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(red, green, blue, Math.min(alpha, Math.round(alpha * alphaMultiplier)));
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            delegate.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            delegate.defaultColor(red, green, blue, Math.min(alpha, Math.round(alpha * alphaMultiplier)));
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }

}
