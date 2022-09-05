package gregtech.common.items.behaviors;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.worldgen.bedrockOres.BedrockOreVeinHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class PrimitiveDrillBehavior extends AbstractUsableBehaviour {

    private final static int COOLDOWN = 100;

    public PrimitiveDrillBehavior(int totalUses) {
        super(totalUses);
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if(getUsesLeft(stack) > 0) {
            IBlockState blockState = world.getBlockState(pos);
            if(OreDictUnifier.getOreDictionaryNames(blockState.getBlock().getItem(world, pos, blockState)).contains("stone")) {
                BedrockOreVeinHandler.OreVeinWorldEntry entry = BedrockOreVeinHandler.getOreVeinWorldEntry(world, pos.getX() / 16, pos.getY() / 16, 0);
                if (entry != null && entry.getOperationsRemaining() > 0) {
                    BedrockOreVeinHandler.depleteVein(world, pos.getX() / 16, pos.getY() / 16, 1, 0, false);
                    player.addItemStackToInventory(OreDictUnifier.get(OrePrefix.crushed, entry.getDefinition().getStoredOres().get(0), 1));
                    player.getCooldownTracker().setCooldown(stack.getItem(), COOLDOWN);
                    useItemDurability(player, hand, stack, ItemStack.EMPTY);
                    return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
                }
            }
        }
        return ActionResult.newResult(EnumActionResult.FAIL, stack);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        super.addInformation(itemStack, lines);
        lines.add(I18n.format("metaitem.drill.primitive.uses", getUsesLeft(itemStack)));
    }
}
