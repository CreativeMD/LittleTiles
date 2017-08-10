package com.creativemd.littletiles.common.action;

import net.minecraft.util.text.translation.I18n;

public class LittleActionException extends Exception {
	
	public LittleActionException(String msg) {
		super(msg);
	}
	
	@Override
	public String getLocalizedMessage()
	{
		return I18n.translateToLocal(getMessage());
	}
	
}
