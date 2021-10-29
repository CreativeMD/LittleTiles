package team.creative.littletiles.common.gui;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.littletiles.common.util.converation.StructureStringUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.mc.WorldUtils;
import team.creative.littletiles.common.item.ItemLittleRecipe;
import team.creative.littletiles.common.item.ItemLittleBlueprint;

public class SubContainerImport extends SubContainer {
    
    public final InventoryBasic slot = new InventoryBasic("slot", false, 1);
    
    public SubContainerImport(EntityPlayer player) {
        super(player);
    }
    
    @Override
    public void createControls() {
        addSlotToContainer(new Slot(slot, 0, 10, 10));
        addPlayerSlotsToContainer(player);
    }
    
    @Override
    public void onPacketReceive(NBTTagCompound nbt) {
        ItemStack stack = slot.getStackInSlot(0);
        if (stack.getItem() instanceof ItemLittleRecipe || stack.getItem() instanceof ItemLittleBlueprint || (getPlayer().capabilities.isCreativeMode && stack.isEmpty())) {
            ItemStack newStack = StructureStringUtils.importStructure(nbt);
            if (stack.getItem() instanceof ItemLittleRecipe) {
                stack.setTagCompound(newStack.getTagCompound());
                newStack = stack;
            } else {
                if (getPlayer().isCreative() && stack.isEmpty())
                    newStack.setCount(1);
                else
                    newStack.setCount(stack.getCount());
            }
            slot.setInventorySlotContents(0, newStack);
        }
    }
    
    @Override
    public void onClosed() {
        super.onClosed();
        WorldUtils.dropItem(getPlayer(), slot.getStackInSlot(0));
    }
    
}
