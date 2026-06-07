package com.example.maidmarriage.client.interaction;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.client.HugClientState;
import com.example.maidmarriage.client.RhythmKeyMappings;
import com.example.maidmarriage.client.dialoguesystem.runtime.DialogueChoiceView;
import com.example.maidmarriage.client.dialoguesystem.runtime.DialogueFrameView;
import com.example.maidmarriage.client.dialoguesystem.runtime.HugDialogueRuntimeBridge;
import com.example.maidmarriage.client.dialogueui.DialogueBoxComponent;
import com.example.maidmarriage.client.dialogueui.DialogueIconButtonComponent;
import com.example.maidmarriage.client.dialogueui.DialogueOptionComponent;
import com.example.maidmarriage.client.dialogueui.DialoguePortraitComponent;
import com.example.maidmarriage.client.dialogueui.DialogueState;
import com.example.maidmarriage.client.dialogueui.DialogueTheme;
import com.example.maidmarriage.client.dialogueui.DialogueThemeLoader;
import com.example.maidmarriage.config.DialogueScriptManager;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import com.example.maidmarriage.entity.MaidSpiritEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Generic data-driven interaction screen for maid-related targets.
 *
 * <p>This intentionally does not replace {@code HugActionScreen} yet. It reuses the good UI pieces
 * from the hug UI and lets new target types prove the generic framework first.
 */
