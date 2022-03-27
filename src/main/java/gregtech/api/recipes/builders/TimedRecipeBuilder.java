package gregtech.api.recipes.builders;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.api.util.ValidationResult;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;

public class TimedRecipeBuilder extends RecipeBuilder<TimedRecipeBuilder> {

    protected HashMap<Integer, HashSet<ItemStack>> timedOutputs;
    protected HashMap<Integer, HashSet<FluidStack>> timedFluidOutputs;

    public TimedRecipeBuilder(){
        super();
        this.timedOutputs = new HashMap<>();
        this.timedFluidOutputs = new HashMap<>();
    }

    public TimedRecipeBuilder(RecipeBuilder<TimedRecipeBuilder> recipeBuilder){
        super(recipeBuilder);
        if(recipeBuilder instanceof TimedRecipeBuilder) {
            this.timedOutputs = ((TimedRecipeBuilder) recipeBuilder).getTimedOutputs();
            this.timedFluidOutputs = ((TimedRecipeBuilder) recipeBuilder).getTimedFluidOutputs();
        }
    }

    public TimedRecipeBuilder(Recipe recipe, RecipeMap<TimedRecipeBuilder> recipeMap){
        super(recipe, recipeMap);
        this.timedOutputs = recipe.getTimedOutputs();
        this.timedFluidOutputs = recipe.getTimedFluidOutputs();
    }

    @Override
    public TimedRecipeBuilder copy() {
        return new TimedRecipeBuilder(this);
    }

    //timed outputs

    public TimedRecipeBuilder timedOutput(OrePrefix orePrefix, Material material, int tick) {
        return timedOutput(orePrefix, material, 1, tick);
    }

    public TimedRecipeBuilder timedOutput(OrePrefix orePrefix, Material material, int count, int tick) {
        return timedOutputs(OreDictUnifier.get(orePrefix, material, count), tick);
    }

    public TimedRecipeBuilder timedOutput(Item item, int tick) {
        return timedOutput(item, 1, tick);
    }

    public TimedRecipeBuilder timedOutput(Item item, int count, int tick) {
        return timedOutputs(new ItemStack(item, count), tick);
    }

    public TimedRecipeBuilder timedOutput(Item item, int count, int meta, int tick) {
        return timedOutputs(new ItemStack(item, count, meta), tick);
    }

    public TimedRecipeBuilder timedOutput(Block item, int tick) {
        return timedOutput(item, 1, tick);
    }

    public TimedRecipeBuilder timedOutput(Block item, int count, int tick) {
        return timedOutputs(new ItemStack(item, count), tick);
    }

    public TimedRecipeBuilder timedOutput(MetaItem<?>.MetaValueItem item, int count, int tick) {
        return timedOutputs(item.getStackForm(count), tick);
    }

    public TimedRecipeBuilder timedOutput(MetaItem<?>.MetaValueItem item, int tick) {
        return timedOutput(item, 1, tick);
    }

    public TimedRecipeBuilder timedOutput(MetaTileEntity mte, int tick) {
        return timedOutput(mte, 1, tick);
    }

    public TimedRecipeBuilder timedOutput(MetaTileEntity mte, int count, int tick) {
        return timedOutputs(mte.getStackForm(count), tick);
    }

    protected TimedRecipeBuilder timedOutputs(ItemStack item, int tick) {
        if(tick < 0){
            GTLog.logger.error("Timed output can't use values less than zero");
            GTLog.logger.error("Stacktrace: ", new IllegalArgumentException());
        }
        else {
            if (!this.timedOutputs.containsKey(tick)) this.timedOutputs.put(tick, new HashSet<>());
            this.timedOutputs.get(tick).add(item);
        }
        return this;
    }

    public TimedRecipeBuilder clearTimedOutputs(){
        this.timedOutputs.clear();
        return this;
    }

    public TimedRecipeBuilder timedFluidOutput(FluidStack fluid, int tick){
        if(tick < 0){
            GTLog.logger.error("Timed output can't use values less than zero");
            GTLog.logger.error("Stacktrace: ", new IllegalArgumentException());
        }else {
            if (!this.timedFluidOutputs.containsKey(tick)) this.timedFluidOutputs.put(tick, new HashSet<>());
            this.timedFluidOutputs.get(tick).add(fluid);
        }
        return this;
    }

    public TimedRecipeBuilder clearTimedFluidOutputs(){
        this.timedFluidOutputs.clear();
        return this;
    }

    public HashMap<Integer, HashSet<ItemStack>> getTimedOutputs() {
        return timedOutputs;
    }

    public HashMap<Integer, HashSet<FluidStack>> getTimedFluidOutputs() {
        return timedFluidOutputs;
    }

    @Override
    @Nonnull
    public ValidationResult<Recipe> build() {
        return ValidationResult.newResult(finalizeAndValidate(),
                new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs, timedOutputs, timedFluidOutputs, duration, EUt, hidden, false));
    }
}
