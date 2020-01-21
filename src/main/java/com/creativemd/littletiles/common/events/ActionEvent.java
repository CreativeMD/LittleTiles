package com.creativemd.littletiles.common.events;

import com.creativemd.littletiles.common.action.LittleAction;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ActionEvent extends Event {
	
	public final LittleAction action;
	
	public final ActionType type;
	
	public ActionEvent(LittleAction action, ActionType type) {
		this.action = action;
		this.type = type;
	}
	
	public static enum ActionType {
		normal, undo, redo;
	}
}
