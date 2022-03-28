package gregtech.loaders.recipe.handlers;

import com.google.common.collect.ImmutableMap;
import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.builders.IntCircuitRecipeBuilder;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;

import static gregtech.api.recipes.RecipeMaps.PACKER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.WIRE_ASSEMBLER;
import static gregtech.api.unification.ore.OrePrefix.*;

public class WireCombiningHandler {

    private static final OrePrefix[] WIRE_DOUBLING_ORDER = new OrePrefix[]{
            wireGtSingle, wireGtDouble, wireGtQuadruple, wireGtOctal, wireGtHex
    };

    private static final Map<OrePrefix, OrePrefix> cableToWireMap = ImmutableMap.of(
            cableGtSingle, wireGtSingle,
            cableGtDouble, wireGtDouble,
            cableGtQuadruple, wireGtQuadruple,
            cableGtOctal, wireGtOctal,
            cableGtHex, wireGtHex
    );

    public static void register() {

        // Generate manual recipes for combining Wires/Cables
        for (OrePrefix wirePrefix : WIRE_DOUBLING_ORDER) {
            wirePrefix.addProcessingHandler(PropertyKey.WIRE, WireCombiningHandler::generateWireCombiningRecipe);
        }

        // Generate Cable -> Wire recipes in the unpacker
        for (OrePrefix cablePrefix : cableToWireMap.keySet()) {
            cablePrefix.addProcessingHandler(PropertyKey.WIRE, WireCombiningHandler::processCableStripping);
        }
    }

    private static void generateWireCombiningRecipe(OrePrefix wirePrefix, Material material, WireProperties property) {
        int wireIndex = ArrayUtils.indexOf(WIRE_DOUBLING_ORDER, wirePrefix);
        if(wireIndex == 4 ) return;

        //Generate combining recipes in wire assembler
        WIRE_ASSEMBLER.recipeBuilder()
                .circuitMeta(1<<wireIndex)
                .input(wirePrefix, material, (wireIndex == 0)?2:1<<wireIndex)
                .output(WIRE_DOUBLING_ORDER[wireIndex+1], material)
                .EUt(property.getVoltage()).duration(20).buildAndRegister();

        //Handcrafting recipes under 2048
        if(property.getVoltage() <= GTValues.V[4])
        {
            if (wireIndex < WIRE_DOUBLING_ORDER.length - 1) {
                ModHandler.addShapelessRecipe(String.format("%s_wire_%s_doubling", material, wirePrefix),
                        OreDictUnifier.get(WIRE_DOUBLING_ORDER[wireIndex + 1], material),
                        new UnificationEntry(wirePrefix, material),
                        new UnificationEntry(wirePrefix, material));
            }

            if (wireIndex > 0) {
                ModHandler.addShapelessRecipe(String.format("%s_wire_%s_splitting", material, wirePrefix),
                        OreDictUnifier.get(WIRE_DOUBLING_ORDER[wireIndex - 1], material, 2),
                        new UnificationEntry(wirePrefix, material));
            }

            if (wireIndex < 3) {
                ModHandler.addShapelessRecipe(String.format("%s_wire_%s_quadrupling", material, wirePrefix),
                        OreDictUnifier.get(WIRE_DOUBLING_ORDER[wireIndex + 2], material),
                        new UnificationEntry(wirePrefix, material),
                        new UnificationEntry(wirePrefix, material),
                        new UnificationEntry(wirePrefix, material),
                        new UnificationEntry(wirePrefix, material));
            }
        }
    }

    private static void processCableStripping(OrePrefix prefix, Material material, WireProperties property) {
        PACKER_RECIPES.recipeBuilder()
                .input(prefix, material)
                .output(cableToWireMap.get(prefix), material)
                .output(plate, Materials.Rubber, (int) (prefix.secondaryMaterials.get(0).amount / GTValues.M))
                .duration(100).EUt(GTValues.VA[GTValues.ULV])
                .buildAndRegister();
    }
}
