package com.example.maidmarriage.client;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.config.ModConfigs;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

/**
 * 大女仆抱小女仆姿态的客户端调试器。
 *
 * <p>这个类只负责开发期实时调姿态，不参与服务端逻辑，也不影响存档数据。
 * 游戏内按 F8 调 GeckoLib，按 F11 调东方 / Bedrock，然后使用 Alt + 方向键切换/调整参数。
 * 调好以后把左上角显示的数值发出来，再固化回默认常量即可。
 */
@EventBusSubscriber(modid = MaidMarriageMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class CarryChildPoseDebug {
    private static final PoseParam[] GECKO_PARAMS = {
            PoseParam.GECKO_ROT_X,
            PoseParam.GECKO_ROT_Y,
            PoseParam.GECKO_ROT_Z,
            PoseParam.GECKO_SHIFT_X,
            PoseParam.GECKO_SHIFT_Y,
            PoseParam.GECKO_SHIFT_Z,
            PoseParam.GECKO_TRANSLATE_X,
            PoseParam.GECKO_TRANSLATE_Y,
            PoseParam.GECKO_TRANSLATE_Z
    };
    private static final PoseParam[] BEDROCK_PARAMS = {
            PoseParam.BEDROCK_ROT_X,
            PoseParam.BEDROCK_ROT_Y,
            PoseParam.BEDROCK_ROT_Z,
            PoseParam.BEDROCK_SHIFT_X,
            PoseParam.BEDROCK_SHIFT_Y,
            PoseParam.BEDROCK_SHIFT_Z,
            PoseParam.BEDROCK_TRANSLATE_X,
            PoseParam.BEDROCK_TRANSLATE_Y,
            PoseParam.BEDROCK_TRANSLATE_Z
    };

    private static boolean geckoEnabled;
    private static boolean bedrockEnabled;
    private static int selectedIndex;

    /**
     * GeckoLib 模型专用调试参数。
     */
    private static float geckoRotX = 190.0F;
    private static float geckoRotY = -90.0F;
    private static float geckoRotZ = -100.0F;
    private static float geckoShiftX = -19.4F;
    private static float geckoShiftY = -10.4F;
    private static float geckoShiftZ = -12.5F;
    private static double geckoTranslateX = 0.27D;
    private static double geckoTranslateY = -0.03D;
    private static double geckoTranslateZ = 0.0D;

    /**
     * 东方 / Bedrock 模型专用调试参数。
     *
     * <p>这套参数只作用于普通 Bedrock 女仆模型，和 GeckoLib 完全隔离，
     * 调东方模型时不会把 Gecko 那边已经调好的姿态弄乱。
     */
    private static float bedrockRotX = 90.0F;
    private static float bedrockRotY = -90.0F;
    private static float bedrockRotZ = -100.0F;
    private static float bedrockShiftX = 4.2F;
    private static float bedrockShiftY = -2.0F;
    private static float bedrockShiftZ = 0.0F;
    private static double bedrockTranslateX = 0.97D;
    private static double bedrockTranslateY = 0.57D;
    private static double bedrockTranslateZ = 0.8D;

    private static boolean lastLeft;
    private static boolean lastRight;
    private static boolean lastUp;
    private static boolean lastDown;
    private static boolean lastCopy;
    private static boolean lastReset;
    private static boolean overlayDirty = true;
    private static String[] overlayLines = new String[0];

    private CarryChildPoseDebug() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!ModConfigs.enableDebugTools()) {
            geckoEnabled = false;
            bedrockEnabled = false;
            clearKeyEdges();
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }

        boolean skipGeckoToggle = YsmLiftHeightDebug.consumeCarryPoseDebugToggleSuppression();
        long window = mc.getWindow().getWindow();
        boolean shift = isDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || isDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        while (RhythmKeyMappings.CARRY_POSE_DEBUG.consumeClick()) {
            if (skipGeckoToggle || shift) {
                continue;
            }
            geckoEnabled = !geckoEnabled;
            if (geckoEnabled) {
                bedrockEnabled = false;
            }
            selectedIndex = clampSelectedIndex(selectedIndex, activeParams());
            markOverlayDirty();
            mc.player.displayClientMessage(Component.literal(geckoEnabled
                    ? "GeckoLib 抱小女仆姿态调试：开启"
                    : "GeckoLib 抱小女仆姿态调试：关闭"), true);
        }
        while (RhythmKeyMappings.BEDROCK_CARRY_POSE_DEBUG.consumeClick()) {
            bedrockEnabled = !bedrockEnabled;
            if (bedrockEnabled) {
                geckoEnabled = false;
            }
            selectedIndex = clampSelectedIndex(selectedIndex, activeParams());
            markOverlayDirty();
            mc.player.displayClientMessage(Component.literal(bedrockEnabled
                    ? "东方模型抱小女仆姿态调试：开启"
                    : "东方模型抱小女仆姿态调试：关闭"), true);
        }

        if (!geckoEnabled && !bedrockEnabled) {
            clearKeyEdges();
            return;
        }

        boolean alt = isDown(window, GLFW.GLFW_KEY_LEFT_ALT) || isDown(window, GLFW.GLFW_KEY_RIGHT_ALT);
        if (!alt) {
            clearAdjustmentEdges();
            return;
        }

        boolean left = isDown(window, GLFW.GLFW_KEY_LEFT);
        boolean right = isDown(window, GLFW.GLFW_KEY_RIGHT);
        boolean up = isDown(window, GLFW.GLFW_KEY_UP);
        boolean down = isDown(window, GLFW.GLFW_KEY_DOWN);
        boolean copy = isDown(window, GLFW.GLFW_KEY_C);
        boolean reset = isDown(window, GLFW.GLFW_KEY_R);
        PoseParam[] params = activeParams();

        if (pressed(left, lastLeft)) {
            selectedIndex = Math.floorMod(selectedIndex - 1, params.length);
            overlayDirty = true;
        }
        if (pressed(right, lastRight)) {
            selectedIndex = Math.floorMod(selectedIndex + 1, params.length);
            overlayDirty = true;
        }
        if (pressed(up, lastUp)) {
            adjustSelected(shift ? 5.0D : 1.0D);
        }
        if (pressed(down, lastDown)) {
            adjustSelected(shift ? -5.0D : -1.0D);
        }
        if (pressed(reset, lastReset)) {
            resetDefaults();
            mc.player.displayClientMessage(Component.literal("抱小女仆姿态调试：已恢复默认值"), true);
        }
        if (pressed(copy, lastCopy)) {
            String text = exportValues();
            mc.keyboardHandler.setClipboard(text);
            mc.player.displayClientMessage(Component.literal("抱小女仆姿态参数已复制到剪贴板"), true);
        }

        lastLeft = left;
        lastRight = right;
        lastUp = up;
        lastDown = down;
        lastCopy = copy;
        lastReset = reset;
    }

    @SubscribeEvent
    public static void onRender(RenderGuiLayerEvent.Post event) {
        if ((!geckoEnabled && !bedrockEnabled) || !ModConfigs.enableDebugTools()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        Font font = mc.font;
        int x = 8;
        int y = 8;
        String[] lines = overlayLines();
        for (int index = 0; index < lines.length; index++) {
            int color = index == 0 ? 0xFFFFE08A : index < 4 ? 0xFFE8E8E8 : 0xFFFFFFFF;
            if (index >= 4 && index - 4 == selectedIndex) {
                color = 0xFF8BFF98;
            }
            draw(event, font, x, y, lines[index], color);
            y += index == 3 ? 13 : 10;
        }
    }

    private static String[] overlayLines() {
        if (!overlayDirty) {
            return overlayLines;
        }
        overlayDirty = false;
        PoseParam[] params = activeParams();
        String[] lines = new String[params.length + 4];
        lines[0] = (bedrockEnabled ? "Bedrock Carry Child Pose Debug - F11 close" : "Gecko Carry Child Pose Debug - F8 close");
        lines[1] = "Alt+Left/Right select, Alt+Up/Down adjust, Shift = big step";
        lines[2] = "Alt+C copy, Alt+R reset";
        lines[3] = "Translate values affect the visible carried-child position";
        for (int index = 0; index < params.length; index++) {
            PoseParam param = params[index];
            lines[index + 4] = (index == selectedIndex ? "> " : "  ") + param.label + ": " + param.format();
        }
        overlayLines = lines;
        return overlayLines;
    }

    private static void markOverlayDirty() {
        overlayDirty = true;
    }

    public static float rotationX() {
        return geckoRotX;
    }

    public static float rotationY() {
        return geckoRotY;
    }

    public static float rotationZ() {
        return geckoRotZ;
    }

    public static float bedrockRotationX() {
        return bedrockRotX;
    }

    public static float bedrockRotationY() {
        return bedrockRotY;
    }

    public static float bedrockRotationZ() {
        return bedrockRotZ;
    }

    public static float shiftX() {
        return geckoShiftX;
    }

    public static float shiftY() {
        return geckoShiftY;
    }

    public static float shiftZ() {
        return geckoShiftZ;
    }

    public static double translateX() {
        return geckoTranslateX;
    }

    public static double translateY() {
        return geckoTranslateY;
    }

    public static double translateZ() {
        return geckoTranslateZ;
    }

    public static float bedrockShiftX() {
        return bedrockShiftX;
    }

    public static float bedrockShiftY() {
        return bedrockShiftY;
    }

    public static float bedrockShiftZ() {
        return bedrockShiftZ;
    }

    public static double bedrockTranslateX() {
        return bedrockTranslateX;
    }

    public static double bedrockTranslateY() {
        return bedrockTranslateY;
    }

    public static double bedrockTranslateZ() {
        return bedrockTranslateZ;
    }

    private static void adjustSelected(double direction) {
        PoseParam[] params = activeParams();
        selectedIndex = clampSelectedIndex(selectedIndex, params);
        PoseParam selected = params[selectedIndex];
        double step = selected.rotation ? 5.0D : 0.1D;
        selected.add(direction * step);
        markOverlayDirty();
    }

    private static void resetDefaults() {
        geckoRotX = 190.0F;
        geckoRotY = -90.0F;
        geckoRotZ = -100.0F;
        geckoShiftX = -19.4F;
        geckoShiftY = -10.4F;
        geckoShiftZ = -12.5F;
        geckoTranslateX = 0.27D;
        geckoTranslateY = -0.03D;
        geckoTranslateZ = 0.0D;

        bedrockRotX = 90.0F;
        bedrockRotY = -90.0F;
        bedrockRotZ = -100.0F;
        bedrockShiftX = 4.2F;
        bedrockShiftY = -2.0F;
        bedrockShiftZ = 0.0F;
        bedrockTranslateX = 0.97D;
        bedrockTranslateY = 0.57D;
        bedrockTranslateZ = 0.8D;
        markOverlayDirty();
    }

    private static String exportValues() {
        return String.format(Locale.ROOT,
                "gecko{rotX=%.2f, rotY=%.2f, rotZ=%.2f, shiftX=%.2f, shiftY=%.2f, shiftZ=%.2f, translateX=%.3f, translateY=%.3f, translateZ=%.3f}, "
                        + "bedrock{rotX=%.2f, rotY=%.2f, rotZ=%.2f, shiftX=%.2f, shiftY=%.2f, shiftZ=%.2f, translateX=%.3f, translateY=%.3f, translateZ=%.3f}",
                geckoRotX, geckoRotY, geckoRotZ, geckoShiftX, geckoShiftY, geckoShiftZ, geckoTranslateX, geckoTranslateY, geckoTranslateZ,
                bedrockRotX, bedrockRotY, bedrockRotZ, bedrockShiftX, bedrockShiftY, bedrockShiftZ, bedrockTranslateX, bedrockTranslateY, bedrockTranslateZ);
    }

    private static void draw(RenderGuiLayerEvent.Post event, Font font, int x, int y, String text, int color) {
        event.getGuiGraphics().drawString(font, text, x, y, color, true);
    }

    private static boolean isDown(long window, int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    private static boolean pressed(boolean current, boolean previous) {
        return current && !previous;
    }

    private static PoseParam[] activeParams() {
        return bedrockEnabled ? BEDROCK_PARAMS : GECKO_PARAMS;
    }

    private static int clampSelectedIndex(int index, PoseParam[] params) {
        if (params.length == 0) {
            return 0;
        }
        return Math.floorMod(index, params.length);
    }

    private static void clearKeyEdges() {
        clearAdjustmentEdges();
    }

    private static void clearAdjustmentEdges() {
        lastLeft = false;
        lastRight = false;
        lastUp = false;
        lastDown = false;
        lastCopy = false;
        lastReset = false;
    }

    private enum PoseParam {
        GECKO_ROT_X("Gecko Rot X", true) {
            @Override
            void add(double value) {
                geckoRotX += (float) value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.1f°", geckoRotX);
            }
        },
        GECKO_ROT_Y("Gecko Rot Y", true) {
            @Override
            void add(double value) {
                geckoRotY += (float) value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.1f°", geckoRotY);
            }
        },
        GECKO_ROT_Z("Gecko Rot Z", true) {
            @Override
            void add(double value) {
                geckoRotZ += (float) value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.1f°", geckoRotZ);
            }
        },
        GECKO_SHIFT_X("Gecko Shift X", false) {
            @Override
            void add(double value) {
                geckoShiftX += (float) value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.2f", geckoShiftX);
            }
        },
        GECKO_SHIFT_Y("Gecko Shift Y", false) {
            @Override
            void add(double value) {
                geckoShiftY += (float) value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.2f", geckoShiftY);
            }
        },
        GECKO_SHIFT_Z("Gecko Shift Z", false) {
            @Override
            void add(double value) {
                geckoShiftZ += (float) value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.2f", geckoShiftZ);
            }
        },
        GECKO_TRANSLATE_X("Gecko Translate X", false) {
            @Override
            void add(double value) {
                geckoTranslateX += value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.3f", geckoTranslateX);
            }
        },
        GECKO_TRANSLATE_Y("Gecko Translate Y", false) {
            @Override
            void add(double value) {
                geckoTranslateY += value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.3f", geckoTranslateY);
            }
        },
        GECKO_TRANSLATE_Z("Gecko Translate Z", false) {
            @Override
            void add(double value) {
                geckoTranslateZ += value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.3f", geckoTranslateZ);
            }
        },
        BEDROCK_ROT_X("Bedrock Rot X", true) {
            @Override
            void add(double value) {
                bedrockRotX += (float) value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.1f°", bedrockRotX);
            }
        },
        BEDROCK_ROT_Y("Bedrock Rot Y", true) {
            @Override
            void add(double value) {
                bedrockRotY += (float) value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.1f°", bedrockRotY);
            }
        },
        BEDROCK_ROT_Z("Bedrock Rot Z", true) {
            @Override
            void add(double value) {
                bedrockRotZ += (float) value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.1f°", bedrockRotZ);
            }
        },
        BEDROCK_SHIFT_X("Bedrock Shift X", false) {
            @Override
            void add(double value) {
                bedrockShiftX += (float) value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.2f", bedrockShiftX);
            }
        },
        BEDROCK_SHIFT_Y("Bedrock Shift Y", false) {
            @Override
            void add(double value) {
                bedrockShiftY += (float) value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.2f", bedrockShiftY);
            }
        },
        BEDROCK_SHIFT_Z("Bedrock Shift Z", false) {
            @Override
            void add(double value) {
                bedrockShiftZ += (float) value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.2f", bedrockShiftZ);
            }
        },
        BEDROCK_TRANSLATE_X("Bedrock Translate X", false) {
            @Override
            void add(double value) {
                bedrockTranslateX += value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.3f", bedrockTranslateX);
            }
        },
        BEDROCK_TRANSLATE_Y("Bedrock Translate Y", false) {
            @Override
            void add(double value) {
                bedrockTranslateY += value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.3f", bedrockTranslateY);
            }
        },
        BEDROCK_TRANSLATE_Z("Bedrock Translate Z", false) {
            @Override
            void add(double value) {
                bedrockTranslateZ += value;
            }

            @Override
            String format() {
                return String.format(Locale.ROOT, "%.3f", bedrockTranslateZ);
            }
        };

        private final String label;
        private final boolean rotation;

        PoseParam(String label, boolean rotation) {
            this.label = label;
            this.rotation = rotation;
        }

        abstract void add(double value);

        abstract String format();
    }
}
