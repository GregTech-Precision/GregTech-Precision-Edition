package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDrillHeadHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.items.behaviors.DrillHeadBehaviour;
import gregtech.common.items.behaviors.TurbineRotorBehavior;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class MetaTileEntityDrillHeadHolder extends MetaTileEntityMultiblockPart implements IDrillHeadHandler, IMultiblockAbilityPart<IDrillHeadHandler> {

    private final InventoryDrillHandler drillHandler;

    public MetaTileEntityDrillHeadHolder(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 0);
        this.drillHandler = new InventoryDrillHandler();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDrillHeadHolder(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 166)
                .slot(this.drillHandler, 0, 18, 18, true, true)
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
    }

    @Override
    public boolean hasDrillHead() {
        return !drillHandler.getStackInSlot(0).isEmpty();
    }

    @Override
    public int getDrillEfficiency() {
        ItemStack stack = drillHandler.getStackInSlot(0);
        if(stack.isEmpty())
            return 0;
        return DrillHeadBehaviour.getInstanceFor(stack).getDrillHeadLevel(stack);
    }

    @Override
    public void applyDrillHeadDamage(int damageApplied) {
        ItemStack stack = drillHandler.getStackInSlot(0);
        if(stack.isEmpty())
            return;

        DrillHeadBehaviour.getInstanceFor(stack).applyDrillHeadDamage(stack, damageApplied);
    }

    @Override
    public MultiblockAbility<IDrillHeadHandler> getAbility() {
        return MultiblockAbility.DRILL_HANDLER;
    }

    @Override
    public void registerAbilities(List<IDrillHeadHandler> abilityList) {
        abilityList.add(this);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setTag("inventory", drillHandler.serializeNBT());
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        this.drillHandler.deserializeNBT(data.getCompoundTag("inventory"));
        super.readFromNBT(data);
    }

    private static class InventoryDrillHandler extends ItemStackHandler {

        public InventoryDrillHandler(){
            super(1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return DrillHeadBehaviour.getInstanceFor(stack) != null && super.isItemValid(slot, stack);
        }
    }
}