@Mod.EventBusSubscriber(modid = MaidMarriageMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class GenericMaidInteractionScreen extends Screen {
    private static final ResourceLocation DEFAULT_THEME_ID = new ResourceLocation(MaidMarriageMod.MOD_ID, "hug_default");
    private static final ResourceLocation HIDE_ICON = new ResourceLocation(MaidMarriageMod.MOD_ID, "textures/gui/hug_hide_icon.png");
    private static final ResourceLocation EXIT_ICON = new ResourceLocation(MaidMarriageMod.MOD_ID, "textures/gui/hug_exit_icon.png");
    private static final ResourceLocation SOFT_SMILE = new ResourceLocation(MaidMarriageMod.MOD_ID, "textures/gui/emotion/soft_smile.png");
    @Nullable
    private static GenericMaidInteractionScreen hiddenScreen;
    private static boolean lastRestoreUiKey;
    private static boolean lastExitKey;

    private final InteractionSession session;
    private final InteractionTargetAdapter adapter;
    private final HugDialogueRuntimeBridge runtime;
    private final DialogueBoxComponent dialogueBox = new DialogueBoxComponent();
    private final DialoguePortraitComponent portrait = new DialoguePortraitComponent();
    private final DialogueIconButtonComponent hideButton = new DialogueIconButtonComponent();
    private final DialogueIconButtonComponent exitButton = new DialogueIconButtonComponent();
    private final List<DialogueOptionComponent> options = new ArrayList<>();
    private final DialogueState dialogueState = new DialogueState();

    private DialogueTheme theme;
    private boolean compactMode;
    private boolean narrationPage;
    private String displayedFrameKey = "";
    private String cachedTargetName = "";
    private String debugMessage = "";
    private int debugMessageTicks;

    public GenericMaidInteractionScreen(InteractionSession session, InteractionTargetAdapter adapter) {
        super(session == null ? Component.empty() : session.title());
        this.session = session;
        this.adapter = adapter;
        this.runtime = new HugDialogueRuntimeBridge(
                session == null ? new ResourceLocation(MaidMarriageMod.MOD_ID, "missing") : session.scenarioId(),
                this::safeTargetName,
                this::resolvePlayerName
        );
    }

    @Override
    protected void init() {
        this.cachedTargetName = resolveTargetName();
        this.runtime.prepare();
        updateRuntimeVariables();
        this.runtime.start();
        ResourceLocation themeId = this.runtime.activeThemeId();
        if (themeId == null) {
            themeId = DEFAULT_THEME_ID;
        }
        this.theme = DialogueThemeLoader.load(themeId);
        rebuildComponents();
        refreshDialogueState(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isTargetStillValid()) {
            onClose();
            return;
        }
        if (debugMessageTicks > 0) {
            debugMessageTicks--;
        }
        if (!compactMode) {
            dialogueBox.tickTypewriter();
        }
        String targetName = resolveTargetName();
        if (!targetName.equals(cachedTargetName)) {
            cachedTargetName = targetName;
            refreshDialogueState(false);
        }
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        rebuildComponents();
        refreshDialogueState(false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (compactMode) {
            renderDebugMessage(graphics);
            return;
        }
        dialogueBox.render(graphics, this.width, this.height, mouseX, mouseY);
        portrait.render(graphics, this.width, this.height, mouseX, mouseY);
        for (DialogueOptionComponent option : options) {
            option.render(graphics, this.width, this.height, mouseX, mouseY);
        }
        hideButton.render(graphics, this.width, this.height, mouseX, mouseY);
        exitButton.render(graphics, this.width, this.height, mouseX, mouseY);
        renderSpiritStatus(graphics);
        renderDebugMessage(graphics);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (compactMode) {
            compactMode = false;
            return true;
        }
        if (hideButton.contains(mouseX, mouseY, this.width, this.height)) {
            hideUi();
            return true;
        }
        if (exitButton.contains(mouseX, mouseY, this.width, this.height)) {
            onClose();
            return true;
        }
        for (DialogueOptionComponent option : options) {
            if (option.hidden || !option.contains(mouseX, mouseY, this.width, this.height)) {
                continue;
            }
            if (!dialogueBox.isComplete()) {
                dialogueBox.revealAll();
                return true;
            }
            boolean accepted = runtime.choose(option.id());
            drainActions();
            if (accepted) {
                narrationPage = false;
                refreshDialogueState(true);
            } else {
                showDebugMessage("Option unavailable: " + option.id());
            }
            return true;
        }
        if (dialogueBox.contains(mouseX, mouseY, this.width, this.height)) {
            advanceDialogue();
            return true;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (RhythmKeyMappings.LAP_PILLOW_EXIT.matches(keyCode, scanCode)
                || RhythmKeyMappings.INTERACTION.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        if (RhythmKeyMappings.RESTORE_HUG_UI.matches(keyCode, scanCode)) {
            hideUi();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER) {
            advanceDialogue();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @SubscribeEvent
    public static void hideVanillaHudWhenGenericInteractionActive(RenderGuiOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof GenericMaidInteractionScreen screen) || !screen.shouldHideVanillaHud()) {
            return;
        }
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())
                || event.getOverlay().id().equals(VanillaGuiOverlay.CHAT_PANEL.id())) {
            event.setCanceled(true);
        }
    }

    @Override
    public void onClose() {
        HugClientState.clearTransientRenderState();
        compactMode = false;
        if (hiddenScreen == this) {
            hiddenScreen = null;
        }
        if (this.minecraft != null && this.minecraft.screen == this) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public void removed() {
        HugClientState.clearTransientRenderState();
        super.removed();
    }

    private boolean shouldHideVanillaHud() {
        return !compactMode;
    }

    public static void tickHiddenHotkeys(Minecraft minecraft) {
        if (minecraft == null || hiddenScreen == null) {
            lastRestoreUiKey = false;
            lastExitKey = false;
            return;
        }
        boolean restoreUiKey = RhythmKeyMappings.RESTORE_HUG_UI.isDown();
        if (restoreUiKey && !lastRestoreUiKey) {
            restoreHidden(minecraft);
        }
        lastRestoreUiKey = restoreUiKey;

        boolean exitKey = RhythmKeyMappings.LAP_PILLOW_EXIT.isDown();
        if (exitKey && !lastExitKey) {
            hiddenScreen.onClose();
        }
        lastExitKey = exitKey;
    }

    private static void restoreHidden(Minecraft minecraft) {
        if (minecraft == null || hiddenScreen == null) {
            return;
        }
        GenericMaidInteractionScreen screen = hiddenScreen;
        hiddenScreen = null;
        screen.compactMode = false;
        minecraft.setScreen(screen);
        minecraft.mouseHandler.releaseMouse();
    }

    private void hideUi() {
        compactMode = true;
        hiddenScreen = this;
        if (this.minecraft == null) {
            return;
        }
        if (this.minecraft.player != null) {
            String restoreKey = RhythmKeyMappings.boundKeyName(RhythmKeyMappings.RESTORE_HUG_UI);
            String exitKey = RhythmKeyMappings.boundKeyName(RhythmKeyMappings.LAP_PILLOW_EXIT);
            this.minecraft.player.displayClientMessage(Component.translatable(
                    "message.maidmarriage.interaction.ui_hidden",
                    restoreKey,
                    exitKey
            ), false);
        }
        this.minecraft.setScreen(null);
        this.minecraft.mouseHandler.grabMouse();
    }

    private void rebuildComponents() {
        if (theme == null) {
            theme = new DialogueTheme();
        }
        dialogueBox.applyTheme(theme.dialogBox);
        portrait.setBounds(theme.portrait.x, theme.portrait.y, theme.portrait.width, theme.portrait.height);
        portrait.setAlign(theme.portrait.alignX, theme.portrait.alignY);
        portrait.setTexture(DialogueTheme.parseTexture(theme.portrait.texture, SOFT_SMILE));
        portrait.setAlpha(theme.portrait.alpha);
        portrait.hidden = !theme.portrait.visible;
        hideButton.applyTheme(theme.controlIcon)
                .setIconTexture(HIDE_ICON)
                .setChromeEnabled(false);
        exitButton.applyTheme(theme.controlIcon)
                .setIconTexture(EXIT_ICON)
                .setChromeEnabled(false);
        float step = theme.controlIcon.width + theme.controlIcon.gapX;
        hideButton.setBounds(
                valueOr(theme.controlIcon.hideX, theme.controlIcon.x),
                valueOr(theme.controlIcon.hideY, theme.controlIcon.y),
                valueOr(theme.controlIcon.hideWidth, theme.controlIcon.width),
                valueOr(theme.controlIcon.hideHeight, theme.controlIcon.height)
        );
        exitButton.setBounds(
                valueOr(theme.controlIcon.exitX, theme.controlIcon.x + step),
                valueOr(theme.controlIcon.exitY, theme.controlIcon.y),
                valueOr(theme.controlIcon.exitWidth, theme.controlIcon.width),
                valueOr(theme.controlIcon.exitHeight, theme.controlIcon.height)
        );
    }

    private void refreshDialogueState(boolean resetTypewriter) {
        updateRuntimeVariables();
        DialogueFrameView frame = runtime.currentFrame();
        drainActions();
        String frameKey = frameKey(frame);
        if (!frameKey.equals(displayedFrameKey)) {
            displayedFrameKey = frameKey;
            narrationPage = false;
        }
        rebuildOptionComponents(frame);
        String speaker = runtime.renderTemplate(frame.speaker());
        String text = runtime.renderTemplate(frame.text());
        if (narrationPage) {
            speaker = "";
            text = runtime.renderTemplate(frame.narration());
        }
        dialogueState
                .setSpeaker(speaker)
                .setText(Component.literal(text))
                .setNarration(Component.literal(runtime.renderTemplate(frame.narration())))
                .setHint(Component.literal(resolveHint(frame)))
                .setPortraitTexture(DialogueTheme.parseTexture(frame.portraitTexture(), SOFT_SMILE));
        portrait.setTexture(dialogueState.portraitTexture());
        dialogueBox
                .setSpeaker(dialogueState.speaker())
                .setFullText(dialogueState.text(), resetTypewriter)
                .setHint(dialogueState.hint());
    }

    private static String frameKey(DialogueFrameView frame) {
        if (frame == null) {
            return "";
        }
        return frame.scenarioId()
                + "|" + frame.nodeId()
                + "|" + frame.lineIndex()
                + "|" + frame.choiceNode()
                + "|" + frame.speaker()
                + "|" + frame.text()
                + "|" + frame.narration();
    }

    private void rebuildOptionComponents(DialogueFrameView frame) {
        options.clear();
        if (frame == null || frame.choices().isEmpty()) {
            return;
        }
        int index = 0;
        for (DialogueChoiceView choice : frame.choices()) {
            if (!choice.available()) {
                continue;
            }
            DialogueOptionComponent option = new DialogueOptionComponent(
                    choice.id(),
                    Component.literal(runtime.renderTemplate(choice.title())),
                    Component.literal(runtime.renderTemplate(choice.description()))
            );
            option.applyTheme(theme.option, index++);
            options.add(option);
        }
    }

    private void advanceDialogue() {
        if (!dialogueBox.isComplete()) {
            dialogueBox.revealAll();
            return;
        }
        DialogueFrameView frame = runtime.currentFrame();
        if (frame.choiceNode()) {
            return;
        }
        if (!narrationPage && frame.narration() != null && !frame.narration().isBlank()) {
            narrationPage = true;
            refreshDialogueState(true);
            return;
        }
        runtime.advance();
        drainActions();
        narrationPage = false;
        refreshDialogueState(true);
    }

    private void drainActions() {
        Minecraft minecraft = Minecraft.getInstance();
        Entity target = findTarget();
        for (var request : runtime.drainActionRequests()) {
            InteractionActionRegistry.execute(
                    minecraft,
                    session,
                    adapter,
                    request,
                    target,
                    this::showDebugMessage,
                    this::onClose
            );
        }
    }

    private void updateRuntimeVariables() {
        runtime.updateVariables();
        Entity target = findTarget();
        if (target != null) {
            adapter.writeVariables(runtime, Minecraft.getInstance(), target);
        }
        runtime.setVariable("target_type", session.targetType().toString());
    }

    private String resolveHint(DialogueFrameView frame) {
        String spiritTip = resolveSpiritTip();
        if (!spiritTip.isBlank()) {
            return spiritTip;
        }
        if (frame != null && !frame.choiceNode()) {
            return DialogueScriptManager.component("ui.maidmarriage.generic_interaction.hint_continue").getString();
        }
        return "";
    }

    private boolean isTargetStillValid() {
        return adapter.isStillValid(Minecraft.getInstance(), session.targetUuid());
    }

    @Nullable
    private Entity findTarget() {
        return InteractionTargetAdapter.findEntity(Minecraft.getInstance(), session.targetUuid());
    }

    private String resolveTargetName() {
        Entity target = findTarget();
        Component name = target == null ? null : adapter.displayName(target);
        return name == null ? "" : name.getString();
    }

    private String safeTargetName() {
        return cachedTargetName == null || cachedTargetName.isBlank()
                ? DialogueScriptManager.component("ui.maidmarriage.hug_action.maid_fallback").getString()
                : cachedTargetName;
    }

    private String resolvePlayerName() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return DialogueScriptManager.component("ui.maidmarriage.hug_action.player_fallback").getString();
        }
        return minecraft.player.getName().getString();
    }

    private void renderDebugMessage(GuiGraphics graphics) {
        if (debugMessageTicks <= 0 || debugMessage.isBlank()) {
            return;
        }
        graphics.fill(5, 5, 9 + this.font.width(debugMessage), 18, 0xAA120C16);
        graphics.drawString(this.font, debugMessage, 8, 8, 0xFFEFCFDE, false);
    }

    private void renderSpiritStatus(GuiGraphics graphics) {
        Entity target = findTarget();
        if (!(target instanceof MaidSpiritEntity spirit)) {
            return;
        }
        Component longing = Component.translatable("ui.maidmarriage.spirit.longing", spirit.getLonging());
        int right = this.width - 10;
        graphics.drawString(this.font, longing, right - this.font.width(longing), 10, 0xFFFFEEF8, true);
    }

    private String resolveSpiritTip() {
        Entity target = findTarget();
        if (!(target instanceof MaidSpiritEntity)) {
            return "";
        }
        String tip = runtime.renderTemplate("${spirit_tip}");
        if (tip == null || tip.isBlank() || "${spirit_tip}".equals(tip)) {
            return "";
        }
        return Component.translatable("ui.maidmarriage.spirit.tip", tip).getString();
    }

    private void showDebugMessage(String message) {
        this.debugMessage = message == null ? "" : message;
        this.debugMessageTicks = 80;
    }

    private static float valueOr(@Nullable Float value, float fallback) {
        return value == null ? fallback : value;
    }
}
