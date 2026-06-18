package com.example.maidmarriage.mixin;

import com.example.maidmarriage.compat.YsmMolangActionBridge;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 往 YSM 已有的 `tlm.*` molang 注册表中追加我们模组自己的动作变量。
 *
 * <p>这里不把 YSM 当作编译依赖，而是：
 * 1. 用字符串目标类名挂 Mixin；
 * 2. 在注入点里用反射调用它的注册器；
 * 3. 用 JDK 动态代理实现 YSM 的 `eval(context)` 接口。
 *
 * <p>YSM 2.6.5（MC 1.21.1 NeoForge）把旧版的查询注册类重新混淆了：
 * 旧类 `o000OoO0Oo0oo0OOOo0O0Oo0` 已经不存在，新的 `tlm`/实体查询注册表在
 * `Oo000O00O0OOoO00OOOO000O` 构造函数里初始化。这里挂构造尾部，确保原版
 * `is_maid`、`maid` 等变量先注册完成，再补上 Heart Pact 的动作变量。
 *
 * <p>这样即使用户没装 YSM，也不会影响模组本体加载。
 */
@Pseudo
@Mixin(targets = "com.elfmcys.yesstevemodel.Oo000O00O0OOoO00OOOO000O", remap = false)
public abstract class YsmTlmMolangCompatMixin {
    private static final String EVAL_INTERFACE = "com.elfmcys.yesstevemodel.OOO0oOo0ooO0oO00OoOOo0Oo";
    private static final String CONTEXT_CLASS = "com.elfmcys.yesstevemodel.O000o00000000OOoO0OooOO0";
    private static final String REGISTER_LIVING_ENTITY_METHOD = "OO000o0ooOooooOOOOO0Ooo0";
    private static final String CONTEXT_ENTITY_GETTER = "oOo0OO0O0o000OO0O000oo0o";

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void maidmarriage$registerExtraTlmVariables(CallbackInfo ci) {
        try {
            Object registry = this;
            ClassLoader loader = registry.getClass().getClassLoader();
            Class<?> evalType = Class.forName(EVAL_INTERFACE, false, loader);
            Method register = registry.getClass().getMethod(REGISTER_LIVING_ENTITY_METHOD, String.class, evalType);

            register.invoke(registry, "maidmarriage_action", createEvaluator(loader, "action"));
            register.invoke(registry, "maidmarriage_hug", createEvaluator(loader, "hug"));
            register.invoke(registry, "maidmarriage_kiss", createEvaluator(loader, "kiss"));
            register.invoke(registry, "maidmarriage_pet", createEvaluator(loader, "pet"));
            register.invoke(registry, "maidmarriage_lift", createEvaluator(loader, "lift"));
            register.invoke(registry, "maidmarriage_action_time", createEvaluator(loader, "time"));
        } catch (Throwable ignored) {
        }
    }

    private static Object createEvaluator(ClassLoader loader, String kind) throws Exception {
        Class<?> evalType = Class.forName(EVAL_INTERFACE, false, loader);
        Class<?> contextType = Class.forName(CONTEXT_CLASS, false, loader);
        Method entityGetter = contextType.getMethod(CONTEXT_ENTITY_GETTER);

        InvocationHandler handler = (proxy, method, args) -> {
            // JDK 代理也会收到 Object 方法，不能把它们当作 Molang 求值处理。
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "HeartPactYsmEvaluator[" + kind + "]";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                };
            }
            if (!"eval".equals(method.getName()) || args == null || args.length != 1) {
                return null;
            }
            Object context = args[0];
            Object entity = entityGetter.invoke(context);
            if (!(entity instanceof EntityMaid maid)) {
                return defaultValue(kind);
            }
            return resolveValue(kind, maid);
        };
        return Proxy.newProxyInstance(loader, new Class<?>[]{evalType}, handler);
    }

    private static Object defaultValue(String kind) {
        return "action".equals(kind) ? 0 : ("time".equals(kind) ? 0.0D : Boolean.FALSE);
    }

    private static Object resolveValue(String kind, EntityMaid maid) {
        return switch (kind) {
            case "action" -> YsmMolangActionBridge.action(maid);
            case "hug" -> YsmMolangActionBridge.isHug(maid);
            case "kiss" -> YsmMolangActionBridge.isKiss(maid);
            case "pet" -> YsmMolangActionBridge.isPet(maid);
            case "lift" -> YsmMolangActionBridge.isLift(maid);
            case "time" -> YsmMolangActionBridge.actionTime(maid);
            default -> 0;
        };
    }
}
