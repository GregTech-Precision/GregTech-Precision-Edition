package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.metaitem.stats.IItemMaxStackSizeProvider;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class DrillHeadBehaviour extends AbstractMaterialPartBehavior implements IItemMaxStackSizeProvider {

    public int getDrillHeadLevel(ItemStack stack){
        Material material = getPartMaterial(stack);
        return material.getHarvestLevel()-1;
    }

    public int getDrillHeadDurabilityPercent(ItemStack itemStack) {
        return 100 - 100 * getPartDamage(itemStack) / getPartMaxDurability(itemStack);
    }

    public void applyDrillHeadDamage(ItemStack itemStack, int damageApplied) {
        int drillHeadDurability = getPartMaxDurability(itemStack);
        int resultDamage = getPartDamage(itemStack) + damageApplied;
        if (resultDamage >= drillHeadDurability) {
            itemStack.shrink(1);
        } else {
            setPartDamage(itemStack, resultDamage);
        }
    }

    @Override
    public void addInformation(ItemStack stack, List<String> lines) {
        super.addInformation(stack, lines);
        lines.add("Drill Head Level: " + getDrillHeadLevel(stack));
        lines.add("Durability: " + getDrillHeadDurabilityPercent(stack));
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack, int defaultValue) {
        return 1;
    }

    @Override
    public int getPartMaxDurability(ItemStack itemStack) {
        Material material = getPartMaterial(itemStack);
        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        return property == null ? -1 : 800 * (int) Math.pow(property.getToolDurability(), 0.65);
    }

    @Nullable
    public static DrillHeadBehaviour getInstanceFor(@Nonnull ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof MetaItem))
            return null;

        MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) itemStack.getItem()).getItem(itemStack);
        if (valueItem == null)
            return null;

        IItemDurabilityManager durabilityManager = valueItem.getDurabilityManager();
        if (!(durabilityManager instanceof DrillHeadBehaviour))
            return null;

        return (DrillHeadBehaviour) durabilityManager;
    }
}
