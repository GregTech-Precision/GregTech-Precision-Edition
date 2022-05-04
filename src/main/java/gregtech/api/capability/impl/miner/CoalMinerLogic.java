package gregtech.api.capability.impl.miner;

import gregtech.common.metatileentities.multi.electric.MetaTileEntityBasicMiner;

public class CoalMinerLogic extends AbstractMinerLogic {



    public CoalMinerLogic(MetaTileEntityBasicMiner metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean consumeEnergy(boolean simulate) {
        return false;
    }

    @Override
    protected boolean checkCanDrain() {
        return false;
    }
}
