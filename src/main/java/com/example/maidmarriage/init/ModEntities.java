package com.example.maidmarriage.init;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.entity.LapPillowAnchorEntity;
import com.example.maidmarriage.entity.LiftProxyEntity;
import com.example.maidmarriage.entity.MaidCarryProxyEntity;
import com.example.maidmarriage.entity.MaidChildEntity;
import com.example.maidmarriage.entity.MaidSpiritEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 实体注册表：注册子代女仆实体类型。
 * 该类的具体逻辑可参见下方方法与字段定义。
 */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MaidMarriageMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<MaidChildEntity>> MAID_CHILD =
            ENTITY_TYPES.register("maid_child", () ->
                    EntityType.Builder.of(MaidChildEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.5F)
                            .clientTrackingRange(10)
                            .build("maidmarriage:maid_child"));

    public static final DeferredHolder<EntityType<?>, EntityType<LiftProxyEntity>> LIFT_PROXY =
            ENTITY_TYPES.register("lift_proxy", () ->
                    EntityType.Builder.<LiftProxyEntity>of(LiftProxyEntity::new, MobCategory.MISC)
                            .sized(0.01F, 0.01F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .noSave()
                            .noSummon()
                            .build("maidmarriage:lift_proxy"));

    public static final DeferredHolder<EntityType<?>, EntityType<MaidCarryProxyEntity>> MAID_CARRY_PROXY =
            ENTITY_TYPES.register("maid_carry_proxy", () ->
                    EntityType.Builder.<MaidCarryProxyEntity>of(MaidCarryProxyEntity::new, MobCategory.MISC)
                            .sized(0.01F, 0.01F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .noSave()
                            .noSummon()
                            .build("maidmarriage:maid_carry_proxy"));

    public static final DeferredHolder<EntityType<?>, EntityType<LapPillowAnchorEntity>> LAP_PILLOW_ANCHOR =
            ENTITY_TYPES.register("lap_pillow_anchor", () ->
                    EntityType.Builder.<LapPillowAnchorEntity>of(LapPillowAnchorEntity::new, MobCategory.MISC)
                            .sized(0.01F, 0.01F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .noSave()
                            .noSummon()
                            .build("maidmarriage:lap_pillow_anchor"));

    public static final DeferredHolder<EntityType<?>, EntityType<MaidSpiritEntity>> MAID_SPIRIT =
            ENTITY_TYPES.register("maid_spirit", () ->
                    EntityType.Builder.<MaidSpiritEntity>of(MaidSpiritEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.4F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build("maidmarriage:maid_spirit"));

    private ModEntities() {
    }
}
