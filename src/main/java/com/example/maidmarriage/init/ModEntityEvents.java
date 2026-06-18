package com.example.maidmarriage.init;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.entity.MaidChildEntity;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitCapabilities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = MaidMarriageMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
/**
 * 实体事件初始化：绑定实体属性与相关事件。
 * 该类的具体逻辑可参见下方方法与字段定义。
 */
public final class ModEntityEvents {
    private ModEntityEvents() {
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.MAID_CHILD.get(), EntityMaid.createAttributes().build());
        event.put(ModEntities.MAID_SPIRIT.get(), EntityMaid.createAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        /*
         * TLM 只会给原生 touhou_little_maid:maid 注册装备/手持物品 capability。
         * maid_child 虽然继承 EntityMaid，但 NeoForge capability 按 EntityType 精确注册，
         * 所以不补这层时 GUI 能打开，却拿不到 armor/hand/all inventory，表现为盔甲栏和主副手都不能用。
         */
        event.registerEntity(InitCapabilities.HAND_ITEM, ModEntities.MAID_CHILD.get(),
                (MaidChildEntity maid, net.minecraft.core.Direction side) -> maid.getHandsInvWrapper());
        event.registerEntity(InitCapabilities.ARMOR_ITEM, ModEntities.MAID_CHILD.get(),
                (MaidChildEntity maid, net.minecraft.core.Direction side) -> maid.getArmorInvWrapper());
        event.registerEntity(Capabilities.ItemHandler.ENTITY, ModEntities.MAID_CHILD.get(),
                (MaidChildEntity maid, Void ignored) -> maid.getAllInv());
    }
}
