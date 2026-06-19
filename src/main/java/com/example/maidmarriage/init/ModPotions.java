package com.example.maidmarriage.init;

import com.example.maidmarriage.MaidMarriageMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPotions {
    private static final int SAFETY_DURATION_TICKS = 5 * 60 * 20;

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, MaidMarriageMod.MOD_ID);

    public static final Holder<Potion> SAFETY = POTIONS.register("safety",
            () -> new Potion("safety", new MobEffectInstance(ModEffects.SAFETY, SAFETY_DURATION_TICKS)));

    private ModPotions() {
    }
}
