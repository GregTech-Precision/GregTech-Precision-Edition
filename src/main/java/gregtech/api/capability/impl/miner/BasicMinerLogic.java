package gregtech.api.capability.impl.miner;

import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityBasicMiner;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityMiner;

public class BasicMinerLogic extends AbstractMinerLogic {

    protected boolean hasNotEnoughEnergy;

    public BasicMinerLogic(MetaTileEntityBasicMiner metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected MetaTileEntityBasicMiner getMetaTileEntity() {
        return ((MetaTileEntityBasicMiner) metaTileEntity);
    }

    @Override
    protected boolean consumeEnergy(boolean simulate)  {
        return getMetaTileEntity().drainEnergy(simulate);
    }

    @Override
    public boolean isWorking() {
        return super.isWorking() && !hasNotEnoughEnergy;
    }

    protected boolean checkCanDrain() {
        if (!consumeEnergy(true)) {
            if (progressTime >= 2) {
                if (ConfigHolder.machines.recipeProgressLowEnergy)
                    this.progressTime = 1;
                else
                    this.progressTime = Math.max(1, progressTime - 2);

                hasNotEnoughEnergy = true;
            }
            return false;
        }

        if (this.hasNotEnoughEnergy && getMetaTileEntity().getEnergyInputPerSecond() > 19L * GTValues.VA[getMetaTileEntity().getEnergyTier()]) {
            this.hasNotEnoughEnergy = false;
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
