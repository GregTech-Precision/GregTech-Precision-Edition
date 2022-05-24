package gregtech.common.items.behaviors;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.hardware.Hardware;
import gregtech.api.util.GTUtility;
import gregtech.common.terminal.hardware.BatteryHardware;
import gregtech.common.tools.DamageValues;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import precisioncore.api.capability.IAddresable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class LaptopBehavior implements ItemUIFactory, IItemBehaviour {

    private int frequency = 0;
    private MetaTileEntityHolder holder;
    private EntityPlayer player;
    private boolean isInUse = false;

    public LaptopBehavior() {}

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if(!world.isRemote && !world.isAirBlock(pos)) {
            ItemStack itemStack = player.getHeldItem(hand);

            IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if(!(electricItem != null && electricItem.getCharge() > 0))
                return EnumActionResult.FAIL;

            TileEntity blockClicked = world.getTileEntity(pos);

            if (blockClicked instanceof MetaTileEntityHolder) {
                MetaTileEntityHolder holder = ((MetaTileEntityHolder) blockClicked);
                if (holder.getMetaTileEntity() instanceof IAddresable) {
                    this.player = player;
                    this.holder = holder;
                    PlayerInventoryHolder inventoryHolder = new PlayerInventoryHolder(player, hand);
                    inventoryHolder.openUI();
                    return EnumActionResult.SUCCESS;
                }
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    public void onUpdate(ItemStack itemStack, Entity entity) {
        if(!isInUse)
            return;

        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);

        if(electricItem == null)
            return;

        if (electricItem.discharge(128, 999, true, false, true) > 0) {
            electricItem.discharge(128, 999, true, false, false);
        } else {
            player.closeScreen();
        }
    }

    private void changeFrequency(int change){
        frequency+=change;
    }

    private void setFrequency(Widget.ClickData data){
        ((IAddresable) holder.getMetaTileEntity()).setNetAddress(player, frequency);
    }

    private void getFrequencyString(List<ITextComponent> textList){
        textList.add(new TextComponentTranslation("laptop.frequency", frequency));
    }

    private void getNetAddressString(List<ITextComponent> textList) {
        String netAddress = "null";
        if(((IAddresable) holder.getMetaTileEntity()).getNetAddress() != null)
            netAddress = ((IAddresable) holder.getMetaTileEntity()).getNetAddress().toString().substring(0,8);
        textList.add(new TextComponentTranslation("laptop.address", netAddress));
    }

    private void openListener(){
        isInUse = true;
        frequency = ((IAddresable) holder.getMetaTileEntity()).getFrequency();
    }

    private void closeListener(){
        frequency = 0;
        holder = null;
        player = null;
        isInUse = false;
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder invHolder, EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND_FREQUENCY, 176, 166)
                .bindOpenListener(this::openListener)
                .bindCloseListener(this::closeListener)
                .bindPlayerInventory(entityPlayer.inventory)
                .widget(new IncrementButtonWidget(8, 8, 16, 16, 1, 4, 16, 64, this::changeFrequency))
                .widget(new IncrementButtonWidget(8, 26, 16, 16, -1, -4, -16, -64, this::changeFrequency))
                .widget(new ClickButtonWidget(8, 55, 16, 16, "=", this::setFrequency))
                .widget(new AdvancedTextWidget(40, 12, this::getFrequencyString, 0xFFFFFF))
                .widget(new AdvancedTextWidget(40, 22, this::getNetAddressString, 0xFFFFFF))
                .build(invHolder, entityPlayer);
    }
}
