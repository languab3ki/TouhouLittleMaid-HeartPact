package com.example.maidmarriage.entity;

import com.example.maidmarriage.init.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class StarfallMagicCircleEntity extends Entity {
    public static final int DEFAULT_IMPACT_TICK = 56;
    private static final int LIFETIME = 126;

    private int age;
    private int impactTick = DEFAULT_IMPACT_TICK;
    private boolean preImpactRingPlayed;

    public StarfallMagicCircleEntity(EntityType<? extends StarfallMagicCircleEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static StarfallMagicCircleEntity create(ServerLevel level, Vec3 target, int impactTick) {
        StarfallMagicCircleEntity circle = new StarfallMagicCircleEntity(ModEntities.STARFALL_MAGIC_CIRCLE.get(), level);
        circle.impactTick = Math.max(12, impactTick);
        circle.setPos(target.x, target.y + 0.035D, target.z);
        return circle;
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (!level().isClientSide && age == impactTick) {
            level().playSound(null, blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.WEATHER, 1.6F, 0.72F);
        }
        if (!level().isClientSide && age >= impactTick - 12 && !preImpactRingPlayed) {
            preImpactRingPlayed = true;
            level().playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.WEATHER, 1.2F, 1.35F);
        }
        if (!level().isClientSide && age > LIFETIME) {
            discard();
        }
    }

    public float age(float partialTick) {
        return age + partialTick;
    }

    public int impactTick() {
        return impactTick;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt("Age");
        impactTick = tag.contains("ImpactTick") ? tag.getInt("ImpactTick") : DEFAULT_IMPACT_TICK;
        preImpactRingPlayed = tag.getBoolean("PreImpactRingPlayed");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", age);
        tag.putInt("ImpactTick", impactTick);
        tag.putBoolean("PreImpactRingPlayed", preImpactRingPlayed);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
}
