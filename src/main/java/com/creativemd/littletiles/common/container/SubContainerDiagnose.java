package com.creativemd.littletiles.common.container;

import java.util.UUID;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.events.LittleDoorHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerDiagnose extends SubContainer {
	
	public SubContainerDiagnose(EntityPlayer player) {
		super(player);
	}
	
	@Override
	public void createControls() {
		
	}
	
	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		EntityAnimation animation = LittleDoorHandler.server.findDoor(UUID.fromString(nbt.getString("uuid")));
		if (animation != null)
			animation.destroyAnimation();
	}
	
}
