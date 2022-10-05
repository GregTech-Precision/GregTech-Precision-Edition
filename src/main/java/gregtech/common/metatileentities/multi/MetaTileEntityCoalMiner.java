package gregtech.common.metatileentities.multi;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.miner.CoalMinerLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityMiner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class MetaTileEntityCoalMiner extends MetaTileEntityMiner {

    private int fuelAmount = 0;

    public MetaTileEntityCoalMiner(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.minerLogic = new CoalMinerLogic(this);
        this.exportItems = new ItemStackHandler(1);
        this.importItems = new ItemStackHandler(1);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 166)
                .slot(importItems, 0, 18, 18, true, true, GuiTextures.SLOT)
                .slot(exportItems, 0, 54, 18, true, false, GuiTextures.SLOT)
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
    }

    public boolean drainEnergy(boolean simulate) {
        if(fuelAmount == 0) {
            ItemStack fuel = this.importItems.getStackInSlot(0);
            int burnTime = TileEntityFurnace.getItemBurnTime(fuel)/4;
            if(burnTime > 0){
                if(simulate) return true;
                fuelAmount = burnTime;
                fuel.setCount(fuel.getCount()-1);
                return true;
            }
        } else {
            if (simulate) {
                return fuelAmount - 1 >= 0;
            } else {
                return fuelAmount-- >= 0;
            }
        }
        return false;
    }

    @Override
    public boolean fillInventory(List<ItemStack> items, boolean simulate) {
        TileEntity storage = getWorld().getTileEntity(getPos().offset(EnumFacing.UP));
        if(storage != null && storage.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)){
            return GTTransferUtils.addItemsToItemHandler(storage.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP), simulate, items);
        }
        return super.fillInventory(items, simulate);
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
                .where('F', states(MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel)))
                .where('D', abilities(MultiblockAbility.DRILL_HOLDER))
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
