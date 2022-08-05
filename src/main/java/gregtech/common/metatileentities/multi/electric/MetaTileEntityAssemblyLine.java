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
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

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
                .aisle("FIF", "RTR", "SAG", "#Y#")
                .aisle("FIF", "RTR", "DAG", "#Y#")
                .aisle("FIF", "RTR", "GAG", "#Y#").setRepeatable(2, 14)
                .aisle("FOF", "RTR", "GAG", "#Y#")
                .where('S', selfPredicate())
                .where('F', states(getCasingState())
                        .or(autoAbilities(false, true, false, false, false, false, false))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4)))
                .where('O', abilities(MultiblockAbility.EXPORT_ITEMS).addTooltips("gregtech.multiblock.pattern.location_end"))
                .where('Y', states(getCasingState()).or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3)))
                .where('I', metaTileEntities(MetaTileEntities.ITEM_IMPORT_BUS[0]))
                .where('G', states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('A', states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_CONTROL)))
                .where('R', states(MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS)))
                .where('T', states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_LINE_CASING)))
                .where('D', ConfigHolder.machines.enableResearch ? abilities(MultiblockAbility.DATA_ACCESS_HATCH) :
                        states(MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('#', any())
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
    public boolean checkRecipe(@Nonnull Recipe recipe, boolean consumeIfSuccess) {
        if (!ConfigHolder.machines.enableResearch) return true;

        List<IDataAccessHatch> dataHatches = getAbilities(MultiblockAbility.DATA_ACCESS_HATCH);
        for (IDataAccessHatch hatch : dataHatches) {
            if (hatch.isCreative()) return true;
            for (Recipe r : hatch.getAvailableRecipes()) {
                if (ConfigHolder.machines.orderedAssembly) {

                } else if (r.equals(recipe)) return true;
            }
        }
        return false;
    }
}
