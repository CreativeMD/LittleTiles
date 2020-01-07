package com.creativemd.littletiles.client.event;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class LeftClick extends Event {
	
	public final World world;
	public final EntityPlayer player;
	
	@Nullable
	public final RayTraceResult result;
	
	public LeftClick(World world, EntityPlayer player, RayTraceResult result) {
		this.world = world;
		this.player = player;
		this.result = result;
	}
	
}
