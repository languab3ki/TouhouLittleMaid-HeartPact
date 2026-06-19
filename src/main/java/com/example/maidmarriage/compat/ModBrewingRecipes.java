package com.example.maidmarriage.compat;

import com.example.maidmarriage.init.ModPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@EventBusSubscriber(modid = com.example.maidmarriage.MaidMarriageMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ModBrewingRecipes {
    private ModBrewingRecipes() {
    }

    @SubscribeEvent
    public static void register(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, Items.CORNFLOWER, ModPotions.SAFETY);
        builder.addMix(Potions.AWKWARD, Items.BLUE_ORCHID, ModPotions.SAFETY);
    }
}
