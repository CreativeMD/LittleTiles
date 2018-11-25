package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.littletiles.common.tileentity.TileEntityParticle;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerParticle extends SubContainer {
	
	public TileEntityParticle particle;
	
	public SubContainerParticle(EntityPlayer player, TileEntityParticle particle) {
		super(player);
		this.particle = particle;
	}
	
	@Override
	public void createControls() {
		
	}
	
	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		particle.receiveUpdatePacket(nbt);
		particle.updateBlock();
	}
	
}
