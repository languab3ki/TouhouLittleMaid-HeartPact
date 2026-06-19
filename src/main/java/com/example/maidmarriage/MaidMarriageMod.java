package com.example.maidmarriage;

import com.example.maidmarriage.config.ModConfigs;
import com.example.maidmarriage.client.ClientOnlyBootstrap;
import com.example.maidmarriage.debug.ModDebugCommands;
import com.example.maidmarriage.init.ModCreativeTabs;
import com.example.maidmarriage.init.ModEffects;
import com.example.maidmarriage.init.ModEntities;
import com.example.maidmarriage.init.ModItems;
import com.example.maidmarriage.init.ModPotions;
import com.example.maidmarriage.network.ModNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MaidMarriageMod.MOD_ID)
/**
 * 模组主入口：负责注册物品、实体与配置界面。
 * 该类的具体逻辑可参见下方方法与字段定义。
 */
public final class MaidMarriageMod {
    public static final String MOD_ID = "maidmarriage";

    public MaidMarriageMod(IEventBus modBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.CREATIVE_TABS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModEffects.MOB_EFFECTS.register(modBus);
        ModPotions.POTIONS.register(modBus);
        modBus.addListener(ModNetworking::register);
        NeoForge.EVENT_BUS.register(ModDebugCommands.class);
        modContainer.registerConfig(ModConfig.Type.COMMON, ModConfigs.SPEC);
        modBus.addListener(ModConfigs::onConfigLoaded);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientOnlyBootstrap.init(modContainer);
        }
    }
}
