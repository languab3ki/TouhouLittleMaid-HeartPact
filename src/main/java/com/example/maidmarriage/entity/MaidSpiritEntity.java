package com.example.maidmarriage.entity;

import com.example.maidmarriage.init.ModEntities;
import com.example.maidmarriage.data.ChildLineageData;
import com.example.maidmarriage.data.ChildStateData;
import com.example.maidmarriage.data.ModTaskData;
import com.example.maidmarriage.util.ComponentJsonUtil;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Phase-one spirit left behind by a born/child maid.
 *
 * <p>It deliberately extends {@link EntityMaid} so the client can reuse the original maid renderer
 * and keep model/YSM/Gecko appearance consistent. The spirit disables gameplay behaviour and only
 * keeps the visual identity plus family metadata.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MaidSpiritEntity extends EntityMaid {
    private static final double FOLLOW_TELEPORT_DISTANCE_SQR = 40.0D * 40.0D;
    private static final double FOLLOW_TELEPORT_MAX_DISTANCE = 10.0D;
    private static final double FOLLOW_TELEPORT_MAX_DISTANCE_SQR = FOLLOW_TELEPORT_MAX_DISTANCE * FOLLOW_TELEPORT_MAX_DISTANCE;
    private static final int FOLLOW_TELEPORT_SEARCH_RADIUS = 10;
    private static final double FOLLOW_BACK_OFFSET = 2.45D;
    private static final double FOLLOW_SIDE_OFFSET = 1.15D;
    private static final double FOLLOW_HEIGHT_OFFSET = 0.45D;
    private static final double FOLLOW_STOP_DISTANCE_SQR = 5.0D * 5.0D;
    private static final double FOLLOW_MAX_STEP_PER_TICK = 0.22D;
    private static final double FOLLOW_DIRECTION_UPDATE_MIN_SPEED_SQR = 0.015D * 0.015D;
    private static final float FOLLOW_MAX_YAW_STEP = 8.0F;

    private static final EntityDataAccessor<Optional<UUID>> DATA_ORIGINAL_MAID_UUID =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_MOTHER_UUID =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_FATHER_UUID =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_GRAND_PARENT_UUID =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> DATA_NAME_JSON =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_GROWTH_STAGE =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_LONGING =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SOOTHE_COUNT =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MEMORY_COUNT =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_RECOGNIZED_FATHER =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_LANTERN_BOUND =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_STAYING =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FAREWELL =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_RENDER_ANCHOR_X =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_RENDER_ANCHOR_Y =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_RENDER_ANCHOR_Z =
            SynchedEntityData.defineId(MaidSpiritEntity.class, EntityDataSerializers.FLOAT);

    private static final String TAG_ORIGINAL_MAID_UUID = "OriginalMaidUuid";
    private static final String TAG_OWNER_UUID = "OwnerUuid";
    private static final String TAG_MOTHER_UUID = "MotherUuid";
    private static final String TAG_FATHER_UUID = "FatherUuid";
    private static final String TAG_GRAND_PARENT_UUID = "GrandParentUuid";
    private static final String TAG_NAME_JSON = "NameJson";
    private static final String TAG_GROWTH_STAGE = "GrowthStage";
    private static final String TAG_LONGING = "Longing";
    private static final String TAG_SOOTHE_COUNT = "SootheCount";
    private static final String TAG_MEMORY_COUNT = "MemoryCount";
    private static final String TAG_RECOGNIZED_FATHER = "RecognizedFather";
    private static final String TAG_LANTERN_BOUND = "LanternBound";
    private static final String TAG_LANTERN_BIND_PLAYER_UUID = "LanternBindPlayerUuid";
    private static final String TAG_STAYING = "Staying";
    private static final String TAG_FAREWELL = "Farewell";

    public static final int MAX_SOOTHE_COUNT = 5;
    public static final int MAX_MEMORY_COUNT = 5;
    public static final int LANTERN_READY_LONGING = 10;

    @Nullable
    private UUID motherUuid;
    @Nullable
    private UUID fatherUuid;
    @Nullable
    private UUID grandParentUuid;
    @Nullable
    private UUID lanternBindPlayerUuid;
    private double baseY;
    private double followForwardX;
    private double followForwardZ;

    public MaidSpiritEntity(EntityType<? extends MaidSpiritEntity> type, Level level) {
        super((EntityType<EntityMaid>) (EntityType) type, level);
        this.noPhysics = true;
        this.blocksBuilding = false;
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setSilent(true);
    }

    public MaidSpiritEntity(Level level, double x, double y, double z) {
        this(ModEntities.MAID_SPIRIT.get(), level);
        this.moveTo(x, y, z);
        this.baseY = y;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ORIGINAL_MAID_UUID, Optional.empty());
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_MOTHER_UUID, Optional.empty());
        builder.define(DATA_FATHER_UUID, Optional.empty());
        builder.define(DATA_GRAND_PARENT_UUID, Optional.empty());
        builder.define(DATA_NAME_JSON, "");
        builder.define(DATA_GROWTH_STAGE, MaidChildEntity.GrowthStage.ADULT.name());
        builder.define(DATA_LONGING, 0);
        builder.define(DATA_SOOTHE_COUNT, 0);
        builder.define(DATA_MEMORY_COUNT, 0);
        builder.define(DATA_RECOGNIZED_FATHER, false);
        builder.define(DATA_LANTERN_BOUND, false);
        builder.define(DATA_STAYING, false);
        builder.define(DATA_FAREWELL, false);
        builder.define(DATA_RENDER_ANCHOR_X, Float.NaN);
        builder.define(DATA_RENDER_ANCHOR_Y, Float.NaN);
        builder.define(DATA_RENDER_ANCHOR_Z, Float.NaN);
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            super.tick();
            keepSpiritPassiveState();
            return;
        }
        super.baseTick();
        keepSpiritPassiveState();
        this.setHealth(Math.max(1.0F, this.getHealth()));
        if (!level().isClientSide && isLanternBound()) {
            tickLanternFollow();
        }
        if (this.baseY == 0.0D || !isLanternBound()) {
            this.baseY = this.getY();
        }
        if (!level().isClientSide && this.tickCount % 40 == 0 && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                    getX(), getY(0.75D), getZ(), 1, 0.15D, 0.25D, 0.15D, 0.0D);
        }
    }

    private void keepSpiritPassiveState() {
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setNoAi(true);
        this.setPose(Pose.STANDING);
        this.setDeltaMovement(Vec3.ZERO);
    }

    public void copyVisualsFrom(EntityMaid maid) {
        setIsYsmModel(false);
        setModelId(resolveSupportedModelId(maid));
        setSoundPackId(maid.getSoundPackId());
        if (maid.hasCustomName()) {
            setCustomName(maid.getCustomName());
        }
        setCustomNameVisible(maid.isCustomNameVisible());
        setYRot(maid.getYRot());
        setYHeadRot(maid.getYHeadRot());
        setYBodyRot(maid.yBodyRot);
        yBodyRotO = maid.yBodyRotO;
        yHeadRotO = maid.yHeadRotO;
        yRotO = maid.yRotO;
        xRotO = maid.xRotO;
        setXRot(maid.getXRot());
        getPersistentData().putString(EntityMaid.MODEL_ID_TAG, getModelId());
        getPersistentData().putBoolean(EntityMaid.IS_YSM_MODEL_TAG, false);
        copyFamilyPersistentData(maid);
        copyChildLifecycleData(maid);
    }

    public void setSpiritData(@Nullable UUID originalMaidUuid,
                              @Nullable UUID ownerUuid,
                              @Nullable UUID motherUuid,
                              @Nullable UUID fatherUuid,
                              @Nullable UUID grandParentUuid,
                              @Nullable String nameJson,
                              MaidChildEntity.GrowthStage growthStage) {
        this.entityData.set(DATA_ORIGINAL_MAID_UUID, Optional.ofNullable(originalMaidUuid));
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(ownerUuid));
        this.motherUuid = motherUuid;
        this.fatherUuid = fatherUuid;
        this.grandParentUuid = grandParentUuid;
        this.entityData.set(DATA_MOTHER_UUID, Optional.ofNullable(motherUuid));
        this.entityData.set(DATA_FATHER_UUID, Optional.ofNullable(fatherUuid));
        this.entityData.set(DATA_GRAND_PARENT_UUID, Optional.ofNullable(grandParentUuid));
        this.entityData.set(DATA_NAME_JSON, nameJson == null ? "" : nameJson);
        this.entityData.set(DATA_GROWTH_STAGE, growthStage.name());
    }

    public void remapMotherUuid(UUID oldMotherUuid, UUID newMotherUuid) {
        if (oldMotherUuid == null || newMotherUuid == null || !oldMotherUuid.equals(this.motherUuid)) {
            return;
        }
        this.motherUuid = newMotherUuid;
        this.entityData.set(DATA_MOTHER_UUID, Optional.of(newMotherUuid));
        getPersistentData().putUUID(MaidChildEntity.PERSISTENT_MOTHER_UUID_KEY, newMotherUuid);
    }

    public int getLonging() {
        return this.entityData.get(DATA_LONGING);
    }

    public int getSootheCount() {
        return this.entityData.get(DATA_SOOTHE_COUNT);
    }

    public int getMemoryCount() {
        return this.entityData.get(DATA_MEMORY_COUNT);
    }

    public boolean hasRecognizedFather() {
        return this.entityData.get(DATA_RECOGNIZED_FATHER);
    }

    public boolean isLanternReady() {
        return hasRecognizedFather() && getLonging() >= LANTERN_READY_LONGING;
    }

    public boolean isLanternBound() {
        return this.entityData.get(DATA_LANTERN_BOUND);
    }

    public boolean isStaying() {
        return this.entityData.get(DATA_STAYING);
    }

    public boolean isFarewell() {
        return this.entityData.get(DATA_FAREWELL);
    }

    /**
     * 安抚阶段推进：前五次每次 +1 眷恋，并记录次数。
     *
     * <p>次数写在实体同步数据里，客户端台本可以直接根据阶段显示不同文本。
     */
    public boolean advanceSoothe() {
        if (getSootheCount() >= MAX_SOOTHE_COUNT || isFarewell()) {
            return false;
        }
        this.entityData.set(DATA_SOOTHE_COUNT, getSootheCount() + 1);
        addLonging(1);
        return true;
    }

    /**
     * 追忆阶段推进：安抚五次后开放，五次追忆后灵体认出父亲。
     */
    public boolean advanceMemory() {
        if (getSootheCount() < MAX_SOOTHE_COUNT || getMemoryCount() >= MAX_MEMORY_COUNT || isFarewell()) {
            return false;
        }
        int nextMemory = getMemoryCount() + 1;
        this.entityData.set(DATA_MEMORY_COUNT, nextMemory);
        addLonging(1);
        if (nextMemory >= MAX_MEMORY_COUNT) {
            this.entityData.set(DATA_RECOGNIZED_FATHER, true);
        }
        return true;
    }

    public void addLonging(int amount) {
        int next = Math.max(0, Math.min(100, getLonging() + amount));
        this.entityData.set(DATA_LONGING, next);
    }

    public void setLanternBound(boolean bound) {
        this.entityData.set(DATA_LANTERN_BOUND, bound);
        if (!bound) {
            this.lanternBindPlayerUuid = null;
        }
    }

    public void bindSoulLanternTo(ServerPlayer player) {
        this.lanternBindPlayerUuid = player == null ? null : player.getUUID();
        this.entityData.set(DATA_LANTERN_BOUND, this.lanternBindPlayerUuid != null);
        if (this.lanternBindPlayerUuid != null) {
            tickLanternFollow();
        }
    }

    public void markStaying() {
        this.entityData.set(DATA_STAYING, true);
    }

    public void markFarewell() {
        this.entityData.set(DATA_FAREWELL, true);
        setLanternBound(false);
    }

    /**
     * 灵魂灯笼牵引距离过远时使用硬传送。
     *
     * <p>只有硬传送才重置 old 坐标，避免客户端从远处拉出很长的插值轨迹。
     */
    public void moveSpiritAnchor(double x, double y, double z, float yaw) {
        this.baseY = y;
        this.moveTo(x, y, z, yaw, this.getXRot());
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.xOld = x;
        this.yOld = y;
        this.zOld = z;
        this.setYHeadRot(yaw);
        this.setYBodyRot(yaw);
        this.hasImpulse = true;
    }

    /**
     * 灵魂灯笼牵引时的真实实体坐标插值。
     *
     * <p>不要用 {@code move(MoverType.SELF, ...)}：灵体继承自女仆实体，女仆自身的导航、
     * 碰撞和客户端同步状态会和 noPhysics 组合出很奇怪的轴向漂移。这里直接写实体坐标，
     * 但不重置 old 坐标，让客户端正常做平滑插值。
     */
    private void setSpiritFollowPosition(double x, double y, double z, float yaw) {
        this.baseY = y;
        this.setPos(x, y, z);
        setSpiritYaw(yaw);
        this.hasImpulse = true;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int positionRotationIncrements) {
        super.lerpTo(x, y, z, yaw, pitch, positionRotationIncrements);
        this.baseY = y;
    }

    @Nullable
    public UUID getOriginalMaidUuid() {
        return this.entityData.get(DATA_ORIGINAL_MAID_UUID).orElse(null);
    }

    @Nullable
    public UUID getOwnerUuid() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    @Nullable
    public UUID getMotherUuid() {
        if (this.motherUuid != null) {
            return this.motherUuid;
        }
        Optional<UUID> synced = this.entityData.get(DATA_MOTHER_UUID);
        if (synced.isPresent()) {
            return synced.get();
        }
        ChildLineageData lineage = getData(ModTaskData.CHILD_LINEAGE_DATA);
        if (lineage != null && lineage.mother().isPresent()) {
            return lineage.mother().get();
        }
        ChildStateData state = getData(ModTaskData.CHILD_STATE_DATA);
        if (state != null && state.mother().isPresent()) {
            return state.mother().get();
        }
        CompoundTag tag = getPersistentData();
        return tag.hasUUID(MaidChildEntity.PERSISTENT_MOTHER_UUID_KEY)
                ? tag.getUUID(MaidChildEntity.PERSISTENT_MOTHER_UUID_KEY)
                : null;
    }

    @Nullable
    public UUID getFatherUuid() {
        if (this.fatherUuid != null) {
            return this.fatherUuid;
        }
        Optional<UUID> synced = this.entityData.get(DATA_FATHER_UUID);
        if (synced.isPresent()) {
            return synced.get();
        }
        ChildLineageData lineage = getData(ModTaskData.CHILD_LINEAGE_DATA);
        if (lineage != null && lineage.father().isPresent()) {
            return lineage.father().get();
        }
        ChildStateData state = getData(ModTaskData.CHILD_STATE_DATA);
        if (state != null && state.father().isPresent()) {
            return state.father().get();
        }
        CompoundTag tag = getPersistentData();
        return tag.hasUUID(MaidChildEntity.PERSISTENT_FATHER_UUID_KEY)
                ? tag.getUUID(MaidChildEntity.PERSISTENT_FATHER_UUID_KEY)
                : null;
    }

    @Nullable
    public UUID getGrandParentUuid() {
        if (this.grandParentUuid != null) {
            return this.grandParentUuid;
        }
        Optional<UUID> synced = this.entityData.get(DATA_GRAND_PARENT_UUID);
        if (synced.isPresent()) {
            return synced.get();
        }
        ChildLineageData lineage = getData(ModTaskData.CHILD_LINEAGE_DATA);
        if (lineage != null && lineage.grandParent().isPresent()) {
            return lineage.grandParent().get();
        }
        CompoundTag tag = getPersistentData();
        return tag.hasUUID(MaidChildEntity.PERSISTENT_GRAND_PARENT_UUID_KEY)
                ? tag.getUUID(MaidChildEntity.PERSISTENT_GRAND_PARENT_UUID_KEY)
                : null;
    }

    public MaidChildEntity.GrowthStage getGrowthStage() {
        MaidChildEntity.GrowthStage synced = MaidChildEntity.GrowthStage.byName(this.entityData.get(DATA_GROWTH_STAGE));
        if (synced != MaidChildEntity.GrowthStage.ADULT) {
            return synced;
        }
        ChildStateData state = getData(ModTaskData.CHILD_STATE_DATA);
        if (state != null && state.child()) {
            return MaidChildEntity.GrowthStage.byName(state.growthStage());
        }
        CompoundTag persistent = getPersistentData();
        if (persistent.contains(MaidChildEntity.PERSISTENT_GROWTH_STAGE_KEY)) {
            return MaidChildEntity.GrowthStage.byName(persistent.getString(MaidChildEntity.PERSISTENT_GROWTH_STAGE_KEY));
        }
        return synced;
    }

    @Override
    public Component getName() {
        Component customName = parseName(this.entityData.get(DATA_NAME_JSON));
        return customName == null ? super.getName() : customName;
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public boolean isFood(net.minecraft.world.item.ItemStack stack) {
        return false;
    }

    @Override
    public boolean shouldLeaveMountOrSitForDanger() {
        return false;
    }

    @Override
    public boolean canPickup(net.minecraft.world.entity.Entity entity, boolean simulate) {
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(TAG_ORIGINAL_MAID_UUID)) {
            this.entityData.set(DATA_ORIGINAL_MAID_UUID, Optional.of(tag.getUUID(TAG_ORIGINAL_MAID_UUID)));
        }
        if (tag.hasUUID(TAG_OWNER_UUID)) {
            this.entityData.set(DATA_OWNER_UUID, Optional.of(tag.getUUID(TAG_OWNER_UUID)));
        }
        this.motherUuid = tag.hasUUID(TAG_MOTHER_UUID) ? tag.getUUID(TAG_MOTHER_UUID) : null;
        this.fatherUuid = tag.hasUUID(TAG_FATHER_UUID) ? tag.getUUID(TAG_FATHER_UUID) : null;
        this.grandParentUuid = tag.hasUUID(TAG_GRAND_PARENT_UUID) ? tag.getUUID(TAG_GRAND_PARENT_UUID) : null;
        CompoundTag persistent = getPersistentData();
        if (this.motherUuid == null && persistent.hasUUID(MaidChildEntity.PERSISTENT_MOTHER_UUID_KEY)) {
            this.motherUuid = persistent.getUUID(MaidChildEntity.PERSISTENT_MOTHER_UUID_KEY);
        }
        if (this.fatherUuid == null && persistent.hasUUID(MaidChildEntity.PERSISTENT_FATHER_UUID_KEY)) {
            this.fatherUuid = persistent.getUUID(MaidChildEntity.PERSISTENT_FATHER_UUID_KEY);
        }
        if (this.grandParentUuid == null && persistent.hasUUID(MaidChildEntity.PERSISTENT_GRAND_PARENT_UUID_KEY)) {
            this.grandParentUuid = persistent.getUUID(MaidChildEntity.PERSISTENT_GRAND_PARENT_UUID_KEY);
        }
        this.entityData.set(DATA_MOTHER_UUID, Optional.ofNullable(this.motherUuid));
        this.entityData.set(DATA_FATHER_UUID, Optional.ofNullable(this.fatherUuid));
        this.entityData.set(DATA_GRAND_PARENT_UUID, Optional.ofNullable(this.grandParentUuid));
        this.lanternBindPlayerUuid = tag.hasUUID(TAG_LANTERN_BIND_PLAYER_UUID) ? tag.getUUID(TAG_LANTERN_BIND_PLAYER_UUID) : null;
        if (tag.contains(TAG_NAME_JSON)) {
            this.entityData.set(DATA_NAME_JSON, tag.getString(TAG_NAME_JSON));
        }
        if (tag.contains(TAG_GROWTH_STAGE)) {
            this.entityData.set(DATA_GROWTH_STAGE, tag.getString(TAG_GROWTH_STAGE));
        }
        this.entityData.set(DATA_LONGING, tag.getInt(TAG_LONGING));
        this.entityData.set(DATA_SOOTHE_COUNT, Math.min(MAX_SOOTHE_COUNT, Math.max(0, tag.getInt(TAG_SOOTHE_COUNT))));
        this.entityData.set(DATA_MEMORY_COUNT, Math.min(MAX_MEMORY_COUNT, Math.max(0, tag.getInt(TAG_MEMORY_COUNT))));
        this.entityData.set(DATA_RECOGNIZED_FATHER, tag.getBoolean(TAG_RECOGNIZED_FATHER));
        this.entityData.set(DATA_LANTERN_BOUND, tag.getBoolean(TAG_LANTERN_BOUND));
        this.entityData.set(DATA_STAYING, tag.getBoolean(TAG_STAYING));
        this.entityData.set(DATA_FAREWELL, tag.getBoolean(TAG_FAREWELL));
        this.baseY = tag.contains("BaseY") ? tag.getDouble("BaseY") : this.getY();
        this.setNoAi(true);
        this.setInvulnerable(true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID originalMaidUuid = getOriginalMaidUuid();
        UUID ownerUuid = getOwnerUuid();
        if (originalMaidUuid != null) {
            tag.putUUID(TAG_ORIGINAL_MAID_UUID, originalMaidUuid);
        }
        if (ownerUuid != null) {
            tag.putUUID(TAG_OWNER_UUID, ownerUuid);
        }
        if (this.motherUuid != null) {
            tag.putUUID(TAG_MOTHER_UUID, this.motherUuid);
        }
        if (this.fatherUuid != null) {
            tag.putUUID(TAG_FATHER_UUID, this.fatherUuid);
        }
        if (this.grandParentUuid != null) {
            tag.putUUID(TAG_GRAND_PARENT_UUID, this.grandParentUuid);
        }
        if (this.lanternBindPlayerUuid != null) {
            tag.putUUID(TAG_LANTERN_BIND_PLAYER_UUID, this.lanternBindPlayerUuid);
        }
        tag.putString(TAG_NAME_JSON, this.entityData.get(DATA_NAME_JSON));
        tag.putString(TAG_GROWTH_STAGE, this.entityData.get(DATA_GROWTH_STAGE));
        tag.putInt(TAG_LONGING, getLonging());
        tag.putInt(TAG_SOOTHE_COUNT, getSootheCount());
        tag.putInt(TAG_MEMORY_COUNT, getMemoryCount());
        tag.putBoolean(TAG_RECOGNIZED_FATHER, hasRecognizedFather());
        tag.putBoolean(TAG_LANTERN_BOUND, isLanternBound());
        tag.putBoolean(TAG_STAYING, isStaying());
        tag.putBoolean(TAG_FAREWELL, isFarewell());
        tag.putDouble("BaseY", this.baseY);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    private void tickLanternFollow() {
        if (!(level() instanceof ServerLevel serverLevel) || lanternBindPlayerUuid == null) {
            return;
        }
        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(lanternBindPlayerUuid);
        if (player == null || !player.isAlive() || player.level() != serverLevel || !hasSoulLantern(player)) {
            setLanternBound(false);
            return;
        }
        Vec3 followForward = resolveFollowForward(player);
        Vec3 right = new Vec3(-followForward.z, 0.0D, followForward.x);
        Vec3 target = player.position()
                .subtract(followForward.scale(FOLLOW_BACK_OFFSET))
                .add(right.scale(FOLLOW_SIDE_OFFSET))
                .add(0.0D, FOLLOW_HEIGHT_OFFSET, 0.0D);
        if (distanceToSqr(player) > FOLLOW_TELEPORT_DISTANCE_SQR) {
            Vec3 safe = findSafeFollowTeleportPos(serverLevel, player, followForward);
            moveSpiritAnchor(safe.x, safe.y, safe.z, yawFromMovement(followForward));
            return;
        }
        Vec3 toTarget = target.subtract(position());
        if (toTarget.lengthSqr() <= FOLLOW_STOP_DISTANCE_SQR) {
            facePlayer(player);
            return;
        }
        double distance = toTarget.length();
        double step = Math.min(FOLLOW_MAX_STEP_PER_TICK, distance);
        Vec3 stepVector = toTarget.scale(step / distance);
        moveSpiritStep(stepVector);
    }

    private Vec3 resolveFollowForward(ServerPlayer player) {
        Vec3 movement = player.getDeltaMovement();
        Vec3 horizontalMovement = new Vec3(movement.x, 0.0D, movement.z);
        if (horizontalMovement.lengthSqr() > FOLLOW_DIRECTION_UPDATE_MIN_SPEED_SQR) {
            Vec3 normalized = horizontalMovement.normalize();
            this.followForwardX = normalized.x;
            this.followForwardZ = normalized.z;
            return normalized;
        }
        if (Math.abs(this.followForwardX) > 1.0E-4D || Math.abs(this.followForwardZ) > 1.0E-4D) {
            return new Vec3(this.followForwardX, 0.0D, this.followForwardZ).normalize();
        }
        Vec3 fallback = Vec3.directionFromRotation(0.0F, player.getYRot()).normalize();
        this.followForwardX = fallback.x;
        this.followForwardZ = fallback.z;
        return fallback;
    }

    private void moveSpiritStep(Vec3 step) {
        Vec3 next = position().add(step);
        float yaw = yawFromMovement(step);
        setSpiritFollowPosition(next.x, next.y, next.z, yaw);
    }

    private void facePlayer(ServerPlayer player) {
        Vec3 toPlayer = player.position().subtract(position());
        if (toPlayer.horizontalDistanceSqr() < 1.0E-4D) {
            return;
        }
        float yaw = yawFromMovement(toPlayer);
        setSpiritYaw(yaw);
    }

    private static float yawFromMovement(Vec3 movement) {
        return (float) (net.minecraft.util.Mth.atan2(movement.z, movement.x) * (180.0D / Math.PI)) - 90.0F;
    }

    private void setSpiritYaw(float yaw) {
        float smoothedYaw = getYRot() + net.minecraft.util.Mth.wrapDegrees(yaw - getYRot()) * 0.35F;
        float limitedYaw = net.minecraft.util.Mth.approachDegrees(getYRot(), smoothedYaw, FOLLOW_MAX_YAW_STEP);
        setYRot(limitedYaw);
        setYHeadRot(limitedYaw);
        setYBodyRot(limitedYaw);
    }

    private static boolean hasSoulLantern(ServerPlayer player) {
        return player.getMainHandItem().is(net.minecraft.world.item.Items.SOUL_LANTERN)
                || player.getOffhandItem().is(net.minecraft.world.item.Items.SOUL_LANTERN);
    }

    private static Vec3 findSafeFollowTeleportPos(ServerLevel level, ServerPlayer player, Vec3 look) {
        Vec3 preferred = player.position()
                .subtract(look.x * 3.0D, 0.0D, look.z * 3.0D)
                .add(0.0D, 0.35D, 0.0D);
        Vec3 safePreferred = safeCenter(level, preferred);
        if (safePreferred != null && safePreferred.distanceToSqr(player.position()) <= FOLLOW_TELEPORT_MAX_DISTANCE_SQR) {
            return safePreferred;
        }

        BlockPos origin = player.blockPosition();
        for (int radius = 1; radius <= FOLLOW_TELEPORT_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    for (int dy = -2; dy <= 2; dy++) {
                        BlockPos pos = origin.offset(dx, dy, dz);
                        Vec3 candidate = Vec3.atCenterOf(pos);
                        if (candidate.distanceToSqr(player.position()) > FOLLOW_TELEPORT_MAX_DISTANCE_SQR) {
                            continue;
                        }
                        Vec3 safe = safeCenter(level, candidate);
                        if (safe != null) {
                            return safe;
                        }
                    }
                }
            }
        }
        return preferred;
    }

    @Nullable
    private static Vec3 safeCenter(ServerLevel level, Vec3 pos) {
        BlockPos feet = BlockPos.containing(pos);
        BlockPos head = feet.above();
        if (!isOpen(level, feet) || !isOpen(level, head)) {
            return null;
        }
        return new Vec3(feet.getX() + 0.5D, feet.getY() + 0.35D, feet.getZ() + 0.5D);
    }

    private static boolean isOpen(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty();
    }

    public double getMeleeAttackRangeSqr(net.minecraft.world.entity.LivingEntity target) {
        return 0.0D;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity entity) {
        return false;
    }

    @Override
    public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
        return false;
    }

    @Override
    public boolean canAttackType(EntityType<?> type) {
        return false;
    }

    public void finalizeSpiritAttributes() {
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(1.0D);
        }
        setHealth(1.0F);
    }

    @Nullable
    private static Component parseName(String nameJson) {
        if (nameJson == null || nameJson.isBlank()) {
            return null;
        }
        try {
            return ComponentJsonUtil.fromJson(nameJson, net.minecraft.client.Minecraft.getInstance().level.registryAccess());
        } catch (Exception ignored) {
            return null;
        }
    }

    private void copyFamilyPersistentData(EntityMaid maid) {
        CompoundTag source = maid.getPersistentData();
        CompoundTag target = getPersistentData();
        copyUuid(source, target, MaidChildEntity.PERSISTENT_MOTHER_UUID_KEY);
        copyUuid(source, target, MaidChildEntity.PERSISTENT_FATHER_UUID_KEY);
        copyUuid(source, target, MaidChildEntity.PERSISTENT_GRAND_PARENT_UUID_KEY);
        if (source.contains(MaidChildEntity.PERSISTENT_CHILD_NAME_JSON_KEY)) {
            target.putString(MaidChildEntity.PERSISTENT_CHILD_NAME_JSON_KEY,
                    source.getString(MaidChildEntity.PERSISTENT_CHILD_NAME_JSON_KEY));
        }
        target.putBoolean(MaidChildEntity.BORN_MAID_TAG, true);
        addTag(MaidChildEntity.BORN_MAID_TAG);
    }

    private void copyChildLifecycleData(EntityMaid maid) {
        ChildStateData sourceState = maid.getData(ModTaskData.CHILD_STATE_DATA);
        ChildLineageData lineage = maid.getData(ModTaskData.CHILD_LINEAGE_DATA);
        if (lineage != null && lineage.bornMaid()) {
            setData(ModTaskData.CHILD_LINEAGE_DATA, lineage);
        }
        CompoundTag source = maid.getPersistentData();
        if (sourceState != null && sourceState.child()) {
            setData(ModTaskData.CHILD_STATE_DATA, sourceState);
            getPersistentData().putBoolean(MaidChildEntity.PERSISTENT_CHILD_ACTIVE_KEY, true);
            getPersistentData().putInt(MaidChildEntity.PERSISTENT_GROWTH_TICKS_KEY, sourceState.growthTicks());
            getPersistentData().putString(MaidChildEntity.PERSISTENT_GROWTH_STAGE_KEY, sourceState.growthStage());
            return;
        }
        if (MaidChildEntity.shouldStayChild(maid)) {
            MaidChildEntity.GrowthStage stage = MaidChildEntity.resolveGrowthStage(maid);
            getPersistentData().putBoolean(MaidChildEntity.PERSISTENT_CHILD_ACTIVE_KEY, true);
            if (source.contains(MaidChildEntity.PERSISTENT_GROWTH_TICKS_KEY)) {
                getPersistentData().putInt(MaidChildEntity.PERSISTENT_GROWTH_TICKS_KEY,
                        source.getInt(MaidChildEntity.PERSISTENT_GROWTH_TICKS_KEY));
            }
            getPersistentData().putString(MaidChildEntity.PERSISTENT_GROWTH_STAGE_KEY, stage.name());
        }
    }

    private static void copyUuid(CompoundTag source, CompoundTag target, String key) {
        if (source.hasUUID(key)) {
            target.putUUID(key, source.getUUID(key));
        }
    }

    private static String resolveSupportedModelId(EntityMaid maid) {
        if (maid.isYsmModel()) {
            return "touhou_little_maid:hakurei_reimu";
        }
        String modelId = maid.getModelId();
        return modelId == null || modelId.isBlank() ? "touhou_little_maid:hakurei_reimu" : modelId;
    }
}
