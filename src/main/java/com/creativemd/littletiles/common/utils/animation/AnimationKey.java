package com.creativemd.littletiles.common.utils.animation;

import java.util.Collection;
import java.util.HashMap;

import net.minecraft.util.EnumFacing.Axis;

public abstract class AnimationKey {
	
	private static final HashMap<String, AnimationKey> savedKeys = new HashMap<>();
	
	public static final AnimationKey rotX = new RotationKey("rotX", Axis.X);
	public static final AnimationKey rotY = new RotationKey("rotY", Axis.Y);
	public static final AnimationKey rotZ = new RotationKey("rotZ", Axis.Z);
	
	public static final AnimationKey offX = new OffsetKey("offX", Axis.X);
	public static final AnimationKey offY = new OffsetKey("offY", Axis.Y);
	public static final AnimationKey offZ = new OffsetKey("offZ", Axis.Z);
	
	public static Collection<AnimationKey> getKeys() {
		return savedKeys.values();
	}
	
	public static AnimationKey getKey(String name) {
		return savedKeys.get(name);
	}
	
	public static AnimationKey getRotation(Axis axis) {
		switch (axis) {
		case X:
			return rotX;
		case Y:
			return rotY;
		case Z:
			return rotZ;
		}
		return null;
	}
	
	public static AnimationKey getOffset(Axis axis) {
		switch (axis) {
		case X:
			return offX;
		case Y:
			return offY;
		case Z:
			return offZ;
		}
		return null;
	}
	
	public static class RotationKey extends AnimationKey {
		
		public final Axis axis;
		
		RotationKey(String name, Axis axis) {
			super(name);
			this.axis = axis;
		}
		
		@Override
		public boolean isAligned(double value) {
			return value % 360 == 0;
		}
		
	}
	
	public static class OffsetKey extends AnimationKey {
		
		public final Axis axis;
		
		OffsetKey(String name, Axis axis) {
			super(name);
			this.axis = axis;
		}
		
		@Override
		public boolean isAligned(double value) {
			return true;
		}
		
	}
	
	public final String name;
	
	AnimationKey(String name) {
		this.name = name;
		if (savedKeys.containsKey(name))
			throw new RuntimeException("Duplicate AnimationKey name=" + name);
		savedKeys.put(name, this);
	}
	
	public double getDefault() {
		return 0;
	}
	
	public abstract boolean isAligned(double value);
	
}
