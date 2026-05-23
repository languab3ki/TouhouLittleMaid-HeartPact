package com.example.maidmarriage.client.interaction;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A concrete generic interaction opened for one target entity.
 */
public record InteractionSession(
        ResourceLocation targetType,
        UUID targetUuid,
        ResourceLocation scenarioId,
        Component title,
        boolean allowVoice
) {
}
