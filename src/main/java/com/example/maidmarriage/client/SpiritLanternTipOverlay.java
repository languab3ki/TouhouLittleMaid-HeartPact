package com.example.maidmarriage.client;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.entity.MaidSpiritEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 灵魂灯笼指向灵体时的轻量提示。
 *
 * <p>灵魂灯笼是原版物品，不能像本模组物品那样无条件注册到 TLM 的物品提示里；
 * 否则玩家拿着灯笼看普通女仆/普通场景也会出现误导提示。这里仅在准星命中灵体时绘制。
 */
@Mod.EventBusSubscriber(modid = MaidMarriageMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SpiritLanternTipOverlay {
    private SpiritLanternTipOverlay() {
    }

    @SubscribeEvent
    public static void onRender(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }
        boolean holdingLantern = minecraft.player.getMainHandItem().is(Items.SOUL_LANTERN);
        boolean holdingTorch = minecraft.player.getMainHandItem().is(Items.SOUL_TORCH);
        if (!holdingLantern && !holdingTorch) {
            return;
        }
        if (!(minecraft.hitResult instanceof EntityHitResult hitResult)
                || !(hitResult.getEntity() instanceof MaidSpiritEntity spirit)) {
            return;
        }

        Component text = Component.translatable(holdingTorch ? "overlay.maidmarriage.spirit_torch.ready" : resolveLanternTipKey(spirit), spirit.getDisplayName());
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int textWidth = minecraft.font.width(text);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2 + 34;
        event.getGuiGraphics().drawString(minecraft.font, text, x, y, 0xFFE8FFF6, true);
    }

    private static String resolveLanternTipKey(MaidSpiritEntity spirit) {
        if (spirit.isFarewell()) {
            return "overlay.maidmarriage.spirit_lantern.farewell";
        }
        if (!spirit.isLanternReady()) {
            return "overlay.maidmarriage.spirit_lantern.not_ready";
        }
        if (spirit.isLanternBound()) {
            return "overlay.maidmarriage.spirit_lantern.following";
        }
        return "overlay.maidmarriage.spirit_lantern.ready";
    }
}
