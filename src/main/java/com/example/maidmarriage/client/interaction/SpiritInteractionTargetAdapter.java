package com.example.maidmarriage.client.interaction;

import com.example.maidmarriage.MaidMarriageMod;
import com.example.maidmarriage.client.ClientEntityLookup;
import com.example.maidmarriage.client.dialoguesystem.runtime.HugDialogueRuntimeBridge;
import com.example.maidmarriage.client.dialoguesystem.runtime.HugDialogueTextPools;
import com.example.maidmarriage.entity.MaidChildEntity;
import com.example.maidmarriage.entity.MaidSpiritEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

public final class SpiritInteractionTargetAdapter implements InteractionTargetAdapter {
    public static final ResourceLocation TYPE = new ResourceLocation(MaidMarriageMod.MOD_ID, "spirit");
    public static final ResourceLocation SCENARIO = new ResourceLocation(MaidMarriageMod.MOD_ID, "spirit_interaction_v1");
    private static final double MOTHER_REUNION_DISTANCE = 10.0D;
    private static final double MOTHER_REUNION_DISTANCE_SQR = MOTHER_REUNION_DISTANCE * MOTHER_REUNION_DISTANCE;

    @Override
    public ResourceLocation targetType() {
        return TYPE;
    }

    @Override
    public boolean supports(Entity entity) {
        return entity instanceof MaidSpiritEntity;
    }

    @Override
    public ResourceLocation defaultScenario(Entity entity) {
        return SCENARIO;
    }

    @Override
    public Component title(Entity entity) {
        return Component.translatable("ui.maidmarriage.generic_interaction.spirit.title");
    }

    @Override
    public boolean allowVoice(Entity entity) {
        return false;
    }

    @Override
    public void writeVariables(HugDialogueRuntimeBridge runtime, Minecraft minecraft, Entity entity) {
        if (!(entity instanceof MaidSpiritEntity spirit)) {
            return;
        }
        runtime.setVariable("spirit_original_uuid", shortUuid(spirit.getOriginalMaidUuid()));
        runtime.setVariable("spirit_growth_stage", spirit.getGrowthStage().name().toLowerCase(java.util.Locale.ROOT));
        int longing = spirit.getLonging();
        runtime.setVariable("spirit_longing", Integer.toString(longing));
        runtime.setVariable("spirit_longing_low", Boolean.toString(longing < 30));
        runtime.setVariable("spirit_longing_mid", Boolean.toString(longing >= 30 && longing < 70));
        runtime.setVariable("spirit_longing_high", Boolean.toString(longing >= 70));
        runtime.setVariable("spirit_resurrection_ready", Boolean.toString(longing >= 100 && spirit.isStaying() && !spirit.isFarewell()));
        writeSpiritPoolVariables(runtime, spirit, longing);
        runtime.setVariable("spirit_soothe_count", Integer.toString(spirit.getSootheCount()));
        runtime.setVariable("spirit_memory_count", Integer.toString(spirit.getMemoryCount()));
        runtime.setVariable("spirit_lantern_ready", Boolean.toString(spirit.isLanternReady()));
        runtime.setVariable("spirit_recognized_father", Boolean.toString(spirit.hasRecognizedFather()));
        runtime.setVariable("spirit_staying", Boolean.toString(spirit.isStaying()));
        runtime.setVariable("spirit_farewell", Boolean.toString(spirit.isFarewell()));
        runtime.setVariable("spirit_stage_infant", Boolean.toString(spirit.getGrowthStage() == MaidChildEntity.GrowthStage.INFANT));
        runtime.setVariable("spirit_stage_juvenile", Boolean.toString(spirit.getGrowthStage() == MaidChildEntity.GrowthStage.JUVENILE));
        runtime.setVariable("spirit_stage_child", Boolean.toString(spirit.getGrowthStage() == MaidChildEntity.GrowthStage.CHILD));
        runtime.setVariable("spirit_stage_adult", Boolean.toString(spirit.getGrowthStage() == MaidChildEntity.GrowthStage.ADULT));
        writeCountFlags(runtime, "spirit_soothe", spirit.getSootheCount(), MaidSpiritEntity.MAX_SOOTHE_COUNT);
        writeCountFlags(runtime, "spirit_memory", spirit.getMemoryCount(), MaidSpiritEntity.MAX_MEMORY_COUNT);
        boolean family = minecraft != null
                && minecraft.player != null
                && (minecraft.player.getUUID().equals(spirit.getOwnerUuid())
                || minecraft.player.getUUID().equals(spirit.getGrandParentUuid()));
        runtime.setVariable("spirit_family", Boolean.toString(family));
        EntityMaid nearbyMother = findNearbyMother(minecraft, spirit);
        boolean motherNearby = nearbyMother != null;
        runtime.setVariable("spirit_mother_nearby", Boolean.toString(motherNearby));
        runtime.setVariable("spirit_reunion_ready", Boolean.toString(motherNearby));
        runtime.setVariable("spirit_mother", nearbyMother == null ? "妈妈" : nearbyMother.getDisplayName().getString());
        runtime.setVariable("spirit_tip", resolveTip(spirit));
    }

