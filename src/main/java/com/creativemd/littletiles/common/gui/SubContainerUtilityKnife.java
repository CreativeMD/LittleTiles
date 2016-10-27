package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.gui.container.SubContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerUtilityKnife extends SubContainer {
	
	public ItemStack stack;
	
	public SubContainerUtilityKnife(EntityPlayer player, ItemStack stack) {
		super(player);
		this.stack = stack;
	}

	@Override
	public void createControls() {
		
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("mode", nbt.getInteger("mode"));
		stack.getTagCompound().setInteger("thick", nbt.getInteger("thick"));
		//player.inventory.mainInventory[player.inventory.currentItem] = stack;
	}

}
