package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemUseManager;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.worldgen.bedrockOres.BedrockOreVeinHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.List;

public class PrimitiveDrillBehavior extends AbstractUsableBehaviour implements IItemUseManager {

    private final static int USE_DURATION = 100;
    private BlockPos usedPos;
    private World usedWorld;
    private EnumHand usedHand;

    public PrimitiveDrillBehavior(int totalUses) {
        super(totalUses);
    }

    @Override
    public EnumAction getUseAction(ItemStack stack) {
        return EnumAction.BLOCK;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if(getUsesLeft(stack) > 0) {
            IBlockState blockState = world.getBlockState(pos);
            if(OreDictUnifier.getOreDictionaryNames(blockState.getBlock().getItem(world, pos, blockState)).contains("stone")) {
                if (BedrockOreVeinHandler.getOperationsRemaining(world, pos.getX() / 16, pos.getY() / 16, 0) > 0) {
                    usedHand = hand;
                    usedPos = pos;
                    usedWorld = world;
                    return EnumActionResult.PASS;
                }
            }
        }
        return EnumActionResult.FAIL;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, EntityPlayer player) {
        BedrockOreVeinHandler.OreVeinWorldEntry entry = BedrockOreVeinHandler.getOreVeinWorldEntry(usedWorld, usedPos.getX() / 16, usedPos.getZ() / 16, 0);
        if(entry != null) {
            useItemDurability(player, usedHand, stack, ItemStack.EMPTY);
            player.addItemStackToInventory(OreDictUnifier.get(OrePrefix.crushed, entry.getDefinition().getStoredOres().get(0)));
            usedWorld = null;
            usedHand = null;
            usedPos = null;
        }
        return IItemUseManager.super.onItemUseFinish(stack, player);
    }

    @Override
    public void onItemUsingTick(ItemStack stack, EntityPlayer player, int count) {
        int percentage = (int) (100 * (1 - (float) count / (float) USE_DURATION));
        player.sendStatusMessage(new TextComponentString(percentage + "%"), true);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        super.addInformation(itemStack, lines);
        lines.add(I18n.format("metaitem.drill.primitive.uses", getUsesLeft(itemStack)));
    }
}
