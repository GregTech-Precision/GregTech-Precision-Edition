package gregtech.api.capability;

import net.minecraft.item.ItemStack;

public interface IDrillHeadHandler {

    boolean hasDrillHead();

    int getDrillEfficiency();

    void applyDrillHeadDamage(int damageApplied);
}
