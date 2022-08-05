package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiFluidHatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityAssemblyLine extends RecipeMapMultiblockController {

    public MetaTileEntityAssemblyLine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.ASSEMBLY_LINE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAssemblyLine(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("FIF", "RTR", "SAG", " Y ")
                .aisle("FIF", "RTR", "DAG", " Y ")
                .aisle("FIF", "RTR", "GAG", " Y ").setRepeatable(2, 14)
                .aisle("FOF", "RTR", "GAG", " Y ")
                .where('S', selfPredicate())
                .where('F', states(getCasingState())
                        .or(autoAbilities(false, true, false, false, false, false, false))

                        // if ordered fluids are enabled, ban multi fluid hatches, otherwise allow all types
                        .or(ConfigHolder.machines.enableResearch && ConfigHolder.machines.orderedAssembly && ConfigHolder.machines.orderedFluidAssembly ?
                                metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.IMPORT_FLUIDS).stream()
                                .filter(mte -> !(mte instanceof MetaTileEntityMultiFluidHatch)).toArray(MetaTileEntity[]::new)).setMaxGlobalLimited(4) :
                                abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4)))

                .where('O', abilities(MultiblockAbility.EXPORT_ITEMS).addTooltips("gregtech.multiblock.pattern.location_end"))
                .where('Y', states(getCasingState()).or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3)))
                .where('I', metaTileEntities(MetaTileEntities.ITEM_IMPORT_BUS[0]))
                .where('G', states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('A', states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_CONTROL)))
                .where('R', states(MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS)))
                .where('T', states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_LINE_CASING)))

                // if research is enabled, require the data hatch, otherwise use a grate instead
                .where('D', ConfigHolder.machines.enableResearch ? abilities(MultiblockAbility.DATA_ACCESS_HATCH) :
                        states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where(' ', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    protected Function<BlockPos, Integer> multiblockPartSorter() {
        // ensure the inputs are always in order
        EnumFacing frontFacing = getFrontFacing();
        if (frontFacing == EnumFacing.NORTH) return pos -> -pos.getX();
        if (frontFacing == EnumFacing.SOUTH) return BlockPos::getX;
        if (frontFacing == EnumFacing.EAST) return pos -> -pos.getZ();
        if (frontFacing == EnumFacing.WEST) return BlockPos::getZ;
        return BlockPos::hashCode;
    }

    @Override
    public boolean checkRecipe(@Nonnull Recipe recipe, boolean consumeIfSuccess) {
        if (!ConfigHolder.machines.enableResearch) return true;

        List<IDataAccessHatch> dataHatches = getAbilities(MultiblockAbility.DATA_ACCESS_HATCH);
        for (IDataAccessHatch hatch : dataHatches) {
            // creative hatches do not need to check, they always have the recipe
            if (hatch.isCreative()) return true;

            for (Recipe r : hatch.getAvailableRecipes()) {
                if (ConfigHolder.machines.orderedAssembly) {
                    List<GTRecipeInput> inputs = r.getInputs();
                    List<IItemHandlerModifiable> itemInputInventory = getOrderedItemInputs();
                    // slot count is not enough, so don't try to match it
                    if (itemInputInventory.size() < inputs.size()) continue;

                    boolean failedItemInputs = false;
                    for (int i = 0; i < inputs.size(); i++) {
                        if (!inputs.get(i).acceptsStack(itemInputInventory.get(i).getStackInSlot(0))) {
                            failedItemInputs = true;
                            break;
                        }
                    }
                    // if items were good, try the fluids
                    if (!failedItemInputs) {
                        if (ConfigHolder.machines.orderedFluidAssembly) {
                            inputs = r.getFluidInputs();
                            List<IFluidTank> fluidInputInventory = getOrderedFluidInputs();

                            // slot count is not enough, so don't try to match it
                            if (fluidInputInventory.size() < inputs.size()) continue;

                            boolean failedFluidInputs = false;
                            for (int i = 0; i < inputs.size(); i++) {
                                if (!inputs.get(i).acceptsFluid(fluidInputInventory.get(i).getFluid())) {
                                    failedFluidInputs = true;
                                    break;
                                }
                            }
                            // fluids are good, return true
                            if (!failedFluidInputs) return true;
                        } else {
                            // fluid checking is off, so return true as items are good
                            return true;
                        }
                    }
                } else if (r.equals(recipe)) {
                    // no ordering involved, so return true if the recipes match
                    return true;
                }
            }
        }
        return false;
    }

    protected List<IItemHandlerModifiable> getOrderedItemInputs() {
        // order is reversed when facing the negative direction. Positive it is in order, so just return.
//        if (getFrontFacing().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
//            // getAbilities is unmodifiable so we need to make a copy
//            List<IItemHandlerModifiable> inputItems = new ObjectArrayList<>();
//            inputItems.addAll(getAbilities(MultiblockAbility.IMPORT_ITEMS));
//            Collections.reverse(inputItems);
//
//            // need to remove all the trailing empty hatches at the front after reversing
//            boolean wasPreviousAir = true;
//            for (int i = 0; i < inputItems.size(); i++) {
//                if (!wasPreviousAir) break;
//                if (inputItems.get(i).getStackInSlot(0).isEmpty()) {
//                    inputItems.remove(i);
//                    i--;
//                } else {
//                    wasPreviousAir = false;
//                }
//            }
//            return inputItems;
//        }
        return getAbilities(MultiblockAbility.IMPORT_ITEMS);
    }

    protected List<IFluidTank> getOrderedFluidInputs() {
        // order is reversed when facing the negative direction. Positive it is in order, so just return.
//        if (getFrontFacing().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
//            // getAbilities is unmodifiable so we need to make a copy
//            List<IFluidTank> inputFluids = new ObjectArrayList<>();
//            inputFluids.addAll(getAbilities(MultiblockAbility.IMPORT_FLUIDS));
//            Collections.reverse(inputFluids);
//
//            // need to remove all the trailing empty hatches at the front after reversing
//            boolean wasPreviousAir = true;
//            for (int i = 0; i < inputFluids.size(); i++) {
//                if (!wasPreviousAir) break;
//                if (inputFluids.get(i).getFluid() == null) {
//                    inputFluids.remove(i);
//                    i--;
//                } else {
//                    wasPreviousAir = false;
//                }
//            }
//            return inputFluids;
//        }
        return getAbilities(MultiblockAbility.IMPORT_FLUIDS);
    }
}
