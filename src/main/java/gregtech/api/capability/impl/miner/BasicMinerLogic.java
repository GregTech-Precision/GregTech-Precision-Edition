package gregtech.api.capability.impl.miner;

import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityBasicMiner;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

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
    protected boolean consumeFluid(boolean simulate) {
        if(vein.getDefinition().getSpecialFluids().isEmpty())
            return true;
        for(FluidStack fluid : vein.getDefinition().getSpecialFluids()){
            if(!getMetaTileEntity().drainFluid(fluid, simulate)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isWorking() {
        return super.isWorking() && !hasNotEnoughEnergy;
    }

    @Override
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

        List<ItemStack> ores = new ArrayList<>();
        for(Material ore : vein.getDefinition().getStoredOres())
            ores.add(OreDictUnifier.get(OrePrefix.crushed, ore, getOrePerCycle()));

        if (getMetaTileEntity().fillInventory(ores, true)) {
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
