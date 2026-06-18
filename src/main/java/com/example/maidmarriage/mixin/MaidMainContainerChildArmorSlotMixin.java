package com.example.maidmarriage.mixin;

import com.example.maidmarriage.entity.MaidChildEntity;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.MaidMainContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复小女仆在 TLM 主面板里无法放入盔甲的问题。
 *
 * <p>TLM 的盔甲槽会调用 {@code ItemStack#canEquip(slot, maid)}。
 * 对原生女仆这条链路是正常的，但出生小女仆是我们自己的 EntityType，
 * 某些装备会在 NeoForge 的实体槽位判断里把她当成非标准穿戴者，导致四个盔甲槽全部拒绝。
 *
 * <p>这里仅在菜单实体确认为 {@link MaidChildEntity} 时接管判定：
 * 物品声明的装备槽必须与当前槽位一致，同时仍保留“不能放进容器的物品不能装备”的原版/TLM 约束。
 * 这样不会影响玩家、普通女仆，也不会放宽跨槽位装备。
 */
@Mixin(targets = "com.github.tartaricacid.touhoulittlemaid.inventory.container.MaidMainContainer$3", remap = false)
public abstract class MaidMainContainerChildArmorSlotMixin {
    @Shadow
    @Final
    private EquipmentSlot val$equipmentSlot;

    @Shadow
    @Final
    private MaidMainContainer this$0;

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void maidmarriage$allowChildMaidArmor(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!(this$0.getMaid() instanceof MaidChildEntity)) {
            return;
        }
        if (stack.isEmpty() || !stack.getItem().canFitInsideContainerItems()) {
            cir.setReturnValue(false);
            return;
        }
        EquipmentSlot stackSlot = resolveEquipmentSlot(stack);
        cir.setReturnValue(stackSlot == this.val$equipmentSlot);
    }

    private static EquipmentSlot resolveEquipmentSlot(ItemStack stack) {
        EquipmentSlot neoForgeSlot = stack.getEquipmentSlot();
        if (neoForgeSlot != null) {
            return neoForgeSlot;
        }
        Equipable equipable = Equipable.get(stack);
        return equipable == null ? EquipmentSlot.MAINHAND : equipable.getEquipmentSlot();
    }
}
