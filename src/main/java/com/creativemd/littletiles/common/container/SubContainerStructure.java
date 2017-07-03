package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.gui.container.SubContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerStructure extends SubContainer{
	
	public ItemStack stack;
	public int index;
	
	public SubContainerStructure(EntityPlayer player, ItemStack stack) {
		super(player);
		this.stack = stack;
		this.index = player.inventory.currentItem;
	}

	@Override
	public void createControls() {
		
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		stack.setTagCompound(nbt);
		player.inventory.mainInventory.set(index, stack);
	}
	
	

}
