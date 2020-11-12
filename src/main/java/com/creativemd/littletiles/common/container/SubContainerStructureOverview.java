package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerStructureOverview extends SubContainer {
	
	public IStructureTileList list;
	
	public SubContainerStructureOverview(EntityPlayer player, IStructureTileList list) {
		super(player);
		this.list = list;
	}
	
	@Override
	public void createControls() {
		
	}
	
	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		
	}
	
}
