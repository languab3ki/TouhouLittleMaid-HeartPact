package com.example.maidmarriage.client.interaction;

import com.example.maidmarriage.client.dialoguesystem.runtime.DialogueActionRequest;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public record InteractionActionContext(
        Minecraft minecraft,
        InteractionSession session,
        InteractionTargetAdapter adapter,
        DialogueActionRequest request,
        @Nullable Entity target,
        Consumer<String> debugSink,
        Runnable closeScreen
) {
    @Nullable
    public UUID targetUuid() {
        return session == null ? null : session.targetUuid();
    }
}
