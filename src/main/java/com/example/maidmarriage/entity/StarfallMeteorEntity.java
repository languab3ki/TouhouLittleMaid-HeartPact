package com.example.maidmarriage.entity;

import com.example.maidmarriage.init.ModEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class StarfallMeteorEntity extends Entity {
    private static final DustParticleOptions BLUE = new DustParticleOptions(new Vector3f(0.13F, 0.55F, 1.0F), 1.45F);
    private static final DustParticleOptions VIOLET = new DustParticleOptions(new Vector3f(0.18F, 0.78F, 0.90F), 1.15F);
    private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.84F, 0.22F), 1.25F);
    private static final int MAX_LIFETIME = 160;

    private Vec3 target = Vec3.ZERO;
    private UUID ownerId;
    private int age;
    private boolean impacted;

    public StarfallMeteorEntity(EntityType<? extends StarfallMeteorEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static StarfallMeteorEntity create(ServerLevel level, Player owner, Vec3 target, BlockPos targetPos) {
        Vec3 start = chooseStart(owner, target);
        StarfallMeteorEntity meteor = new StarfallMeteorEntity(ModEntities.STARFALL_METEOR.get(), level);
        meteor.ownerId = owner.getUUID();
        meteor.target = Vec3.atCenterOf(targetPos).add(0.0D, 0.35D, 0.0D);
        meteor.setPos(start);
        Vec3 velocity = meteor.target.subtract(start).normalize().scale(0.92D);
        meteor.setDeltaMovement(velocity);
        meteor.setYRot((float) (Mth.atan2(velocity.x, velocity.z) * Mth.RAD_TO_DEG));
        meteor.setXRot((float) (Mth.atan2(velocity.y, velocity.horizontalDistance()) * Mth.RAD_TO_DEG));
        return meteor;
    }

    private static Vec3 chooseStart(Player owner, Vec3 target) {
        Vec3 look = owner.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0.0D, look.x).normalize();
        if (side.lengthSqr() < 0.01D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }
        return target.add(side.scale(30.0D)).add(look.scale(-44.0D)).add(0.0D, 78.0D, 0.0D);
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        Vec3 velocity = getDeltaMovement();
        if (!level().isClientSide) {
            if (age > MAX_LIFETIME || position().distanceToSqr(target) < 2.4D || onGround()) {
                impact();
                return;
            }
            Vec3 desired = target.subtract(position()).normalize().scale(1.08D + Math.min(age, 70) * 0.010D);
            setDeltaMovement(velocity.lerp(desired, 0.07D));
            emitSonicBoomRing();
            emitPreImpactGoldRing();
        }

        Vec3 next = position().add(getDeltaMovement());
        setPos(next);
        updateRotation();
        spawnFlightParticles();
    }

    private void updateRotation() {
        Vec3 velocity = getDeltaMovement();
        if (velocity.lengthSqr() < 0.001D) {
            return;
        }
        setYRot((float) (Mth.atan2(velocity.x, velocity.z) * Mth.RAD_TO_DEG));
        setXRot((float) (Mth.atan2(velocity.y, velocity.horizontalDistance()) * Mth.RAD_TO_DEG));
    }

    private void spawnFlightParticles() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 backwards = getDeltaMovement().normalize().reverse();
        Vec3 base = position().add(backwards.scale(0.5D));
        for (int i = 0; i < 10; i++) {
            double spread = 0.12D + random.nextDouble() * 0.14D;
            Vec3 offset = backwards.scale(i * 0.22D + random.nextDouble() * 0.45D)
                    .add(random.nextGaussian() * spread, random.nextGaussian() * spread, random.nextGaussian() * spread);
            DustParticleOptions color = switch (i % 3) {
                case 0 -> BLUE;
                case 1 -> VIOLET;
                default -> GOLD;
            };
            serverLevel.sendParticles(color, base.x + offset.x, base.y + offset.y, base.z + offset.z,
                    1, 0.03D, 0.03D, 0.03D, 0.02D);
        }
        serverLevel.sendParticles(ParticleTypes.FLAME, base.x, base.y, base.z, 2, 0.12D, 0.12D, 0.12D, 0.02D);
        serverLevel.sendParticles(ParticleTypes.SMOKE, base.x, base.y, base.z, 1, 0.08D, 0.08D, 0.08D, 0.01D);
        if (age % 6 == 0) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, base.x, base.y, base.z, 4, 0.18D, 0.10D, 0.18D, 0.02D);
        }
        if (age % 12 == 0) {
            serverLevel.playSound(null, blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.WEATHER,
                    0.35F, 0.55F + random.nextFloat() * 0.2F);
        }
    }

    private void emitSonicBoomRing() {
        if (!(level() instanceof ServerLevel serverLevel) || age % 8 != 0) {
            return;
        }
        Vec3 pos = position();
        for (int i = 0; i < 42; i++) {
            double angle = (Math.PI * 2.0D * i) / 42.0D;
            double radius = 1.2D + (i % 2) * 0.16D;
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;
            DustParticleOptions color = i % 3 == 0 ? GOLD : i % 3 == 1 ? VIOLET : BLUE;
            serverLevel.sendParticles(color, x, pos.y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        serverLevel.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 4, 0.55D, 0.03D, 0.55D, 0.0D);
    }

    private void emitPreImpactGoldRing() {
        if (!(level() instanceof ServerLevel serverLevel) || position().distanceTo(target) > 16.0D || age % 4 != 0) {
            return;
        }
        Vec3 pos = position();
        double baseRadius = 1.2D + (16.0D - position().distanceTo(target)) * 0.18D;
        for (int i = 0; i < 36; i++) {
            double angle = (Math.PI * 2.0D * i) / 36.0D;
            double x = pos.x + Math.cos(angle) * baseRadius;
            double z = pos.z + Math.sin(angle) * baseRadius;
            serverLevel.sendParticles(GOLD, x, pos.y, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
        serverLevel.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 2, 0.35D, 0.05D, 0.35D, 0.0D);
    }

    private void impact() {
        if (impacted || !(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }
        impacted = true;
        Vec3 pos = position();
        serverLevel.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.WEATHER, 1.5F, 0.56F);
        serverLevel.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.WEATHER, 2.2F, 0.48F);
        serverLevel.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.WEATHER, 0.8F, 1.18F);
        serverLevel.sendParticles(ParticleTypes.FLASH, pos.x, pos.y + 0.7D, pos.z, 10, 0.35D, 0.55D, 0.35D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0D, pos.z, 80, 1.7D, 1.2D, 1.7D, 0.14D);
        serverLevel.sendParticles(GOLD, pos.x, pos.y + 0.55D, pos.z, 90, 1.0D, 0.7D, 1.0D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y + 0.35D, pos.z, 6, 0.5D, 0.3D, 0.5D, 0.0D);
        for (int layer = 0; layer < 4; layer++) {
            double ringRadius = 0.55D + layer * 0.78D;
            int count = 48 + layer * 18;
            for (int i = 0; i < count; i++) {
                double angle = (Math.PI * 2.0D * i) / count;
                double x = pos.x + Math.cos(angle) * ringRadius;
                double z = pos.z + Math.sin(angle) * ringRadius;
                DustParticleOptions color = layer < 2 ? GOLD : (i % 2 == 0 ? GOLD : VIOLET);
                serverLevel.sendParticles(color, x, pos.y + 0.16D + layer * 0.06D, z, 1, 0.03D, 0.02D, 0.03D, 0.0D);
                if (i % 5 == 0) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, x, pos.y + 0.45D, z, 1, 0.02D, 0.12D, 0.02D, 0.02D);
                }
            }
        }
        for (int i = 0; i < 42; i++) {
            double angle = (Math.PI * 2.0D * i) / 42.0D;
            double radius = 0.2D + random.nextDouble() * 0.25D;
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;
            serverLevel.sendParticles(GOLD, x, pos.y + 0.28D, z, 1, 0.02D, 0.55D, 0.02D, 0.08D);
        }
        serverLevel.sendParticles(ParticleTypes.LAVA, pos.x, pos.y + 0.2D, pos.z, 24, 0.48D, 0.18D, 0.48D, 0.08D);
        serverLevel.sendParticles(GOLD, pos.x, pos.y + 0.8D, pos.z, 56, 1.6D, 0.8D, 1.6D, 0.0D);
        discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        target = new Vec3(tag.getDouble("TargetX"), tag.getDouble("TargetY"), tag.getDouble("TargetZ"));
        age = tag.getInt("Age");
        if (tag.hasUUID("Owner")) {
            ownerId = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("TargetX", target.x);
        tag.putDouble("TargetY", target.y);
        tag.putDouble("TargetZ", target.z);
        tag.putInt("Age", age);
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
}
