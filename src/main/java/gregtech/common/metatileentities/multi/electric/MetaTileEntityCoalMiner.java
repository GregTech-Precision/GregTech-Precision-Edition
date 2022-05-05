package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.FluidDrillLogic;
import gregtech.api.capability.impl.miner.CoalMinerLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Collections;

public class MetaTileEntityCoalMiner extends MetaTileEntityMiner {

    protected CoalMinerLogic minerLogic;
    public MetaTileEntityCoalMiner(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.minerLogic = new CoalMinerLogic(this);
    }

    @Override
    protected void initializeAbilities() {
        this.exportItems = new ItemStackHandler(1);
        this.importItems = new ItemStackHandler(1);
    }

    @Override
    protected void resetTileAbilities() {
        this.exportItems = new ItemStackHandler(1);
        this.importItems = new ItemStackHandler(1);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 166)
                .slot(exportItems, 0, 18, 18, true, false)
                .slot(importItems, 0, 54, 18, true, true)
                .build(((IUIHolder) this), entityPlayer);
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCoalMiner(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("##F##", "##F##", "##F##")
                .aisle("#####", "##F##", "#####")
                .aisle("F###F", "FFDFF", "F#S#F")
                .aisle("#####", "##F##", "#####")
                .aisle("##F##", "##F##", "##F##")
                .where('S', selfPredicate())
                .where('F', states(MetaBlocks.FRAMES.get(Materials.Iron).getBlock(Materials.Iron)))
                .where('D', any()) //DRILL HEAD
                .where('#', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MUFFLER_OVERLAY.render(renderState, translation, pipeline);
    }
}
