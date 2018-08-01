package com.creativemd.littletiles.common.structure.attributes;

public enum LittleStructureAttribute {
	
	NONE,
	LADDER,
	COLLISION,
	NO_DROP;
	
	public static LittleStructureAttribute get(int index)
	{
		return values()[index];
	}
}
