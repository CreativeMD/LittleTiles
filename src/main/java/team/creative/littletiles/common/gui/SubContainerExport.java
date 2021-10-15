package team.creative.littletiles.common.gui;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerExport extends SubContainer {
    
    public final InventoryBasic slot = new InventoryBasic("slot", false, 1);
    
    public SubContainerExport(EntityPlayer player) {
        super(player);
    }
    
    @Override
    public void createControls() {
        addSlotToContainer(new Slot(slot, 0, 10, 10));
        addPlayerSlotsToContainer(player);
    }
    
    @Override
    public void onPacketReceive(NBTTagCompound nbt) {
        
    }
    
    @Override
    public void onClosed() {
        super.onClosed();
        WorldUtils.dropItem(getPlayer(), slot.getStackInSlot(0));
    }
}
