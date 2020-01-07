package com.creativemd.littletiles.client.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class WheelClick extends Event {
	
	public final World world;
	public final EntityPlayer player;
	
	public WheelClick(World world, EntityPlayer player) {
		this.world = world;
		this.player = player;
	}
	
}
