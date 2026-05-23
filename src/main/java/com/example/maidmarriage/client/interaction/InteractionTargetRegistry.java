package com.example.maidmarriage.client.interaction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Client-side registry for generic interaction target adapters.
 */
public final class InteractionTargetRegistry {
    private static final Map<ResourceLocation, InteractionTargetAdapter> ADAPTERS = new LinkedHashMap<>();

    private InteractionTargetRegistry() {
    }

    public static void register(InteractionTargetAdapter adapter) {
        if (adapter == null || adapter.targetType() == null) {
            return;
        }
        ADAPTERS.put(adapter.targetType(), adapter);
    }

    public static Optional<InteractionTargetAdapter> resolve(Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        for (InteractionTargetAdapter adapter : ADAPTERS.values()) {
            if (adapter.supports(entity)) {
                return Optional.of(adapter);
            }
        }
        return Optional.empty();
    }

    public static boolean openFor(Minecraft minecraft, Entity entity) {
        if (minecraft == null || entity == null) {
            return false;
        }
        Optional<InteractionTargetAdapter> adapterOptional = resolve(entity);
        if (adapterOptional.isEmpty()) {
            return false;
        }
        InteractionTargetAdapter adapter = adapterOptional.get();
        InteractionSession session = new InteractionSession(
                adapter.targetType(),
                entity.getUUID(),
                adapter.defaultScenario(entity),
                adapter.title(entity),
                adapter.allowVoice(entity)
        );
        minecraft.setScreen(new GenericMaidInteractionScreen(session, adapter));
        minecraft.mouseHandler.releaseMouse();
        return true;
    }
}
