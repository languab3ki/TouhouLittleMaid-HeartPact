package com.example.maidmarriage.compat;

import com.example.maidmarriage.init.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 灵体献花规则表。
 *
 * <p>这份表同时给客户端预览和服务端结算使用，避免 UI 显示能献、服务端却拒绝。</p>
 */
public final class SpiritOfferingRules {
    private SpiritOfferingRules() {
    }

    public static OfferingCategory classify(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return OfferingCategory.EMPTY;
        }
        if (isFlower(stack)) {
            return OfferingCategory.FLOWER;
        }
        if (isSoulItem(stack)) {
            return OfferingCategory.SOUL;
        }
        return OfferingCategory.BLOCKED;
    }

    public static boolean isAllowed(ItemStack stack) {
        OfferingCategory category = classify(stack);
        return category == OfferingCategory.FLOWER || category == OfferingCategory.SOUL;
    }

    public static boolean isFlower(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && (stack.is(ItemTags.FLOWERS) || stack.is(ModItems.RAINBOW_BOUQUET.get()));
    }

    public static boolean isSoulItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.is(Items.SOUL_LANTERN)
                || stack.is(Items.SOUL_TORCH)
                || stack.is(Items.SOUL_CAMPFIRE)
                || stack.is(Items.SOUL_SAND)
                || stack.is(Items.SOUL_SOIL)
                || stack.is(Items.ECHO_SHARD)
                || stack.is(Items.SCULK)
                || stack.is(Items.SCULK_CATALYST)
                || stack.is(Items.SCULK_SENSOR)
                || stack.is(Items.SCULK_SHRIEKER);
    }

    public static String itemKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key == null ? "" : key.toString();
    }

    public enum OfferingCategory {
        EMPTY,
        FLOWER,
        SOUL,
        BLOCKED
    }
}
