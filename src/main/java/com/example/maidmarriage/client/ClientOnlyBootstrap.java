package com.example.maidmarriage.client;

import com.example.maidmarriage.client.interaction.BuiltinInteractionActions;
import com.example.maidmarriage.client.interaction.InteractionTargetRegistry;
import com.example.maidmarriage.client.interaction.SpiritInteractionTargetAdapter;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.ModContainer;

public final class ClientOnlyBootstrap {
    private ClientOnlyBootstrap() {
    }

    public static void init(ModContainer modContainer) {
        InteractionTargetRegistry.register(new SpiritInteractionTargetAdapter());
        BuiltinInteractionActions.register();
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> new MaidMarriageConfigScreen(parent));
    }
}

