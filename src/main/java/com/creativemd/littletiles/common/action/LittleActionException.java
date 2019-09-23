package com.creativemd.littletiles.common.action;

import com.creativemd.littletiles.common.utils.tooltip.ActionMessage;

import net.minecraft.util.text.translation.I18n;

public class LittleActionException extends Exception {
	
	public LittleActionException(String msg) {
		super(msg);
	}
	
	public ActionMessage getActionMessage() {
		return null;
	}
	
	@Override
	public String getLocalizedMessage() {
		return I18n.translateToLocal(getMessage());
	}
	
	public static class TileNotThereException extends LittleActionException {
		
		public TileNotThereException() {
			super("action.tile.notthere");
		}
		
	}
	
	public static class TileNotFoundException extends LittleActionException {
		
		public TileNotFoundException() {
			super("action.tile.notfound");
		}
		
	}
	
	public static class TileEntityNotFoundException extends LittleActionException {
		
		public TileEntityNotFoundException() {
			super("action.tileentity.notfound");
		}
		
	}
	
	public static class StructureNotLoadedException extends LittleActionException {
		
		public StructureNotLoadedException() {
			super("action.structure.notloaded");
		}
		
	}
	
	public static class EntityNotFoundException extends LittleActionException {
		
		public EntityNotFoundException() {
			super("action.entity.notfound");
		}
		
	}
}
