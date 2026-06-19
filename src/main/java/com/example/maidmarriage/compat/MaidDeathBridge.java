package com.example.maidmarriage.compat;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.config.DialogueScriptManager;
import com.example.maidmarriage.data.ChildLineageData;
import com.example.maidmarriage.data.ModTaskData;
import com.example.maidmarriage.entity.MaidChildEntity;
import com.example.maidmarriage.entity.MaidSpiritEntity;
import com.example.maidmarriage.util.ComponentJsonUtil;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTombstoneEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = MaidMarriageMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class MaidDeathBridge {
    private static final String SPIRIT_SPAWNED_TAG = "maidmarriage_spirit_spawned";

    private MaidDeathBridge() {
    }

    @SubscribeEvent
    public static void onMaidTombstone(MaidTombstoneEvent event) {
        if (MaidChildEntity.shouldStayChild(event.getMaid())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMaidDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EntityMaid maid) || !(maid.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!isChildSpiritEligible(maid)) {
            return;
        }

        CompoundTag persistent = maid.getPersistentData();
        if (persistent.getBoolean(SPIRIT_SPAWNED_TAG)) {
            return;
        }
        persistent.putBoolean(SPIRIT_SPAWNED_TAG, true);

        MaidSpiritEntity spirit = new MaidSpiritEntity(serverLevel, maid.getX(), maid.getY(), maid.getZ());
        spirit.copyVisualsFrom(maid);
        spirit.setYRot(maid.getYRot());
        spirit.setSpiritData(
                maid.getUUID(),
                maid.getOwnerUUID(),
                findMotherUuid(maid).orElse(null),
                findFatherUuid(maid).orElse(null),
                findGrandParentUuid(maid).orElse(null),
                encodeName(maid).orElse(null),
                MaidChildEntity.resolveGrowthStage(maid)
        );
        spirit.finalizeSpiritAttributes();
        serverLevel.addFreshEntity(spirit);
        findMotherUuid(maid)
                .map(uuid -> findMaid(serverLevel, uuid))
                .ifPresent(mother -> MaidMoodManager.markChildLossGrief(mother, maid.getDisplayName()));
        serverLevel.sendParticles(ParticleTypes.SOUL,
                maid.getX(), maid.getY(1.0D), maid.getZ(),
                16, 0.35D, 0.45D, 0.35D, 0.02D);

        if (maid.getOwner() instanceof Player owner) {
            owner.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                    owner,
                    "message.maidmarriage.child_spirit.appeared",
                    maid.getDisplayName()
            ));
        }
    }

    private static boolean isChildSpiritEligible(EntityMaid maid) {
        return MaidChildEntity.shouldStayChild(maid);
    }

    private static Optional<UUID> findMotherUuid(EntityMaid maid) {
        ChildLineageData lineage = maid.getData(ModTaskData.CHILD_LINEAGE_DATA);
        if (lineage != null && lineage.mother().isPresent()) {
            return lineage.mother();
        }
        CompoundTag persistent = maid.getPersistentData();
        return persistent.hasUUID(MaidChildEntity.PERSISTENT_MOTHER_UUID_KEY)
                ? Optional.of(persistent.getUUID(MaidChildEntity.PERSISTENT_MOTHER_UUID_KEY))
                : Optional.empty();
    }

    private static Optional<UUID> findFatherUuid(EntityMaid maid) {
        ChildLineageData lineage = maid.getData(ModTaskData.CHILD_LINEAGE_DATA);
        if (lineage != null && lineage.father().isPresent()) {
            return lineage.father();
        }
        CompoundTag persistent = maid.getPersistentData();
        return persistent.hasUUID(MaidChildEntity.PERSISTENT_FATHER_UUID_KEY)
                ? Optional.of(persistent.getUUID(MaidChildEntity.PERSISTENT_FATHER_UUID_KEY))
                : Optional.empty();
    }

    private static Optional<UUID> findGrandParentUuid(EntityMaid maid) {
        ChildLineageData lineage = maid.getData(ModTaskData.CHILD_LINEAGE_DATA);
        if (lineage != null && lineage.grandParent().isPresent()) {
            return lineage.grandParent();
        }
        CompoundTag persistent = maid.getPersistentData();
        return persistent.hasUUID(MaidChildEntity.PERSISTENT_GRAND_PARENT_UUID_KEY)
                ? Optional.of(persistent.getUUID(MaidChildEntity.PERSISTENT_GRAND_PARENT_UUID_KEY))
                : Optional.empty();
    }

    private static Optional<String> encodeName(EntityMaid maid) {
        Component name = maid.getCustomName();
        if (name == null) {
            name = maid.getDisplayName();
        }
        return name == null ? Optional.empty() : Optional.of(ComponentJsonUtil.toJson(name, maid.level()));
    }

    private static EntityMaid findMaid(ServerLevel level, UUID maidUuid) {
        if (level == null || maidUuid == null) {
            return null;
        }
        for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
            if (entity instanceof EntityMaid maid && maidUuid.equals(maid.getUUID())) {
                return maid;
            }
        }
        return null;
    }
}

