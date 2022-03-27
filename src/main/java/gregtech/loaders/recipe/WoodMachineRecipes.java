package gregtech.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.GTUtility;
import gregtech.api.util.world.DummyWorld;
import gregtech.common.ConfigHolder;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.BIO_CHAFF;

public class WoodMachineRecipes {

    public static void postInit() {
        processLogOreDictionary();
    }

    private static void processLogOreDictionary() {
        List<ItemStack> allWoodLogs = OreDictUnifier.getAllWithOreDictionaryName("logWood").stream()
                .flatMap(stack -> ModHandler.getAllSubItems(stack).stream())
                .collect(Collectors.toList());

        for (ItemStack stack : allWoodLogs) {
            Pair<IRecipe, ItemStack> outputPair = ModHandler.getRecipeOutput(null, stack);
            ItemStack plankStack = outputPair.getValue();
            int originalOutput = plankStack.getCount();
            if (plankStack.isEmpty()) {
                continue;
            }
            IRecipe outputRecipe = outputPair.getKey();

            //wood nerf
            if (ConfigHolder.recipes.nerfWoodCrafting) {
                //remove the old recipe
                ModHandler.removeRecipeByName(outputRecipe.getRegistryName());

                // new wood recipes
                //noinspection ConstantConditions
                ModHandler.addShapelessRecipe(outputRecipe.getRegistryName().toString(),
                        GTUtility.copyAmount(Math.max(1, originalOutput / 2), plankStack), stack);

                ModHandler.addShapedRecipe(outputRecipe.getRegistryName().getPath() + "_saw",
                        GTUtility.copyAmount(originalOutput, plankStack), "s", "L", 'L', stack);
            } else {
                //noinspection ConstantConditions
                ModHandler.addShapedRecipe(outputRecipe.getRegistryName().getPath() + "_saw",
                        GTUtility.copyAmount((int) (originalOutput * 1.5), plankStack), "s", "L", 'L', stack);
            }


            CUTTER_RECIPES.recipeBuilder().inputs(stack)
                    .fluidInputs(Lubricant.getFluid(1))
                    .outputs(GTUtility.copyAmount((int) (originalOutput * 1.5), plankStack), OreDictUnifier.get(dust, Wood, 2))
                    .duration(200).EUt(VA[ULV])
                    .buildAndRegister();

            ItemStack doorStack = ModHandler.getRecipeOutput(DummyWorld.INSTANCE,
                    plankStack, plankStack, null,
                    plankStack, plankStack, null,
                    plankStack, plankStack, null).getRight();

            if (!doorStack.isEmpty()) {
                ASSEMBLER_RECIPES.recipeBuilder()
                        .inputs(GTUtility.copyAmount(6, plankStack))
                        .outputs(doorStack)
                        .duration(600).EUt(4).circuitMeta(6)
                        .buildAndRegister();
            }

            ItemStack slabStack = ModHandler.getRecipeOutput(DummyWorld.INSTANCE, plankStack, plankStack, plankStack).getRight();

            if (!slabStack.isEmpty()) {
                CUTTER_RECIPES.recipeBuilder()
                        .inputs(GTUtility.copyAmount(1, plankStack))
                        .outputs(GTUtility.copyAmount(2, slabStack))
                        .duration(200).EUt(VA[ULV])
                        .buildAndRegister();

                ModHandler.addShapedRecipe(slabStack.getDisplayName() + "_saw", GTUtility.copyAmount(2, slabStack), "sS", 'S', GTUtility.copyAmount(1, plankStack));
            }

            if (ConfigHolder.recipes.harderCharcoalRecipe) {
                ItemStack outputStack = FurnaceRecipes.instance().getSmeltingResult(stack);
                if (outputStack.getItem() == Items.COAL && outputStack.getItemDamage() == 1) {
                    ModHandler.removeFurnaceSmelting(stack);
                }
            }
        }
    }
}
