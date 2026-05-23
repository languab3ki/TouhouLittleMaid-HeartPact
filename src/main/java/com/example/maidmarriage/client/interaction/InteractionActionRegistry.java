package com.example.maidmarriage.client.interaction;

import com.example.maidmarriage.client.dialoguesystem.runtime.DialogueActionRequest;
import com.example.maidmarriage.config.ModConfigs;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Safe registry for generic interaction actions emitted from dialogue JSON.
 */
public final class InteractionActionRegistry {
    private static final Map<ResourceLocation, InteractionActionHandler> HANDLERS = new LinkedHashMap<>();

    private InteractionActionRegistry() {
    }

    public static void register(ResourceLocation id, InteractionActionHandler handler) {
        if (id == null || handler == null) {
            return;
        }
        HANDLERS.put(id, handler);
    }

    public static void execute(Minecraft minecraft,
                               InteractionSession session,
                               InteractionTargetAdapter adapter,
                               DialogueActionRequest request,
                               @Nullable Entity target,
                               Consumer<String> debugSink,
                               Runnable closeScreen) {
        if (request == null || request.actionId() == null || request.actionId().isBlank()) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(request.actionId());
        if (id == null) {
            debug(debugSink, "Invalid interaction action: " + request.actionId());
            return;
        }
        InteractionActionHandler handler = HANDLERS.get(id);
        if (handler == null) {
            debug(debugSink, "Unregistered interaction action: " + id);
            return;
        }
        handler.execute(new InteractionActionContext(minecraft, session, adapter, request, target, debugSink, closeScreen));
    }

    static void debug(Consumer<String> debugSink, String message) {
        if (debugSink != null && ModConfigs.showUiActionDebug()) {
            debugSink.accept(message);
        }
    }
}
