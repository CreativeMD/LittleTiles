package com.creativemd.littletiles.client.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public class HoldLeftClick extends Event {
	
	public final World world;
	public final EntityPlayer player;
	
	public final boolean leftClick;
	
	private boolean leftClickResult;
	
	public HoldLeftClick(World world, EntityPlayer player, boolean leftClick) {
		this.world = world;
		this.player = player;
		this.leftClick = leftClick;
		this.leftClickResult = leftClick;
	}
	
	public void setLeftClickResult(boolean leftClick) {
		this.leftClickResult = leftClick;
	}
	
	public boolean getLeftClickResult() {
		return leftClickResult;
	}
	
}
