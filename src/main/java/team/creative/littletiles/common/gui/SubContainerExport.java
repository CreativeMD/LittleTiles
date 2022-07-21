package team.creative.littletiles.common.gui;

import com.creativemd.creativecore.common.gui.container.SubContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.inventory.Slot;
import team.creative.creativecore.common.util.mc.WorldUtils;

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
