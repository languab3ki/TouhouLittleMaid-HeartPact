package com.example.maidmarriage.util;

import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * 统一封装 Minecraft 1.21 的文本组件 JSON 序列化入口。
 *
 * <p>1.21 起 {@link Component.Serializer} 需要注册表上下文，直接调用旧版
 * {@code toJson(component)} / {@code fromJson(json)} 会编译失败。把迁移差异集中在这里，
 * 可以避免各个业务类重复处理 registryAccess，也方便后续排查存档文本兼容问题。
 */
public final class ComponentJsonUtil {
    private ComponentJsonUtil() {
    }

    public static String toJson(Component component, Level level) {
        return toJson(component, level.registryAccess());
    }

    public static String toJson(Component component, HolderLookup.Provider provider) {
        return Component.Serializer.toJson(component, provider);
    }

    @Nullable
    public static Component fromJson(@Nullable String json, Level level) {
        return fromJson(json, level.registryAccess());
    }

    @Nullable
    public static Component fromJson(@Nullable String json, HolderLookup.Provider provider) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return Component.Serializer.fromJson(json, provider);
        } catch (Exception ignored) {
            return null;
        }
    }
}
