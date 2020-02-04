package com.creativemd.littletiles.common.structure.attribute;

public class LittleStructureAttribute {
	
	public static final int NONE = 0;
	
	// passive types
	
	public static final int LADDER = 0b00000000_00000000_00000000_00000001;
	public static final int NOCOLLISION = 0b00000000_00000000_00000000_00000010;
	public static final int PREMADE = 0b00000000_00000000_00000000_00000100;
	
	// active types
	
	public static final int EXTRA_COLLSION = 0b00000000_00000000_00000001_00000000;
	public static final int EXTRA_RENDERING = 0b00000000_00000000_00000010_00000000;
	public static final int TICKING = 0b00000000_00000000_00000100_00000000;
	public static final int TICK_RENDERING = 0b00000000_00000000_00001000_00000000;
	public static final int NEIGHBOR_LISTENER = 0b00000000_00000000_00010000_00000000;
	
	public static boolean ladder(int attribute) {
		return checkBit(attribute, 0);
	}
	
	public static boolean noCollision(int attribute) {
		return checkBit(attribute, 1);
	}
	
	public static boolean premade(int attribute) {
		return checkBit(attribute, 2);
	}
	
	public static boolean extraCollision(int attribute) {
		return checkBit(attribute, 8);
	}
	
	public static boolean extraRendering(int attribute) {
		return checkBit(attribute, 9);
	}
	
	public static boolean ticking(int attribute) {
		return checkBit(attribute, 10);
	}
	
	public static boolean tickRendering(int attribute) {
		return checkBit(attribute, 11);
	}
	
	public static boolean neighborListener(int attribute) {
		return checkBit(attribute, 12);
	}
	
	public static boolean active(int attribute) {
		return (attribute >>> 8) > 0;
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
