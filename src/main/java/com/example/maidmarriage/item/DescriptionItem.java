package com.example.maidmarriage.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import com.example.maidmarriage.util.ItemStackDataUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 带描述物品基类：统一悬浮提示与誓约戒指发光判定。
 * 该类的具体逻辑可参见下方方法与字段定义。
 */
public class DescriptionItem extends Item {
    private static final String TAG_RING_USED = "maidmarriage_ring_used";
    private final String tooltipKey;

    public DescriptionItem(Properties properties, String tooltipKey) {
        super(properties);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = ItemStackDataUtil.copyCustomData(stack);
        if (tag.getBoolean(TAG_RING_USED)) {
            return true;
        }
        return super.isFoil(stack);
    }
}
