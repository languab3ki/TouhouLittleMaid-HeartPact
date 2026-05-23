package com.example.maidmarriage.init;

import com.example.maidmarriage.MaidMarriageMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 心契同眠专属创造模式分类。
 *
 * <p>不要再把物品挂到原版“原材料”分类里，否则玩家会误以为这些道具只是合成材料。
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MaidMarriageMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> HEART_PACT = CREATIVE_TABS.register("heart_pact",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.maidmarriage.heart_pact"))
                    .icon(() -> new ItemStack(ModItems.HEART_PACT_GUIDE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.HEART_PACT_GUIDE.get());
                        output.accept(ModItems.PROPOSAL_RING.get());
                        output.accept(ModItems.SUNFLOWER_HAIRPIN.get());
                        output.accept(ModItems.YES_PILLOW.get());
                        output.accept(ModItems.RAINBOW_BOUQUET.get());
                        output.accept(ModItems.SAUCE_DUCK.get());
                        output.accept(ModItems.FAMILY_TREE_TOOL.get());
                        output.accept(ModItems.MARRIAGE_CONSENT_FORM.get());

                        output.accept(ModItems.LONGING_TESTER.get());
                        output.accept(ModItems.FLOWER_TEST_KIT.get());
                        output.accept(ModItems.GROWTH_TOOL.get());
                        output.accept(ModItems.BIRTH_TOOL.get());
                        output.accept(ModItems.PREGNANCY_TEST_TOOL.get());
                        output.accept(ModItems.PREGNANCY_SETTLEMENT_TOOL.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
