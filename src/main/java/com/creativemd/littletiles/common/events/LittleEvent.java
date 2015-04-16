package com.creativemd.littletiles.common.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;

public class LittleEvent {
	
	@SubscribeEvent
	public void openContainer(PlayerOpenContainerEvent event){
		if(event.entityPlayer.openContainer instanceof ContainerWorkbench)
			event.setResult(Result.ALLOW);
	}
}
