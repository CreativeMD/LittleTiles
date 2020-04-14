package com.creativemd.littletiles.common.event;

import com.creativemd.littletiles.common.action.LittleAction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ActionEvent extends Event {
	
	public final LittleAction action;
	
	public final ActionType type;
	
	public final EntityPlayer player;
	
	public ActionEvent(LittleAction action, ActionType type, EntityPlayer player) {
		this.action = action;
		this.type = type;
		this.player = player;
	}
	
	public static enum ActionType {
		normal, undo, redo;
	}
}
