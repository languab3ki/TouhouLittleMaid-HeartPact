package com.example.maidmarriage.compat;

import com.example.maidmarriage.advancement.ModAdvancements;
import com.example.maidmarriage.config.DialogueScriptManager;
import com.example.maidmarriage.config.ModConfigs;
import com.example.maidmarriage.data.ModTaskData;
import com.example.maidmarriage.entity.MaidChildEntity;
import com.example.maidmarriage.entity.MaidSpiritEntity;
import com.example.maidmarriage.init.ModEntities;
import com.example.maidmarriage.init.ModItems;
import com.example.maidmarriage.network.payload.SpiritInteractionPayload;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 灵体交互的服务端执行层。
 *
 * <p>第一期只提供“安抚”，先把目标识别、权限和反馈链路跑通。
 */
@Mod.EventBusSubscriber(modid = com.example.maidmarriage.MaidMarriageMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpiritInteractionManager {
    private static final String TAG_SOOTHE_COOLDOWN_UNTIL = "maidmarriage_spirit_soothe_cooldown_until";
    private static final String TAG_MEMORY_COOLDOWN_UNTIL = "maidmarriage_spirit_memory_cooldown_until";
    private static final String TAG_BOUND_SPIRIT = "maidmarriage_bound_spirit_uuid";
    private static final String TAG_DAILY_SOOTHE_DAY = "maidmarriage_spirit_daily_soothe_day";
    private static final String TAG_DAILY_SOOTHE_COUNT = "maidmarriage_spirit_daily_soothe_count";
    private static final String TAG_OFFERING_DAY = "maidmarriage_spirit_offering_day";
    private static final String TAG_OFFERING_COUNT = "maidmarriage_spirit_offering_count";
    private static final String TAG_OFFERED_FLOWER_PREFIX = "maidmarriage_spirit_offered_flower_";
    private static final int MAX_DISTANCE = 6;
    private static final double MAX_DISTANCE_SQR = MAX_DISTANCE * MAX_DISTANCE;
    private static final double MOTHER_FAREWELL_DISTANCE = 10.0D;
    private static final double MOTHER_FAREWELL_DISTANCE_SQR = MOTHER_FAREWELL_DISTANCE * MOTHER_FAREWELL_DISTANCE;
    private static final long SOOTHE_COOLDOWN_TICKS = 40L;
    private static final long MEMORY_COOLDOWN_TICKS = 40L;
    private static final int DAILY_SOOTHE_LIMIT = 3;
    private static final int DAILY_OFFERING_LIMIT = 3;
    private static final int RESURRECTION_REQUIRED_LONGING = 100;

    private SpiritInteractionManager() {
    }

    public static void handleAction(ServerPlayer player, UUID spiritUuid, String actionId) {
        if (player == null || spiritUuid == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Entity entity = level.getEntity(spiritUuid);
        if (!(entity instanceof MaidSpiritEntity spirit)) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.no_target"));
            return;
        }
        if (spirit.distanceToSqr(player) > MAX_DISTANCE_SQR) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                    player,
                    "message.maidmarriage.spirit.too_far",
                    spirit.getDisplayName(),
                    MAX_DISTANCE
            ));
            return;
        }
        UUID ownerUuid = spirit.getOwnerUuid();
        UUID grandParentUuid = spirit.getGrandParentUuid();
        boolean allowed = player.getUUID().equals(ownerUuid) || player.getUUID().equals(grandParentUuid);
        if (!allowed) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                    player,
                    "message.maidmarriage.spirit.need_family",
                    spirit.getDisplayName()
            ));
            return;
        }
        if (SpiritInteractionPayload.ACTION_SOOTHE.equals(actionId)) {
            soothe(player, level, spirit);
            return;
        }
        if (SpiritInteractionPayload.ACTION_REMEMBER.equals(actionId)) {
            remember(player, level, spirit);
            return;
        }
        if (SpiritInteractionPayload.ACTION_STAY.equals(actionId)) {
            chooseStay(player, level, spirit);
            return;
        }
        if (SpiritInteractionPayload.ACTION_FAREWELL.equals(actionId)) {
            farewell(player, level, spirit);
            return;
        }
        if (SpiritInteractionPayload.ACTION_DAILY_SOOTHE.equals(actionId)) {
            dailySoothe(player, level, spirit);
        }
    }

    private static void soothe(ServerPlayer player, ServerLevel level, MaidSpiritEntity spirit) {
        long now = level.getGameTime();
        if (player.getPersistentData().getLong(TAG_SOOTHE_COOLDOWN_UNTIL) > now) {
            return;
        }
        player.getPersistentData().putLong(TAG_SOOTHE_COOLDOWN_UNTIL, now + SOOTHE_COOLDOWN_TICKS);
        boolean advanced = spirit.advanceSoothe();

        spirit.getLookControl().setLookAt(player, 20.0F, 20.0F);
        level.sendParticles(ParticleTypes.SOUL,
                spirit.getX(), spirit.getY(0.85D), spirit.getZ(),
                8, 0.22D, 0.28D, 0.22D, 0.01D);
        level.sendParticles(ParticleTypes.HEART,
                spirit.getX(), spirit.getY(1.05D), spirit.getZ(),
                2, 0.18D, 0.14D, 0.18D, 0.0D);
        level.playSound(null, spirit.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.55F, 1.35F);
        player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                player,
                advanced ? "message.maidmarriage.spirit.soothe.success" : "message.maidmarriage.spirit.soothe.max",
                spirit.getDisplayName(),
                spirit.getLonging()
        ));
        if (advanced) {
            ModAdvancements.grantSpiritSoothe(player);
        }
    }

    private static void remember(ServerPlayer player, ServerLevel level, MaidSpiritEntity spirit) {
        long now = level.getGameTime();
        if (player.getPersistentData().getLong(TAG_MEMORY_COOLDOWN_UNTIL) > now) {
            return;
        }
        if (spirit.getSootheCount() < MaidSpiritEntity.MAX_SOOTHE_COUNT) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.memory.locked"));
            return;
        }
        player.getPersistentData().putLong(TAG_MEMORY_COOLDOWN_UNTIL, now + MEMORY_COOLDOWN_TICKS);
        boolean recognizedBefore = spirit.hasRecognizedFather();
        boolean advanced = spirit.advanceMemory();
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                spirit.getX(), spirit.getY(0.9D), spirit.getZ(),
                6, 0.2D, 0.22D, 0.2D, 0.01D);
        level.playSound(null, spirit.blockPosition(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.45F, 1.15F);
        player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                player,
                advanced ? "message.maidmarriage.spirit.memory.success" : "message.maidmarriage.spirit.memory.max",
                spirit.getDisplayName(),
                spirit.getLonging()
        ));
        if (advanced) {
            ModAdvancements.grantSpiritRemember(player);
        }
        if (!recognizedBefore && spirit.hasRecognizedFather()) {
            ModAdvancements.grantSpiritRecognized(player);
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.recognized", spirit.getDisplayName()));
        }
    }

    public static void tryBindSoulLantern(Player rawPlayer, MaidSpiritEntity spirit) {
        if (!(rawPlayer instanceof ServerPlayer player) || spirit == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (spirit.isFarewell()) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.lantern.farewell"));
            return;
        }
        if (!isFamily(player, spirit)) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.need_family", spirit.getDisplayName()));
            return;
        }
        if (!spirit.isLanternReady()) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                    player,
                    "message.maidmarriage.spirit.lantern.not_ready",
                    spirit.getDisplayName(),
                    MaidSpiritEntity.LANTERN_READY_LONGING
            ));
            return;
        }
        player.getPersistentData().putUUID(TAG_BOUND_SPIRIT, spirit.getUUID());
        spirit.bindSoulLanternTo(player);
        ModAdvancements.grantSpiritLantern(player);
        player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.lantern.bound", spirit.getDisplayName()));
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()
                || !(event.getTarget() instanceof MaidSpiritEntity spirit)) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.is(Items.SOUL_LANTERN)) {
            tryBindSoulLantern(event.getEntity(), spirit);
        } else if (stack.is(Items.SOUL_TORCH)) {
            exorciseWithSoulTorch(event.getEntity(), spirit, stack);
        } else if (stack.is(ModItems.LONGING_TESTER.get())) {
            fillLongingForTest(event.getEntity(), spirit);
        } else if (stack.is(Items.TOTEM_OF_UNDYING)) {
            tryResurrectWithTotem(event.getEntity(), spirit, stack);
        } else {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide() || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (!player.getPersistentData().hasUUID(TAG_BOUND_SPIRIT)) {
            return;
        }
        UUID spiritUuid = player.getPersistentData().getUUID(TAG_BOUND_SPIRIT);
        Entity entity = player.serverLevel().getEntity(spiritUuid);
        if (!(entity instanceof MaidSpiritEntity spirit) || !spirit.isAlive() || spirit.isFarewell()) {
            player.getPersistentData().remove(TAG_BOUND_SPIRIT);
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.lantern.lost"));
            return;
        }
        if (!hasSoulLantern(player)) {
            clearBoundSpirit(player, spirit, "message.maidmarriage.spirit.lantern.stopped");
        }
    }

    private static boolean hasSoulLantern(ServerPlayer player) {
        return player.getMainHandItem().is(Items.SOUL_LANTERN)
                || player.getOffhandItem().is(Items.SOUL_LANTERN);
    }

    private static void clearBoundSpirit(ServerPlayer player, MaidSpiritEntity spirit, String messageKey) {
        player.getPersistentData().remove(TAG_BOUND_SPIRIT);
        if (spirit != null) {
            spirit.setLanternBound(false);
        }
        player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, messageKey));
    }

    private static void fillLongingForTest(Player rawPlayer, MaidSpiritEntity spirit) {
        if (!(rawPlayer instanceof ServerPlayer player) || spirit == null) {
            return;
        }
        if (!isFamily(player, spirit)) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.need_family", spirit.getDisplayName()));
            return;
        }
        addLongingAndNotifyReady(player, spirit, RESURRECTION_REQUIRED_LONGING);
        player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                player,
                "message.maidmarriage.spirit.longing_tester.success",
                spirit.getDisplayName(),
                spirit.getLonging()
        ));
    }

    private static void exorciseWithSoulTorch(Player rawPlayer, MaidSpiritEntity spirit, ItemStack stack) {
        if (!(rawPlayer instanceof ServerPlayer player) || spirit == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!isFamily(player, spirit)) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.need_family", spirit.getDisplayName()));
            return;
        }
        Component spiritName = spirit.getDisplayName();
        clearSpirit(level, spirit, true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                spirit.getX(), spirit.getY(0.8D), spirit.getZ(),
                24, 0.3D, 0.35D, 0.3D, 0.02D);
        level.playSound(null, spirit.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.6F, 0.75F);
        player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.exorcise.success", spiritName));
    }

    private static void tryResurrectWithTotem(Player rawPlayer, MaidSpiritEntity spirit, ItemStack stack) {
        if (!(rawPlayer instanceof ServerPlayer player) || spirit == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!isFamily(player, spirit)) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.need_family", spirit.getDisplayName()));
            return;
        }
        if (spirit.isFarewell()) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.resurrection.farewell"));
            return;
        }
        if (!spirit.isStaying()) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.resurrection.need_stay", spirit.getDisplayName()));
            return;
        }
        if (spirit.getLonging() < RESURRECTION_REQUIRED_LONGING) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                    player,
                    "message.maidmarriage.spirit.resurrection.need_longing",
                    spirit.getDisplayName(),
                    RESURRECTION_REQUIRED_LONGING
            ));
            return;
        }

        MaidChildEntity child = resurrectSpirit(player, level, spirit);
        if (child == null) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.resurrection.failed"));
            return;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                child.getX(), child.getY(0.8D), child.getZ(),
                48, 0.45D, 0.55D, 0.45D, 0.03D);
        level.playSound(null, child.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.05F);
        player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                player,
                "message.maidmarriage.spirit.resurrection.success",
                child.getDisplayName()
        ));
    }

    public static boolean forceClearSpirit(ServerPlayer player, MaidSpiritEntity spirit) {
        if (player == null || spirit == null || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        clearSpirit(level, spirit, true);
        return true;
    }

    public static boolean forceReviveSpirit(ServerPlayer player, MaidSpiritEntity spirit) {
        if (player == null || spirit == null || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        return resurrectSpirit(player, level, spirit) != null;
    }

    private static MaidChildEntity resurrectSpirit(ServerPlayer player, ServerLevel level, MaidSpiritEntity spirit) {
        MaidChildEntity child = ModEntities.MAID_CHILD.get().create(level);
        if (child == null) {
            return null;
        }

        child.moveTo(spirit.getX(), spirit.getY(), spirit.getZ(), spirit.getYRot(), spirit.getXRot());
        child.tame(player);
        child.setCustomName(spirit.getName().copy());
        child.setCustomNameVisible(spirit.isCustomNameVisible());
        child.setParents(spirit.getMotherUuid(), resolveFatherUuid(player, spirit));
        child.applyBornMaidTraits();
        child.prepareNewbornInfant(level.getGameTime());
        child.inheritModelFromMother(spirit);
        child.setSoundPackId(spirit.getSoundPackId());
        copySpiritPersistentIdentity(spirit, child);

        if (!level.addFreshEntity(child)) {
            return null;
        }
        child.getSchedulePos().setHomeModeEnable(child, child.blockPosition());
        child.setHomeModeEnable(true);
        child.syncChildStateToClient();

        if (ModConfigs.recruitAnimationEnabled()) {
            StarfallEffectSpawner.spawnResurrectionEffect(level, child.position());
        }
        clearSpirit(level, spirit, true);
        return child;
    }

    private static void chooseStay(ServerPlayer player, ServerLevel level, MaidSpiritEntity spirit) {
        if (!spirit.hasRecognizedFather()) {
            return;
        }
        if (findNearbyMother(level, spirit, MOTHER_FAREWELL_DISTANCE_SQR) == null) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                    player,
                    "message.maidmarriage.spirit.stay.need_mother",
                    spirit.getDisplayName(),
                    (int) MOTHER_FAREWELL_DISTANCE
            ));
            return;
        }
        spirit.markStaying();
        addLongingAndNotifyReady(player, spirit, 3);
        ModAdvancements.grantSpiritStay(player);
        level.sendParticles(ParticleTypes.HEART, spirit.getX(), spirit.getY(1.0D), spirit.getZ(), 6, 0.22D, 0.18D, 0.22D, 0.0D);
    }

    private static void dailySoothe(ServerPlayer player, ServerLevel level, MaidSpiritEntity spirit) {
        if (!spirit.isStaying() || spirit.isFarewell()) {
            return;
        }
        int gained = tryConsumeDailySoothe(spirit, level);
        if (gained <= 0) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                    player,
                    "message.maidmarriage.spirit.daily_soothe.limit",
                    spirit.getDisplayName(),
                    DAILY_SOOTHE_LIMIT
            ));
            return;
        }
        addLongingAndNotifyReady(player, spirit, gained);
        level.sendParticles(ParticleTypes.HEART, spirit.getX(), spirit.getY(1.0D), spirit.getZ(), 4, 0.18D, 0.16D, 0.18D, 0.0D);
        level.playSound(null, spirit.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.35F, 1.45F);
        player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                player,
                "message.maidmarriage.spirit.daily_soothe.success",
                spirit.getDisplayName(),
                gained,
                spirit.getLonging()
        ));
    }

    public static void handleOffering(ServerPlayer player, @Nullable UUID spiritUuid, int slotIndex) {
        if (player == null || spiritUuid == null || slotIndex < 0 || slotIndex >= player.getInventory().items.size()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Entity entity = level.getEntity(spiritUuid);
        if (!(entity instanceof MaidSpiritEntity spirit)) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.no_target"));
            return;
        }
        if (spirit.distanceToSqr(player) > MAX_DISTANCE_SQR) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.too_far", spirit.getDisplayName(), MAX_DISTANCE));
            return;
        }
        if (!isFamily(player, spirit)) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.need_family", spirit.getDisplayName()));
            return;
        }
        if (!spirit.isStaying() || spirit.isFarewell()) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.offering.locked", spirit.getDisplayName()));
            return;
        }
        ItemStack stack = player.getInventory().items.get(slotIndex);
        SpiritOfferingRules.OfferingCategory category = SpiritOfferingRules.classify(stack);
        if (category != SpiritOfferingRules.OfferingCategory.FLOWER && category != SpiritOfferingRules.OfferingCategory.SOUL) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.offering.invalid"));
            return;
        }
        if (!canOfferToday(spirit, level)) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.offering.limit", DAILY_OFFERING_LIMIT));
            return;
        }

        int gained = category == SpiritOfferingRules.OfferingCategory.SOUL ? 2 : resolveFlowerOfferingGain(spirit, stack);
        addLongingAndNotifyReady(player, spirit, gained);
        recordOffering(spirit, level);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            player.getInventory().setChanged();
            syncInventorySlot(player, slotIndex, stack);
        }
        level.sendParticles(category == SpiritOfferingRules.OfferingCategory.SOUL ? ParticleTypes.SOUL : ParticleTypes.HEART,
                spirit.getX(), spirit.getY(1.0D), spirit.getZ(), 8, 0.22D, 0.24D, 0.22D, 0.01D);
        level.playSound(null, spirit.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.45F, 1.2F);
        player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                player,
                category == SpiritOfferingRules.OfferingCategory.SOUL
                        ? "message.maidmarriage.spirit.offering.soul"
                        : "message.maidmarriage.spirit.offering.flower",
                spirit.getDisplayName(),
                gained,
                spirit.getLonging()
        ));
    }

    private static void farewell(ServerPlayer player, ServerLevel level, MaidSpiritEntity spirit) {
        if (!spirit.hasRecognizedFather() || spirit.isStaying()) {
            return;
        }
        EntityMaid mother = findNearbyMother(level, spirit, MOTHER_FAREWELL_DISTANCE_SQR);
        if (mother == null) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                    player,
                    "message.maidmarriage.spirit.farewell.need_mother",
                    spirit.getDisplayName(),
                    (int) MOTHER_FAREWELL_DISTANCE
            ));
            return;
        }
        spirit.markFarewell();
        MaidMoodManager.clearChildLossGrief(mother);
        ModAdvancements.grantSpiritFarewell(player);
        giveFarewellLetter(player, spirit);
        level.sendParticles(ParticleTypes.SOUL, spirit.getX(), spirit.getY(1.0D), spirit.getZ(), 24, 0.35D, 0.4D, 0.35D, 0.01D);
        player.sendSystemMessage(DialogueScriptManager.componentForPlayer(player, "message.maidmarriage.spirit.farewell.letter"));
        player.getPersistentData().remove(TAG_BOUND_SPIRIT);
        spirit.discard();
    }

    private static boolean isFamily(ServerPlayer player, MaidSpiritEntity spirit) {
        UUID ownerUuid = spirit.getOwnerUuid();
        UUID grandParentUuid = spirit.getGrandParentUuid();
        return player.getUUID().equals(ownerUuid) || player.getUUID().equals(grandParentUuid);
    }

    private static UUID resolveFatherUuid(ServerPlayer player, MaidSpiritEntity spirit) {
        UUID fatherUuid = spirit.getFatherUuid();
        if (fatherUuid != null) {
            return fatherUuid;
        }
        UUID ownerUuid = spirit.getOwnerUuid();
        return ownerUuid == null ? player.getUUID() : ownerUuid;
    }

    private static void copySpiritPersistentIdentity(MaidSpiritEntity spirit, MaidChildEntity child) {
        CompoundTag source = spirit.getPersistentData();
        CompoundTag target = child.getPersistentData();
        copyUuid(source, target, MaidChildEntity.PERSISTENT_MOTHER_UUID_KEY);
        copyUuid(source, target, MaidChildEntity.PERSISTENT_FATHER_UUID_KEY);
        copyUuid(source, target, MaidChildEntity.PERSISTENT_GRAND_PARENT_UUID_KEY);
        target.putBoolean(MaidChildEntity.BORN_MAID_TAG, true);
        target.putBoolean(MaidChildEntity.PERSISTENT_CHILD_ACTIVE_KEY, true);
        target.putInt(MaidChildEntity.PERSISTENT_GROWTH_TICKS_KEY, 0);
        target.putString(MaidChildEntity.PERSISTENT_GROWTH_STAGE_KEY, MaidChildEntity.GrowthStage.INFANT.name());
        target.putBoolean(MaidChildEntity.PERSISTENT_TAME_INITIALIZED_KEY, child.isTame() && child.getOwnerUUID() != null);
        target.putBoolean(MaidChildEntity.PERSISTENT_CHILD_NAME_CONFIRMED_KEY, true);
        String nameJson = Component.Serializer.toJson(child.getName());
        target.putString(MaidChildEntity.PERSISTENT_CHILD_NAME_JSON_KEY, nameJson);
        child.addTag(MaidChildEntity.BORN_MAID_TAG);
    }

    private static void copyUuid(CompoundTag source, CompoundTag target, String key) {
        if (source.hasUUID(key)) {
            target.putUUID(key, source.getUUID(key));
        }
    }

    private static void addLongingAndNotifyReady(ServerPlayer player, MaidSpiritEntity spirit, int amount) {
        int before = spirit.getLonging();
        spirit.addLonging(amount);
        if (before < RESURRECTION_REQUIRED_LONGING
                && spirit.getLonging() >= RESURRECTION_REQUIRED_LONGING
                && spirit.isStaying()
                && !spirit.isFarewell()) {
            player.sendSystemMessage(DialogueScriptManager.componentForPlayer(
                    player,
                    "message.maidmarriage.spirit.resurrection.ready",
                    spirit.getDisplayName()
            ));
        }
    }

    private static void clearMotherGrief(ServerLevel level, MaidSpiritEntity spirit) {
        EntityMaid mother = findMother(level, spirit);
        if (mother != null) {
            MaidMoodManager.clearChildLossGrief(mother);
        }
    }

    private static void clearSpirit(ServerLevel level, MaidSpiritEntity spirit, boolean clearGrief) {
        if (level == null || spirit == null) {
            return;
        }
        UUID spiritUuid = spirit.getUUID();
        if (clearGrief) {
            clearMotherGrief(level, spirit);
        }
        for (ServerPlayer serverPlayer : level.players()) {
            if (serverPlayer.getPersistentData().hasUUID(TAG_BOUND_SPIRIT)
                    && spiritUuid.equals(serverPlayer.getPersistentData().getUUID(TAG_BOUND_SPIRIT))) {
                serverPlayer.getPersistentData().remove(TAG_BOUND_SPIRIT);
            }
        }
        spirit.setLanternBound(false);
        spirit.discard();
    }

    private static EntityMaid findMother(ServerLevel level, MaidSpiritEntity spirit) {
        UUID motherUuid = spirit.getMotherUuid();
        if (motherUuid == null) {
            return null;
        }
        Entity entity = level.getEntity(motherUuid);
        return entity instanceof EntityMaid mother ? mother : null;
    }

    private static EntityMaid findNearbyMother(ServerLevel level, MaidSpiritEntity spirit, double maxDistanceSqr) {
        EntityMaid mother = findMother(level, spirit);
        if (mother == null || mother.distanceToSqr(spirit) > maxDistanceSqr) {
            return null;
        }
        return mother;
    }

    private static void giveFarewellLetter(ServerPlayer player, MaidSpiritEntity spirit) {
        ItemStack book = new ItemStack(net.minecraft.world.item.Items.WRITTEN_BOOK);
        net.minecraft.nbt.CompoundTag tag = book.getOrCreateTag();
        tag.putString("author", spirit.getName().getString());
        tag.putString("title", DialogueScriptManager.componentForPlayer(player, "item.maidmarriage.spirit_farewell_letter.title").getString());
        net.minecraft.nbt.ListTag pages = new net.minecraft.nbt.ListTag();
        pages.add(net.minecraft.nbt.StringTag.valueOf(net.minecraft.network.chat.Component.Serializer.toJson(
                DialogueScriptManager.componentForPlayer(player, "item.maidmarriage.spirit_farewell_letter.page1", spirit.getDisplayName()))));
        pages.add(net.minecraft.nbt.StringTag.valueOf(net.minecraft.network.chat.Component.Serializer.toJson(
                DialogueScriptManager.componentForPlayer(player, "item.maidmarriage.spirit_farewell_letter.page2"))));
        tag.put("pages", pages);
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
    }

    private static int tryConsumeDailySoothe(MaidSpiritEntity spirit, ServerLevel level) {
        CompoundTag tag = spirit.getPersistentData();
        long today = level.getGameTime() / 24000L;
        long recordedDay = tag.getLong(TAG_DAILY_SOOTHE_DAY);
        if (recordedDay != today) {
            tag.putLong(TAG_DAILY_SOOTHE_DAY, today);
            tag.putInt(TAG_DAILY_SOOTHE_COUNT, 0);
        }
        int count = Math.max(0, tag.getInt(TAG_DAILY_SOOTHE_COUNT));
        if (count >= DAILY_SOOTHE_LIMIT) {
            return 0;
        }
        tag.putInt(TAG_DAILY_SOOTHE_COUNT, count + 1);
        return 2;
    }

    private static boolean canOfferToday(MaidSpiritEntity spirit, ServerLevel level) {
        CompoundTag tag = spirit.getPersistentData();
        long today = level.getGameTime() / 24000L;
        long recordedDay = tag.getLong(TAG_OFFERING_DAY);
        if (recordedDay != today) {
            tag.putLong(TAG_OFFERING_DAY, today);
            tag.putInt(TAG_OFFERING_COUNT, 0);
            return true;
        }
        return Math.max(0, tag.getInt(TAG_OFFERING_COUNT)) < DAILY_OFFERING_LIMIT;
    }

    private static void recordOffering(MaidSpiritEntity spirit, ServerLevel level) {
        CompoundTag tag = spirit.getPersistentData();
        long today = level.getGameTime() / 24000L;
        long recordedDay = tag.getLong(TAG_OFFERING_DAY);
        if (recordedDay != today) {
            tag.putLong(TAG_OFFERING_DAY, today);
            tag.putInt(TAG_OFFERING_COUNT, 0);
        }
        tag.putInt(TAG_OFFERING_COUNT, Math.min(DAILY_OFFERING_LIMIT, Math.max(0, tag.getInt(TAG_OFFERING_COUNT)) + 1));
    }

    private static int resolveFlowerOfferingGain(MaidSpiritEntity spirit, ItemStack stack) {
        CompoundTag tag = spirit.getPersistentData();
        String itemKey = SpiritOfferingRules.itemKey(stack);
        if (itemKey.isBlank()) {
            return 1;
        }
        String safeKey = itemKey.replace(':', '_').replace('/', '_');
        String tagKey = TAG_OFFERED_FLOWER_PREFIX + safeKey;
        boolean first = !tag.getBoolean(tagKey);
        tag.putBoolean(tagKey, true);
        return first ? 3 : 1;
    }

    private static void syncInventorySlot(ServerPlayer player, int slotIndex, ItemStack stack) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
        player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, slotIndex, stack.copy()));
    }
}