    private static void writeSpiritPoolVariables(HugDialogueRuntimeBridge runtime, MaidSpiritEntity spirit, int longing) {
        String tier = longing >= 70 ? "high" : longing >= 30 ? "mid" : "low";
        String anchor = spirit.getUUID() + "|" + tier + "|" + longing + "|" + spirit.getSootheCount()
                + "|" + spirit.getMemoryCount() + "|" + spirit.isStaying();
        if (anchor.equals(runtime.renderTemplate("${spirit_pool_anchor}"))
                && !runtime.renderTemplate("${spirit_soothe_text}").isBlank()) {
            return;
        }
        runtime.setVariable("spirit_pool_anchor", anchor);
        runtime.setVariable("spirit_soothe_text", HugDialogueTextPools.pickSpiritSootheCue(tier).text());
        runtime.setVariable("spirit_offering_flower_text", HugDialogueTextPools.pickSpiritOfferingFlowerCue().text());
        runtime.setVariable("spirit_offering_soul_text", HugDialogueTextPools.pickSpiritOfferingSoulCue().text());
        runtime.setVariable("spirit_offering_limit_text", HugDialogueTextPools.pickSpiritOfferingLimitCue().text());
    }

    private static void writeCountFlags(HugDialogueRuntimeBridge runtime, String prefix, int value, int max) {
        for (int i = 0; i <= max; i++) {
            runtime.setVariable(prefix + "_" + i, Boolean.toString(value == i));
        }
        runtime.setVariable(prefix + "_max", Boolean.toString(value >= max));
    }

    private static EntityMaid findNearbyMother(Minecraft minecraft, MaidSpiritEntity spirit) {
        if (minecraft == null || minecraft.level == null || spirit.getMotherUuid() == null || !spirit.hasRecognizedFather()) {
            return null;
        }
        Entity direct = ClientEntityLookup.findEntity(spirit.getMotherUuid());
        if (direct instanceof EntityMaid maid && maid.distanceToSqr(spirit) <= MOTHER_REUNION_DISTANCE_SQR) {
            return maid;
        }
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof EntityMaid maid
                    && spirit.getMotherUuid().equals(maid.getUUID())
                    && maid.distanceToSqr(spirit) <= MOTHER_REUNION_DISTANCE_SQR) {
                return maid;
            }
        }
        return null;
    }

    private static String resolveTip(MaidSpiritEntity spirit) {
        if (spirit.isFarewell()) {
            return "残响正在变得很轻。";
        }
        if (spirit.getSootheCount() < MaidSpiritEntity.MAX_SOOTHE_COUNT) {
            return "先一次次安抚她，让破碎的声音慢慢回来。";
        }
        if (spirit.getMemoryCount() < MaidSpiritEntity.MAX_MEMORY_COUNT) {
            return "追忆过去，她会一点点想起家的轮廓。";
        }
        if (!spirit.isLanternReady()) {
            return "眷恋还不够稳定，继续陪她停留一会儿。";
        }
        if (!spirit.isStaying()) {
            return "主手持灵魂灯笼右键她，可以牵起这道小小的残响。";
        }
        return "她选择暂时留下。以后还可以继续陪她追忆。";
    }

    private static String shortUuid(java.util.UUID uuid) {
        if (uuid == null) {
            return "";
        }
        String value = uuid.toString();
        return value.length() <= 8 ? value : value.substring(0, 8);
    }
}
