package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.littletiles.common.tile.parent.StructureTileList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerStructureOverview extends SubContainer {
	
	public StructureTileList list;
	
	public SubContainerStructureOverview(EntityPlayer player, StructureTileList list) {
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
