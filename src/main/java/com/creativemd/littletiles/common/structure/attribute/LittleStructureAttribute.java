package com.creativemd.littletiles.common.structure.attribute;

public class LittleStructureAttribute {
	
	public static final int NONE = 0;
	
	// Does not need connection (structures will not be considered in block list)
	
	public static final int LADDER = 0b00000000_00000000_00000000_00000001;
	public static final int NOCOLLISION = 0b00000000_00000000_00000000_00000010;
	public static final int PREMADE = 0b00000000_00000000_00000000_00000100;
	
	// Does need connection (structures will be considered in block list)
	
	public static final int EXTRA_COLLSION = 0b00000000_00000000_00000001_00000000;
	public static final int EXTRA_RENDERING = 0b00000000_00000000_00000010_00000000;
	public static final int TICKING = 0b00000000_00000000_00000100_00000000;
	public static final int TICK_RENDERING = 0b00000000_00000000_00001000_00000000;
	
	public static boolean isLadder(int attribute) {
		return checkBit(attribute, 0);
	}
	
	public static boolean hasNoCollision(int attribute) {
		return checkBit(attribute, 1);
	}
	
	public static boolean isPremade(int attribute) {
		return checkBit(attribute, 2);
	}
	
	private static boolean checkBit(int attribute, int position) {
		return ((attribute >>> position) & 1) != 0;
	}
	
	public static int loadOld(int ordinal) {
		switch (ordinal) {
		case 1:
			return NOCOLLISION;
		case 2:
			return PREMADE;
		default:
			return NONE;
		}
	}
	
}
