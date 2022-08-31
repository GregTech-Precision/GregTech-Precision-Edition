package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.miner.BasicMinerLogic;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityBasicMiner extends MetaTileEntityMiner {

    protected IMultipleTankHandler inputFluidInventory;
    protected IItemHandlerModifiable outputItemInventory;
    protected IEnergyContainer energyContainer;

    public MetaTileEntityBasicMiner(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.minerLogic = new BasicMinerLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBasicMiner(metaTileEntityId);
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = super.createUITemplate(entityPlayer);
        builder.widget(new ClickButtonWidget(135, 104, 32, 18, "Next Layer", this::nextLayer));
        return builder;
    }

    private void nextLayer(Widget.ClickData clickData) {
        layer = (layer + 1) % 2;
    }

    protected void initializeAbilities() {
        this.inputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputItemInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    protected void resetTileAbilities() {
        this.inputFluidInventory = new FluidTankList(true);
        this.outputItemInventory = new ItemHandlerList(Lists.newArrayList());
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("#F#F#", "#F#F#", "#F#F#", "#####", "#####", "#####", "#####", "#####", "#####", "#####", "#####", "#####")
                .aisle("F###F", "F###F", "FMMMF", "#F#F#", "#F#F#", "#FCF#", "##F##", "##F##", "##F##", "#####", "#####", "#####")
                .aisle("#####", "#####", "#MDM#", "#####", "#####", "#CCC#", "#FCF#", "#FCF#", "#FCF#", "##F##", "##F##", "##F##")
                .aisle("F###F", "F###F", "FMSMF", "#F#F#", "#F#F#", "#FCF#", "##F##", "##F##", "##F##", "#####", "#####", "#####")
                .aisle("#F#F#", "#F#F#", "#F#F#", "#####", "#####", "#####", "#####", "#####", "#####", "#####", "#####", "#####")
                .where('S', selfPredicate())
                .where('M', states(getCasingState())
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3))
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4))
                        .or(autoAbilities(true, false)))
                .where('C', states(getCasingState()))
                .where('F', states(MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel)))
                .where('D', abilities(MultiblockAbility.DRILL_HOLDER))
                .where('#', any())
                .build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (!isStructureFormed())
            return;

        if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
            long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
            String voltageName = GTValues.VNF[GTUtility.getTierByVoltage(maxVoltage)];
            textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
        }

        if (!minerLogic.isWorkingEnabled()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));

        } else if (minerLogic.isActive()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.running"));
            int currentProgress = (int) (minerLogic.getProgressPercent() * 100);
            textList.add(new TextComponentTranslation("gregtech.multiblock.progress", currentProgress));
        } else {
            textList.add(new TextComponentTranslation("gregtech.multiblock.idling"));
        }

        if (!drainEnergy(true)) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.not_enough_energy").setStyle(new Style().setColor(TextFormatting.RED)));
        }

        if (minerLogic.isInventoryFull())
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.invfull").setStyle(new Style().setColor(TextFormatting.RED)));

        textList.add(new TextComponentString("Current Layer: "+layer));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.fluid_drilling_rig.description"));
        tooltip.add(I18n.format("gregtech.machine.fluid_drilling_rig.overclock"));
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.FLUID_RIG_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.minerLogic.isActive(), this.minerLogic.isWorkingEnabled());
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.minerLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.minerLogic.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public boolean fillInventory(List<ItemStack> items, boolean simulate) {
        return GTTransferUtils.addItemsToItemHandler(outputItemInventory, simulate, items);
    }

    public boolean drainFluid(FluidStack fluid, boolean simulate){
        return inputFluidInventory.drain(fluid, !simulate) != null;
    }

    public int getEnergyTier() {
        return GTUtility.getTierByVoltage(energyContainer.getInputVoltage());
    }

    public long getEnergyInputPerSecond() {
        return energyContainer.getInputPerSec();
    }

    public boolean drainEnergy(boolean simulate) {
        long energyToDrain = GTValues.VA[getEnergyTier()];
        long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-energyToDrain);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("layer", layer);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        this.layer = data.getInteger("layer");
        super.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(layer);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.layer = buf.readInt();
    }
}
