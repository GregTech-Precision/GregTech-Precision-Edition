package gregtech.api.capability.impl.miner;

import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityBasicMiner;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityCoalMiner;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityMiner;
import net.minecraft.item.ItemStack;

public class CoalMinerLogic extends AbstractMinerLogic {

    private int fuelAmount = 0;

    public CoalMinerLogic(MetaTileEntityMiner metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected MetaTileEntityCoalMiner getMetaTileEntity() {
        return ((MetaTileEntityCoalMiner) super.getMetaTileEntity());
    }

    @Override
    protected boolean consumeEnergy(boolean simulate) {
        if(fuelAmount == 0) {
            ItemStack fuel = getMetaTileEntity().getImportItems().getStackInSlot(0);
            int burnTime = fuel.getItem().getItemBurnTime(fuel);
            if(burnTime > 0){
                if(simulate) return true;
                fuelAmount = burnTime;
                fuel.setCount(fuel.getCount()-1);
                return true;
            }
        } else {
            if (simulate) {
                return fuelAmount - 1 > 0;
            } else {
                return fuelAmount-- > 0;
            }
        }
        return false;
    }

    protected boolean checkCanDrain() {
        if (!consumeEnergy(true)) {
            if (progressTime >= 2) {
                if (ConfigHolder.machines.recipeProgressLowEnergy)
                    this.progressTime = 1;
                else
                    this.progressTime = Math.max(1, progressTime - 2);
            }
            return false;
        }

        if (getMetaTileEntity().fillInventory(OreDictUnifier.get(OrePrefix.crushed, vein.getDefinition().getNextOre(), 4), true)) {
            this.isInventoryFull = false;
            return true;
        }
        this.isInventoryFull = true;

        if (isActive()) {
            setActive(false);
            setWasActiveAndNeedsUpdate(true);
        }
        return false;
    }
}
