package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.slots.SlotStackLimit;
import com.creativemd.littletiles.common.structure.LittleStorage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerStorage extends SubContainer {
	
	public static enum StorageSize {
		
		SMALL(176, 166, 8, 84, false),
		Large(250, 250, 45, 170, false),
		INFINITE(250, 250, 45, 170, true);
		
		public final int height;
		public final int width;
		public final int playerOffsetX;
		public final int playerOffsetY;
		public final boolean scrollbox;
		
		StorageSize(int width, int height, int playerOffsetX, int playerOffsetY, boolean scrollbox)
		{
			this.scrollbox = scrollbox;
			this.height = height;
			this.width = width;
			this.playerOffsetX = playerOffsetX;
			this.playerOffsetY = playerOffsetY;
		}
		
		public static StorageSize getSizeFromInventory(IInventory inventory)
		{
			if(inventory.getSizeInventory() <= 27)
				return StorageSize.SMALL;
			else if(inventory.getSizeInventory() <= 117)
				return Large;
			return StorageSize.INFINITE;
		}
		
	}

	public LittleStorage storage;
	public final StorageSize size;

	public SubContainerStorage(EntityPlayer player, LittleStorage storage) {
		super(player);
		this.storage = storage;
		this.size = StorageSize.getSizeFromInventory(storage.inventory);
	}

	@Override
	public void createControls() {
		if(storage.inventory != null)
		{
			int slotsPerRow = size.width/18;
			int rows = (int) Math.ceil(storage.inventory.getSizeInventory()/(double)slotsPerRow);
			int rowWidth = Math.min(slotsPerRow, storage.inventory.getSizeInventory()) * 18;
			int offsetX = (size.width - rowWidth) / 2;
			
			for (int i = 0; i < storage.inventory.getSizeInventory(); i++) {
				int row = i/slotsPerRow;
				int rowIndex = i-row*slotsPerRow;
				int stackSize = storage.stackSizeLimit;
				if(i+1 == storage.numberOfSlots && storage.lastSlotStackSize > 0)
					stackSize = storage.lastSlotStackSize;
				addSlotToContainer(new SlotStackLimit(storage.inventory, i, offsetX+rowIndex*18, 5+row*18, stackSize));
			}
			
			addPlayerSlotsToContainer(player, size.playerOffsetX, size.playerOffsetY);
		}
	}
	
	@Override
	public void writeOpeningNBT(NBTTagCompound nbt)
	{
		nbt.setTag("inventory", InventoryUtils.saveInventoryBasic(storage.inventory));
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		if(nbt.hasKey("inventory"))
		{
			ItemStack[] stacks = InventoryUtils.loadInventory(nbt.getCompoundTag("inventory"));
			for (int i = 0; i < stacks.length; i++) {
				storage.inventory.setInventorySlotContents(i, stacks[i]);
			}
		}
	}

}
