package com.example.maidmarriage.init;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.effect.SafetyMobEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, MaidMarriageMod.MOD_ID);

    public static final Holder<MobEffect> SAFETY = MOB_EFFECTS.register("safety", SafetyMobEffect::new);

    private ModEffects() {
    }
}
