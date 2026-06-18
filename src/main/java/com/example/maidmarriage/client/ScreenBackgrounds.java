package com.example.maidmarriage.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 我们自己的游戏内界面背景工具。
 *
 * <p>MC 1.21 的 {@code Screen#renderBackground} 会在世界中打开界面时叠加原版菜单背景，
 * 表现为一层半透明模糊遮罩。心契的指南、送礼、配置等面板本身已经画了可读性底色，
 * 继续调用原版背景会让画面像被糊住，所以统一改成只画一层很淡的遮罩。
 */
public final class ScreenBackgrounds {
    private static final int IN_WORLD_DIM = 0x22000000;
    private static final int TITLE_DIM = 0xAA000000;

    private ScreenBackgrounds() {
    }

    public static void renderInWorld(GuiGraphics graphics, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.fill(0, 0, width, height, minecraft.level == null ? TITLE_DIM : IN_WORLD_DIM);
    }

    /**
     * 1.21 的 Screen#renderWithTooltip 会先调用 renderBackground，再进入各界面自己的 render。
     * 如果只在 render 里画半透明底色，原版模糊已经生效了；这些空覆盖给自定义面板统一挡掉原版高斯模糊。
     */
    public static void suppressVanillaBackground() {
    }
}
