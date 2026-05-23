package com.example.maidmarriage.client;

import com.example.maidmarriage.client.interaction.BuiltinInteractionActions;
import com.example.maidmarriage.client.interaction.InteractionTargetRegistry;
import com.example.maidmarriage.client.interaction.SpiritInteractionTargetAdapter;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public final class ClientOnlyBootstrap {
    private ClientOnlyBootstrap() {
    }

    public static void init() {
        InteractionTargetRegistry.register(new SpiritInteractionTargetAdapter());
        BuiltinInteractionActions.register();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new MaidMarriageConfigScreen(parent)));
    }
}

