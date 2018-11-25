package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerStructureOverview extends SubContainer {
	
	public LittleTile tile;
	
	public SubContainerStructureOverview(EntityPlayer player, LittleTile tile) {
		super(player);
		this.tile = tile;
	}
	
	@Override
	public void createControls() {
		
	}
	
	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		
	}
	
}
