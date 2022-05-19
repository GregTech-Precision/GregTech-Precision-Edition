package gregtech.integration.jei;

import com.google.common.collect.ImmutableList;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.worldgen.config.BedrockOreDepositDefinition;
import gregtech.common.blocks.BlockOre;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Loader;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;

import static gregtech.api.GTValues.*;

public class GTOreInfo implements IRecipeWrapper {

    private final BedrockOreDepositDefinition definition;
    private final int layer;
    private final String name;
    private final FluidStack specialFluid;
    private final int weight;
    private final List<List<ItemStack>> groupedOutputsAsItemStacks = new ArrayList<>();
    private final Function<Biome, Integer> biomeFunction;
    private final Map<Material, Integer> oreWeights;

    public GTOreInfo(BedrockOreDepositDefinition definition) {
        this.definition = definition;

        //Get layer of the vein
        this.layer = definition.getLayer();

        //Get the Name and trim unneeded information
        if (definition.getAssignedName() == null) {
            this.name = makePrettyName(definition.getDepositName());
        } else {
            this.name = definition.getAssignedName();
        }

        this.biomeFunction = definition.getBiomeWeightModifier();
        this.oreWeights = definition.getOreWeights();
        this.weight = definition.getWeight();
        for(Material material : definition.getStoredOres()){
            groupedOutputsAsItemStacks.add(Collections.singletonList(OreDictUnifier.get(OrePrefix.crushed, material, 1)));
        }
        this.specialFluid = definition.getSpecialFluid();
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setOutputLists(VanillaTypes.ITEM, groupedOutputsAsItemStacks);
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(Collections.singletonList(specialFluid)));
    }

    public String makePrettyName(String name) {
        FileSystem fs = FileSystems.getDefault();
        String separator = fs.getSeparator();

        //Remove the leading "folderName\"
        String[] tempName = name.split(Matcher.quoteReplacement(separator));
        //Take the last entry in case of nested folders
        String newName = tempName[tempName.length - 1];
        //Remove the ".json"
        tempName = newName.split("\\.");
        //Take the first entry
        newName = tempName[0];
        //Replace all "_" with a space
        newName = newName.replaceAll("_", " ");
        //Capitalize the first letter
        newName = newName.substring(0, 1).toUpperCase() + newName.substring(1);

        return newName;
    }

    //Creates a tooltip based on the specific slots
    public void addTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {

        //Surface Indicator slot
        if (slotIndex < getOutputCount()) {
            tooltip.addAll(createOreWeightingTooltip(slotIndex));
        }
    }

    //Creates a tooltip showing the Biome weighting of the ore vein
    public List<String> createBiomeTooltip() {

        Iterator<Biome> biomeIterator = Biome.REGISTRY.iterator();
        int biomeWeight;
        Map<Biome, Integer> modifiedBiomeMap = new HashMap<>();
        List<String> tooltip = new ArrayList<>();

        //Tests biomes against all registered biomes to find which biomes have had their weights modified
        while (biomeIterator.hasNext()) {

            Biome biome = biomeIterator.next();

            //Gives the Biome Weight
            biomeWeight = biomeFunction.apply(biome);
            //Check if the biomeWeight is modified
            if (biomeWeight != weight) {
                modifiedBiomeMap.put(biome, weight + biomeWeight);
            }
        }

        for (Map.Entry<Biome, Integer> entry : modifiedBiomeMap.entrySet()) {

            //Don't show non changed weights, to save room
            if (!(entry.getValue() == weight)) {
                //Cannot Spawn
                if (entry.getValue() <= 0) {
                    tooltip.add(I18n.format("gregtech.jei.ore.biome_weighting_no_spawn", entry.getKey().getBiomeName()));
                } else {
                    tooltip.add(I18n.format("gregtech.jei.ore.biome_weighting", entry.getKey().getBiomeName(), entry.getValue()));
                }
            }
        }


        return tooltip;
    }

    public List<String> createOreWeightingTooltip(int slotIndex) {
        List<String> tooltip = new ArrayList<>();
        tooltip.add(I18n.format("gregtech.jei.ore.ore_weight", getOreWeight(slotIndex)));
        return tooltip;
    }

    public int getOutputCount() {
        return groupedOutputsAsItemStacks.size();
    }

    public String getVeinName() {
        return name;
    }

    public int getLayer() {
        return layer;
    }

    public int getWeight(){
        return weight;
    }

    public BedrockOreDepositDefinition getDefinition() {
        return definition;
    }

    public int getOreWeight(int index) {
        return oreWeights.size() > index ? -1 : definition.getOreWeight(definition.getStoredOres().get(index));
    }
}
