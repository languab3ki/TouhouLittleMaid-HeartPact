package com.example.maidmarriage.compat;

import com.example.maidmarriage.entity.StarfallMagicCircleEntity;
import com.example.maidmarriage.entity.StarfallMeteorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class StarfallEffectSpawner {
    private StarfallEffectSpawner() {
    }

    public static void spawnBirthEffect(ServerLevel level, Player owner, Vec3 target) {
        Vec3 groundTarget = resolveGroundTarget(level, target);
        spawnMagicCircle(level, groundTarget, StarfallMagicCircleEntity.DEFAULT_IMPACT_TICK);
        if (owner != null) {
            BlockPos impactPos = BlockPos.containing(groundTarget);
            StarfallMeteorEntity meteor = StarfallMeteorEntity.create(level, owner, groundTarget, impactPos);
            level.addFreshEntity(meteor);
            level.playSound(null, owner.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.45F);
            level.playSound(null, impactPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.WEATHER, 1.4F, 0.55F);
        }
    }

    public static void spawnResurrectionEffect(ServerLevel level, Vec3 target) {
        Vec3 groundTarget = resolveGroundTarget(level, target);
        spawnMagicCircle(level, groundTarget, 18);
        level.playSound(null, BlockPos.containing(groundTarget), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.65F, 1.65F);
    }

    private static void spawnMagicCircle(ServerLevel level, Vec3 target, int impactTick) {
        if (level == null || target == null) {
            return;
        }
        StarfallMagicCircleEntity circle = StarfallMagicCircleEntity.create(level, target, impactTick);
        level.addFreshEntity(circle);
    }

    private static Vec3 resolveGroundTarget(ServerLevel level, Vec3 target) {
        if (level == null || target == null) {
            return target;
        }
        BlockPos.MutableBlockPos pos = BlockPos.containing(target).mutable();
        int minY = level.getMinBuildHeight();
        while (pos.getY() > minY && level.getBlockState(pos.below()).isAir()) {
            pos.move(Direction.DOWN);
        }
        return new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
    }
}
