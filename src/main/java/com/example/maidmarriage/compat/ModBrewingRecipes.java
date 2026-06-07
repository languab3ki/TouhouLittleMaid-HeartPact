package com.example.maidmarriage.compat;

import com.example.maidmarriage.init.ModPotions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class ModBrewingRecipes {
    private ModBrewingRecipes() {
    }

    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BrewingRecipeRegistry.addRecipe(new PotionBrewingRecipe(
                    Potions.AWKWARD,
                    Ingredient.of(Items.CORNFLOWER, Items.BLUE_ORCHID),
                    ModPotions.SAFETY.get()
            ));
        });
    }

    private record PotionBrewingRecipe(Potion inputPotion, Ingredient ingredient, Potion outputPotion) implements IBrewingRecipe {
        @Override
        public boolean isInput(ItemStack input) {
            return input.is(Items.POTION) && PotionUtils.getPotion(input) == inputPotion;
        }

        @Override
        public boolean isIngredient(ItemStack ingredientStack) {
            return ingredient.test(ingredientStack);
        }

        @Override
        public ItemStack getOutput(ItemStack input, ItemStack ingredientStack) {
            if (!isInput(input) || !isIngredient(ingredientStack)) {
                return ItemStack.EMPTY;
            }
            ItemStack output = new ItemStack(Items.POTION);
            PotionUtils.setPotion(output, outputPotion);
            return output;
        }
    }
}
