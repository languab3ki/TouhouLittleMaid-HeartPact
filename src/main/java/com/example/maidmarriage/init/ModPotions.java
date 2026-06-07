package com.example.maidmarriage.init;

import com.example.maidmarriage.MaidMarriageMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModPotions {
    private static final int SAFETY_DURATION_TICKS = 5 * 60 * 20;

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, MaidMarriageMod.MOD_ID);

    public static final RegistryObject<Potion> SAFETY = POTIONS.register("safety",
            () -> new Potion("safety", new MobEffectInstance(ModEffects.SAFETY.get(), SAFETY_DURATION_TICKS)));

    private ModPotions() {
    }
}
