package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.gui.container.SubContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerHammer extends SubContainer {
	
	public ItemStack stack;
	
	public SubContainerHammer(EntityPlayer player, ItemStack stack) {
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
