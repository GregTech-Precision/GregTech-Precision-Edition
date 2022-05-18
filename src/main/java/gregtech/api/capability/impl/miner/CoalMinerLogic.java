package gregtech.api.capability.impl.miner;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.MetaTileEntityCoalMiner;

public class CoalMinerLogic extends AbstractMinerLogic {

    private int fuelAmount = 0;

    public CoalMinerLogic(MetaTileEntityCoalMiner metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected MetaTileEntityCoalMiner getMetaTileEntity() {
        return ((MetaTileEntityCoalMiner) super.getMetaTileEntity());
    }

    @Override
    protected boolean consumeEnergy(boolean simulate) {
        return getMetaTileEntity().drainEnergy(simulate);
    }

    @Override
    protected boolean consumeFluid(boolean simulate) {
        return true;
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

        if (getMetaTileEntity().fillInventory(OreDictUnifier.get(OrePrefix.crushed, vein.getDefinition().getNextOre(), 1+getDrillEfficiency()), true)) {
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
