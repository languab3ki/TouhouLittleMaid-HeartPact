package com.example.maidmarriage.init;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.entity.LapPillowAnchorEntity;
import com.example.maidmarriage.entity.LiftProxyEntity;
import com.example.maidmarriage.entity.MaidCarryProxyEntity;
import com.example.maidmarriage.entity.MaidChildEntity;
import com.example.maidmarriage.entity.MaidSpiritEntity;
import com.example.maidmarriage.entity.StarfallMagicCircleEntity;
import com.example.maidmarriage.entity.StarfallMeteorEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 实体注册表：注册子代女仆实体类型。
 * 该类的具体逻辑可参见下方方法与字段定义。
 */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MaidMarriageMod.MOD_ID);

    public static final RegistryObject<EntityType<MaidChildEntity>> MAID_CHILD =
            ENTITY_TYPES.register("maid_child", () ->
                    EntityType.Builder.of(MaidChildEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.5F)
                            .clientTrackingRange(10)
                            .build(new ResourceLocation(MaidMarriageMod.MOD_ID, "maid_child").toString()));

    public static final RegistryObject<EntityType<LiftProxyEntity>> LIFT_PROXY =
            ENTITY_TYPES.register("lift_proxy", () ->
                    EntityType.Builder.<LiftProxyEntity>of(LiftProxyEntity::new, MobCategory.MISC)
                            .sized(0.01F, 0.01F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .noSave()
                            .noSummon()
                            .build(new ResourceLocation(MaidMarriageMod.MOD_ID, "lift_proxy").toString()));

    public static final RegistryObject<EntityType<MaidCarryProxyEntity>> MAID_CARRY_PROXY =
            ENTITY_TYPES.register("maid_carry_proxy", () ->
                    EntityType.Builder.<MaidCarryProxyEntity>of(MaidCarryProxyEntity::new, MobCategory.MISC)
                            .sized(0.01F, 0.01F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .noSave()
                            .noSummon()
                            .build(new ResourceLocation(MaidMarriageMod.MOD_ID, "maid_carry_proxy").toString()));

    public static final RegistryObject<EntityType<LapPillowAnchorEntity>> LAP_PILLOW_ANCHOR =
            ENTITY_TYPES.register("lap_pillow_anchor", () ->
                    EntityType.Builder.<LapPillowAnchorEntity>of(LapPillowAnchorEntity::new, MobCategory.MISC)
                            .sized(0.01F, 0.01F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .noSave()
                            .noSummon()
                            .build(new ResourceLocation(MaidMarriageMod.MOD_ID, "lap_pillow_anchor").toString()));

    public static final RegistryObject<EntityType<MaidSpiritEntity>> MAID_SPIRIT =
            ENTITY_TYPES.register("maid_spirit", () ->
                    EntityType.Builder.<MaidSpiritEntity>of(MaidSpiritEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.4F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build(new ResourceLocation(MaidMarriageMod.MOD_ID, "maid_spirit").toString()));

    public static final RegistryObject<EntityType<StarfallMeteorEntity>> STARFALL_METEOR =
            ENTITY_TYPES.register("starfall_meteor", () ->
                    EntityType.Builder.<StarfallMeteorEntity>of(StarfallMeteorEntity::new, MobCategory.MISC)
                            .sized(1.25F, 1.25F)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .fireImmune()
                            .build(new ResourceLocation(MaidMarriageMod.MOD_ID, "starfall_meteor").toString()));

    public static final RegistryObject<EntityType<StarfallMagicCircleEntity>> STARFALL_MAGIC_CIRCLE =
            ENTITY_TYPES.register("starfall_magic_circle", () ->
                    EntityType.Builder.<StarfallMagicCircleEntity>of(StarfallMagicCircleEntity::new, MobCategory.MISC)
                            .sized(6.5F, 0.05F)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build(new ResourceLocation(MaidMarriageMod.MOD_ID, "starfall_magic_circle").toString()));

    private ModEntities() {
    }
}
