package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.common.container.SubContainer;
import com.creativemd.littletiles.common.items.ItemColorTube;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerColorTube extends SubContainer{
	
	public ItemStack stack;
	
	public SubContainerColorTube(EntityPlayer player, ItemStack stack) {
		super(player);
		this.stack = stack;
	}

	@Override
	public void createControls() {
		
	}

	@Override
	public void onGuiPacket(int controlID, NBTTagCompound nbt, EntityPlayer player) {
		if(controlID == 0)
		{
			ItemColorTube.setColor(stack, nbt.getInteger("color"));
			player.inventory.mainInventory[player.inventory.currentItem] = stack;
		}
	}

}
