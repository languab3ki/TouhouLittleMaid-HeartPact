package com.example.maidmarriage.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

/**
 * 游戏内玩家指南。
 *
 * <p>这不是开发调试页，而是正式版给玩家看的流程说明：
 * 从按键配置开始，按“培养关系 -> 表白结婚 -> 怀孕与小女仆 -> 常见问题”的顺序整理。
 */
public class MaidMarriageGuideScreen extends Screen {
    private static final int PANEL_WIDTH = 372;
    private static final int CONTENT_PADDING = 18;
    private static final List<GuideSection> SECTIONS = List.of(
            section("guide.maidmarriage.section.0.title",
                    List.of(
                            p("guide.maidmarriage.section.0.p.0"),
                            key("interaction", "guide.maidmarriage.section.0.p.1"),
                            key("pet_head", "guide.maidmarriage.section.0.p.2"),
                            key("rhythm_hit", "guide.maidmarriage.section.0.p.3"),
                            key("restore_ui", "guide.maidmarriage.section.0.p.4"),
                            key("lap_exit", "guide.maidmarriage.section.0.p.5"),
                            p("guide.maidmarriage.section.0.p.6"),
                            p("guide.maidmarriage.section.0.p.7"),
                            p("guide.maidmarriage.section.0.p.8"))),
            section("guide.maidmarriage.section.1.title",
                    List.of(
                            p("guide.maidmarriage.section.1.p.0"),
                            p("guide.maidmarriage.section.1.p.1"),
                            p("guide.maidmarriage.section.1.p.2"),
                            p("guide.maidmarriage.section.1.p.3"),
                            p("guide.maidmarriage.section.1.p.4"),
                            p("guide.maidmarriage.section.1.p.5"),
                            p("guide.maidmarriage.section.1.p.6"),
                            p("guide.maidmarriage.section.1.p.7"),
                            p("guide.maidmarriage.section.1.p.8"),
                            p("guide.maidmarriage.section.1.p.9"),
                            p("guide.maidmarriage.section.1.p.10"),
                            p("guide.maidmarriage.section.1.p.11"))),
            section("guide.maidmarriage.section.2.title",
                    List.of(
                            p("guide.maidmarriage.section.2.p.0"),
                            p("guide.maidmarriage.section.2.p.1"),
                            p("guide.maidmarriage.section.2.p.2"),
                            p("guide.maidmarriage.section.2.p.3"),
                            p("guide.maidmarriage.section.2.p.4"),
                            p("guide.maidmarriage.section.2.p.5"))),
            section("guide.maidmarriage.section.3.title",
                    List.of(
                            p("guide.maidmarriage.section.3.p.0"),
                            p("guide.maidmarriage.section.3.p.1"),
                            p("guide.maidmarriage.section.3.p.2"),
                            p("guide.maidmarriage.section.3.p.3"),
                            p("guide.maidmarriage.section.3.p.4"),
                            p("guide.maidmarriage.section.3.p.5"),
                            p("guide.maidmarriage.section.3.p.6"))),
            section("guide.maidmarriage.section.4.title",
                    List.of(
                            p("guide.maidmarriage.section.4.p.0"),
                            p("guide.maidmarriage.section.4.p.1"),
                            p("guide.maidmarriage.section.4.p.2"),
                            p("guide.maidmarriage.section.4.p.3"),
                            p("guide.maidmarriage.section.4.p.4"),
                            p("guide.maidmarriage.section.4.p.5"),
                            p("guide.maidmarriage.section.4.p.6"),
                            p("guide.maidmarriage.section.4.p.7"))),
            section("guide.maidmarriage.section.5.title",
                    List.of(
                            p("guide.maidmarriage.section.5.p.0"),
                            p("guide.maidmarriage.section.5.p.1"),
                            p("guide.maidmarriage.section.5.p.2"),
                            p("guide.maidmarriage.section.5.p.3"),
                            p("guide.maidmarriage.section.5.p.4"))),
            section("guide.maidmarriage.section.6.title",
                    List.of(
                            p("guide.maidmarriage.section.6.p.0"),
                            p("guide.maidmarriage.section.6.p.1"),
                            p("guide.maidmarriage.section.6.p.2"),
                            p("guide.maidmarriage.section.6.p.3"),
                            p("guide.maidmarriage.section.6.p.4"))),
            section("guide.maidmarriage.section.7.title",
                    List.of(
                            p("guide.maidmarriage.section.7.p.0"),
                            p("guide.maidmarriage.section.7.p.1"),
                            p("guide.maidmarriage.section.7.p.2"),
                            p("guide.maidmarriage.section.7.p.3"),
                            p("guide.maidmarriage.section.7.p.4"),
                            p("guide.maidmarriage.section.7.p.5"),
                            p("guide.maidmarriage.section.7.p.6"))),
            section("guide.maidmarriage.section.8.title",
                    List.of(
                            p("guide.maidmarriage.section.8.p.0"),
                            p("guide.maidmarriage.section.8.p.1"),
                            p("guide.maidmarriage.section.8.p.2"),
                            p("guide.maidmarriage.section.8.p.3"),
                            p("guide.maidmarriage.section.8.p.4"),
                            p("guide.maidmarriage.section.8.p.5"),
                            p("guide.maidmarriage.section.8.p.6"),
                            p("guide.maidmarriage.section.8.p.7"),
                            p("guide.maidmarriage.section.8.p.8"),
                            p("guide.maidmarriage.section.8.p.9"))),
            section("guide.maidmarriage.section.9.title",
                    List.of(
                            p("guide.maidmarriage.section.9.p.0"),
                            p("guide.maidmarriage.section.9.p.1"),
                            p("guide.maidmarriage.section.9.p.2"),
                            p("guide.maidmarriage.section.9.p.3"),
                            p("guide.maidmarriage.section.9.p.4"),
                            p("guide.maidmarriage.section.9.p.5"),
                            p("guide.maidmarriage.section.9.p.6"))),
            section("guide.maidmarriage.section.10.title",
                    List.of(
                            p("guide.maidmarriage.section.10.p.0"),
                            p("guide.maidmarriage.section.10.p.1"),
                            p("guide.maidmarriage.section.10.p.2"),
                            p("guide.maidmarriage.section.10.p.3"),
                            p("guide.maidmarriage.section.10.p.4"),
                            p("guide.maidmarriage.section.10.p.5"),
                            p("guide.maidmarriage.section.10.p.6"),
                            p("guide.maidmarriage.section.10.p.7"),
                            p("guide.maidmarriage.section.10.p.8"))),
            section("guide.maidmarriage.section.11.title",
                    List.of(
                            p("guide.maidmarriage.section.11.p.0"),
                            p("guide.maidmarriage.section.11.p.1"),
                            p("guide.maidmarriage.section.11.p.2"),
                            p("guide.maidmarriage.section.11.p.3"),
                            p("guide.maidmarriage.section.11.p.4"),
                            p("guide.maidmarriage.section.11.p.5"),
                            p("guide.maidmarriage.section.11.p.6"))),
            section("guide.maidmarriage.section.13.title",
                    List.of(
                            p("guide.maidmarriage.section.13.p.0"),
                            p("guide.maidmarriage.section.13.p.1"),
                            p("guide.maidmarriage.section.13.p.2"),
                            p("guide.maidmarriage.section.13.p.3"),
                            p("guide.maidmarriage.section.13.p.4"),
                            p("guide.maidmarriage.section.13.p.5"),
                            p("guide.maidmarriage.section.13.p.6"),
                            p("guide.maidmarriage.section.13.p.7"),
                            p("guide.maidmarriage.section.13.p.8"))),
            section("guide.maidmarriage.section.12.title",
                    List.of(
                            p("guide.maidmarriage.section.12.p.0"),
                            p("guide.maidmarriage.section.12.p.1"),
                            p("guide.maidmarriage.section.12.p.2"),
                            p("guide.maidmarriage.section.12.p.3"),
                            p("guide.maidmarriage.section.12.p.4"),
                            p("guide.maidmarriage.section.12.p.5")))
    );

