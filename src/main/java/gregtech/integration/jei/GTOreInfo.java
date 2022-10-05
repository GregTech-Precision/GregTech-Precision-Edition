package gregtech.integration.jei;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.worldgen.config.BedrockOreDepositDefinition;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.FluidStack;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;

public class GTOreInfo implements IRecipeWrapper {

    private final BedrockOreDepositDefinition definition;
    private final int layer;
    private final String name;
    private final int weight;
    private final List<List<ItemStack>> groupedOutputsAsItemStacks = new ArrayList<>();
    private final List<List<FluidStack>> groupedInputAsFluidStacks = new ArrayList<>();
    private final Function<Biome, Integer> biomeFunction;

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
        this.weight = definition.getWeight();
        for(Material material : definition.getStoredOres()){
            groupedOutputsAsItemStacks.add(Collections.singletonList(OreDictUnifier.get(OrePrefix.crushed, material, 1)));
        }
        for(FluidStack stack : definition.getSpecialFluids()){
            groupedInputAsFluidStacks.add(Collections.singletonList(stack));
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setOutputLists(VanillaTypes.ITEM, groupedOutputsAsItemStacks);
        ingredients.setInputLists(VanillaTypes.FLUID, groupedInputAsFluidStacks);
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
        tooltip.add(I18n.format("gregtech.jei.ore.ore_weight", getOreWeight(slotIndex)));
    }

    public void addFluidTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip){
        tooltip.add("Fluid is consumed every cycle");
    }

    public int getOutputCount() {
        return groupedOutputsAsItemStacks.size();
    }

    public int getFluidInputCount() { return groupedInputAsFluidStacks.size(); }

    public String getVeinName() {
        return name;
    }

    public int getLayer() {
        return layer;
    }

    public int getWeight(){
        return weight;
    }

    public FluidStack getFluid(int index){
        return definition.getSpecialFluids().get(index);
    }

    public BedrockOreDepositDefinition getDefinition() {
        return definition;
    }

    public int getOreWeight(int index) {
        return index >= definition.getStoredOres().size() ? -1 : 100*definition.getOreWeight(definition.getStoredOres().get(index))/ definition.getAllOresWeight();
    }
}
