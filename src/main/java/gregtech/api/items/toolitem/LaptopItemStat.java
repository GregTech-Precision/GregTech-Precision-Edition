package gregtech.api.items.toolitem;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.tool.ILaptopItem;
import gregtech.api.capability.tool.IScrewdriverItem;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class LaptopItemStat implements IItemCapabilityProvider {

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new LaptopItemStat().createProvider(itemStack);
    }

    private static class CapabilityProvider extends AbstractToolItemCapabilityProvider<ILaptopItem> implements ILaptopItem {

        public CapabilityProvider(ItemStack itemStack) {
            super(itemStack);
        }

        @Override
        protected Capability<ILaptopItem> getCapability() {
            return GregtechCapabilities.CAPABILITY_LAPTOP;
        }
    }
}
