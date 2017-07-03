package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.slots.SlotStackLimit;
import com.creativemd.littletiles.common.structure.LittleStorage;
import com.creativemd.littletiles.common.utils.LittleTile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerStorage extends SubContainer {
	
	public LittleStorage storage;

	public SubContainerStorage(EntityPlayer player, LittleStorage storage) {
		super(player);
		this.storage = storage;
	}

	@Override
	public void createControls() {
		if(storage.inventory != null)
		{
			int slotsPerRow = 12;
			for (int i = 0; i < storage.inventory.getSizeInventory(); i++) {
				int row = i/slotsPerRow;
				int rowIndex = i-row*slotsPerRow;
				int stackSize = storage.stackSizeLimit;
				if(i+1 == storage.numberOfSlots && storage.lastSlotStackSize > 0)
					stackSize = storage.lastSlotStackSize;
				addSlotToContainer(new SlotStackLimit(storage.inventory, i, 5+rowIndex*18, 5+row*18, stackSize));
			}
			
			addPlayerSlotsToContainer(player, 45, 170);
		}
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		
	}

}
