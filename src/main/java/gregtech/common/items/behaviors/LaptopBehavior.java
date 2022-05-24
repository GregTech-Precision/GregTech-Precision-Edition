package gregtech.common.items.behaviors;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.IncrementButtonWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.hardware.Hardware;
import gregtech.api.util.GTUtility;
import gregtech.common.terminal.hardware.BatteryHardware;
import gregtech.common.tools.DamageValues;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
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

public class LaptopBehavior extends AbstractUsableBehaviour implements ItemUIFactory {

    private int frequency = 0;
    private MetaTileEntityHolder holder;
    private EntityPlayer player;

    public LaptopBehavior() {
        super(1000);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if(!world.isRemote && !world.isAirBlock(pos)) {

            ItemStack itemStack = player.getHeldItem(hand);
            if(!GTUtility.doDamageItem(itemStack, DamageValues.DAMAGE_FOR_LAPTOP, true))
                return EnumActionResult.FAIL;

            IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if(!(electricItem != null && electricItem.getCharge() > 0))
                return EnumActionResult.FAIL;

            TileEntity blockClicked = world.getTileEntity(pos);

            if (blockClicked instanceof MetaTileEntityHolder) {
                MetaTileEntityHolder holder = ((MetaTileEntityHolder) blockClicked);
                if (holder.getMetaTileEntity() instanceof IAddresable) {
                    GTUtility.doDamageItem(itemStack, DamageValues.DAMAGE_FOR_LAPTOP, false);
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
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            electricItem.discharge(32, 999, true, false, false);
        }
    }

    private void changeFrequency(int change){
        frequency+=change;
    }

    private void setFrequency(Widget.ClickData data){
        ((IAddresable) holder.getMetaTileEntity()).setNetAddress(player, frequency);
    }

    private String getFrequencyString(){
        return "Frequency: " + frequency;
    }

    private String getNetAddressString(){
        String netAddress = "null";
        if(((IAddresable) holder.getMetaTileEntity()).getNetAddress() != null)
            netAddress = ((IAddresable) holder.getMetaTileEntity()).getNetAddress().toString().substring(0,8);
        return "Address: " + netAddress;
    }

    private void openListener(){
        frequency = ((IAddresable) holder.getMetaTileEntity()).getFrequency();
    }

    private void closeListener(){
        frequency = 0;
        holder = null;
        player = null;
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder invHolder, EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND_FREQUENCY, 176, 166)
                .bindOpenListener(this::openListener)
                .bindCloseListener(this::closeListener)
                .widget(new IncrementButtonWidget(8, 8, 16, 16, 1, 4, 16, 64, this::changeFrequency))
                .widget(new IncrementButtonWidget(8, 26, 16, 16, -1, -4, -16, -64, this::changeFrequency))
                .widget(new ClickButtonWidget(8, 55, 16, 16, "=", this::setFrequency))
                .widget(new SimpleTextWidget(70, 15, "", this::getFrequencyString))
                .widget(new SimpleTextWidget(80, 25, "", this::getNetAddressString))
                .build(invHolder, entityPlayer);
    }
}
