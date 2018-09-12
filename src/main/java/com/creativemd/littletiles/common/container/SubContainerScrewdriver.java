package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.gui.container.SubContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerScrewdriver extends SubContainer {
	
	public SubContainerScrewdriver(EntityPlayer player) {
		super(player);
	}
	
	@Override
	public void createControls() {
		
	}
	
	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		
	}
	
}