    private final Screen parent;
    private int scroll;
    private int maxScroll;
    private int cachedContentWidth = -1;
    private String cachedLanguage = "";
    private List<GuideRenderLine> cachedLines = List.of();

    public MaidMarriageGuideScreen(Screen parent) {
        super(Component.translatable("guide.maidmarriage.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(this.width / 2 - 38, this.height - 30, 76, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ScreenBackgrounds.renderInWorld(graphics, this.width, this.height);

        int panelLeft = this.width / 2 - PANEL_WIDTH / 2;
        int panelRight = this.width / 2 + PANEL_WIDTH / 2;
        int panelTop = 18;
        int panelBottom = this.height - 38;
        int contentWidth = PANEL_WIDTH - CONTENT_PADDING * 2;
        rebuildLayoutIfNeeded(contentWidth);

        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDD14121E);
        graphics.fill(panelLeft, panelTop, panelRight, panelTop + 22, 0xEE201A33);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 7, 0xFFFFE8B8);

        graphics.enableScissor(panelLeft + 8, panelTop + 28, panelRight - 8, panelBottom - 8);
        int baseY = panelTop + 32 - scroll;
        int visibleTop = panelTop + 28;
        int visibleBottom = panelBottom - 8;
        for (GuideRenderLine line : cachedLines) {
            int y = baseY + line.y();
            if (y + this.font.lineHeight < visibleTop || y > visibleBottom) {
                continue;
            }
            graphics.drawString(this.font, line.text(), panelLeft + CONTENT_PADDING + line.x(), y, line.color(), false);
        }
        graphics.disableScissor();

        int contentHeight = cachedLines.isEmpty()
                ? 0
                : cachedLines.get(cachedLines.size() - 1).y() + this.font.lineHeight + 8;
        maxScroll = Math.max(0, contentHeight - (panelBottom - panelTop - 44));
        scroll = Mth.clamp(scroll, 0, maxScroll);

        if (maxScroll > 0) {
            int trackTop = panelTop + 30;
            int trackBottom = panelBottom - 10;
            int trackHeight = trackBottom - trackTop;
            int thumbHeight = Math.max(18, trackHeight * trackHeight / Math.max(trackHeight + maxScroll, 1));
            int thumbY = trackTop + (trackHeight - thumbHeight) * scroll / maxScroll;
            graphics.fill(panelRight - 12, trackTop, panelRight - 8, trackBottom, 0x553D3752);
            graphics.fill(panelRight - 12, thumbY, panelRight - 8, thumbY + thumbHeight, 0xFFFFD38B);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ScreenBackgrounds.suppressVanillaBackground();
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        ScreenBackgrounds.suppressVanillaBackground();
    }

    @Override
    protected void renderMenuBackground(GuiGraphics graphics) {
        ScreenBackgrounds.suppressVanillaBackground();
    }

    @Override
    public void renderTransparentBackground(GuiGraphics graphics) {
        ScreenBackgrounds.suppressVanillaBackground();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Mth.clamp(scroll - (int) Math.round(scrollY * 18.0D), 0, maxScroll);
        return true;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private void rebuildLayoutIfNeeded(int contentWidth) {
        Minecraft minecraft = Minecraft.getInstance();
        String language = minecraft.getLanguageManager().getSelected();
        if (contentWidth == cachedContentWidth && language.equals(cachedLanguage)) {
            return;
        }
        cachedContentWidth = contentWidth;
        cachedLanguage = language;
        cachedLines = buildLayout(contentWidth);
    }

    private List<GuideRenderLine> buildLayout(int contentWidth) {
        List<GuideRenderLine> lines = new ArrayList<>();
        int y = 0;
        for (GuideSection section : SECTIONS) {
            lines.add(new GuideRenderLine(0, y, Component.translatable(section.titleKey()).getVisualOrderText(), 0xFFFFD38B));
            y += 14;
            for (GuideParagraph paragraph : section.paragraphs()) {
                if (paragraph.keyId() != null) {
                    y = appendKeyParagraph(lines, paragraph, y, contentWidth);
                    continue;
                }
                y = appendWrapped(lines, Component.translatable(paragraph.textKey()), 0, y, contentWidth, 0xFFE7E0F5) + 6;
            }
            y += 8;
        }
        return List.copyOf(lines);
    }

    private int appendKeyParagraph(List<GuideRenderLine> lines, GuideParagraph paragraph, int y, int width) {
        String keyId = paragraph.keyId();
        String prefix = Component.translatable("guide.maidmarriage.key." + keyId + ".label").getString() + ": ";
        String keyBadge = "[" + keyName(keyId) + "]";
        lines.add(new GuideRenderLine(0, y, Component.literal(prefix).getVisualOrderText(), 0xFFE7E0F5));
        int keyX = this.font.width(prefix);
        lines.add(new GuideRenderLine(keyX, y, Component.literal(keyBadge).getVisualOrderText(), 0xFFFFD38B));
        int textX = keyX + this.font.width(keyBadge + " ");
        int bodyWidth = Math.max(40, width - textX);
        return appendWrapped(lines, Component.translatable(paragraph.textKey()), textX, y, bodyWidth, 0xFFE7E0F5) + 6;
    }

    private int appendWrapped(List<GuideRenderLine> lines, Component text, int x, int y, int width, int color) {
        for (FormattedCharSequence sequence : this.font.split(text, width)) {
            lines.add(new GuideRenderLine(x, y, sequence, color));
            y += this.font.lineHeight;
        }
        return y;
    }

    private static String keyName(String keyId) {
        return switch (keyId) {
            case "interaction" -> RhythmKeyMappings.boundKeyName(RhythmKeyMappings.INTERACTION);
            case "pet_head" -> RhythmKeyMappings.boundKeyName(RhythmKeyMappings.PET_HEAD);
            case "rhythm_hit" -> RhythmKeyMappings.boundKeyName(RhythmKeyMappings.RHYTHM_HIT);
            case "restore_ui" -> RhythmKeyMappings.boundKeyName(RhythmKeyMappings.RESTORE_HUG_UI);
            case "lap_exit" -> RhythmKeyMappings.boundKeyName(RhythmKeyMappings.LAP_PILLOW_EXIT);
            default -> "?";
        };
    }

    private static GuideSection section(String titleKey, List<GuideParagraph> paragraphs) {
        return new GuideSection(titleKey, paragraphs);
    }

    private static GuideParagraph p(String textKey) {
        return new GuideParagraph(textKey, null);
    }

    private static GuideParagraph key(String keyId, String textKey) {
        return new GuideParagraph(textKey, keyId);
    }

    private record GuideSection(String titleKey, List<GuideParagraph> paragraphs) {
    }

    private record GuideParagraph(String textKey, String keyId) {
    }

    private record GuideRenderLine(int x, int y, FormattedCharSequence text, int color) {
    }
}
