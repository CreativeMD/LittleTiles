package com.creativemd.littletiles.client.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class PickBlockEvent extends Event {
	
	public final World world;
	public final EntityPlayer player;
	public final RayTraceResult result;
	
	public PickBlockEvent(World world, EntityPlayer player, RayTraceResult result) {
		this.world = world;
		this.player = player;
		this.result = result;
	}
	
}
