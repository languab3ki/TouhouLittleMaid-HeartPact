package com.example.maidmarriage.client.interaction;

import com.example.maidmarriage.client.dialoguesystem.runtime.HugDialogueRuntimeBridge;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Adapts one entity kind into the generic interaction framework.
 */
public interface InteractionTargetAdapter {
    ResourceLocation targetType();

    boolean supports(Entity entity);

    ResourceLocation defaultScenario(Entity entity);

    default Component title(Entity entity) {
        return entity == null ? Component.empty() : entity.getDisplayName();
    }

    default Component displayName(Entity entity) {
        return entity == null ? Component.empty() : entity.getDisplayName();
    }

    default boolean allowVoice(Entity entity) {
        return false;
    }

    default boolean isStillValid(Minecraft minecraft, UUID targetUuid) {
        Entity entity = findEntity(minecraft, targetUuid);
        return entity != null && entity.isAlive() && supports(entity);
    }

    default void writeVariables(HugDialogueRuntimeBridge runtime, Minecraft minecraft, Entity entity) {
    }

    @Nullable
    static Entity findEntity(Minecraft minecraft, UUID targetUuid) {
        if (minecraft == null || minecraft.level == null || targetUuid == null) {
            return null;
        }
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (targetUuid.equals(entity.getUUID())) {
                return entity;
            }
        }
        return null;
    }
}
