package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.gui.container.SubContainer;
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
	public void onPacketReceive(NBTTagCompound nbt) {
		stack.setTagCompound(nbt);
	}

}
