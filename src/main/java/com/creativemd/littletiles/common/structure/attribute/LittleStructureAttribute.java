package com.creativemd.littletiles.common.structure.attribute;

public enum LittleStructureAttribute {
	
	NONE, LADDER, NOCOLLISION, PREMADE;
	
	public static LittleStructureAttribute get(int index) {
		return values()[index];
	}
}
