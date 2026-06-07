package com.example.maidmarriage;

import com.example.maidmarriage.config.ModConfigs;
import com.example.maidmarriage.client.ClientOnlyBootstrap;
import com.example.maidmarriage.compat.ModBrewingRecipes;
import com.example.maidmarriage.debug.ModDebugCommands;
import com.example.maidmarriage.init.ModCreativeTabs;
import com.example.maidmarriage.init.ModEffects;
import com.example.maidmarriage.init.ModEntities;
import com.example.maidmarriage.init.ModItems;
import com.example.maidmarriage.init.ModPotions;
import com.example.maidmarriage.network.ModNetworking;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.DistExecutor;

@Mod(MaidMarriageMod.MOD_ID)
/**
 * 模组主入口：负责注册物品、实体与配置界面。
 * 该类的具体逻辑可参见下方方法与字段定义。
 */
public final class MaidMarriageMod {
    public static final String MOD_ID = "maidmarriage";

    public MaidMarriageMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModEffects.MOB_EFFECTS.register(modBus);
        ModPotions.POTIONS.register(modBus);
        ModCreativeTabs.CREATIVE_TABS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        modBus.addListener(ModBrewingRecipes::register);
        ModNetworking.register();
        MinecraftForge.EVENT_BUS.register(ModDebugCommands.class);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigs.SPEC);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientOnlyBootstrap::init);
    }
}
