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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
    public int getDrillEfficiency() {
        return 1;
    }

    @Override
    public MultiblockAbility<IDrillHeadHandler> getAbility() {
        return MultiblockAbility.DRILL_HANDLER;
    }

    @Override
    public void registerAbilities(List<IDrillHeadHandler> abilityList) {
        abilityList.add(this);
    }

    private class InventoryDrillHandler extends ItemStackHandler {

        public InventoryDrillHandler(){
            super(1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }
    }
}
