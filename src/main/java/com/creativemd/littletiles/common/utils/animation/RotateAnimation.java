package com.creativemd.littletiles.common.utils.animation;

import com.creativemd.littletiles.common.utils.animation.transformation.RotationTransformation;

public class RotateAnimation extends Animation {
	
	public final double startX;
	public final double startY;
	public final double startZ;
	
	public final double endX;
	public final double endY;
	public final double endZ;
	
	public RotateAnimation(long duration, double startX, double startY, double startZ, double endX, double endY, double endZ) {
		super(duration);
		this.startX = startX;
		this.startY = startY;
		this.startZ = startZ;
		
		this.endX = endX;
		this.endY = endY;
		this.endZ = endZ;
	}
	
	public RotateAnimation(long duration, RotationTransformation start, double endX, double endY, double endZ) {
		this(duration, start.x, start.y, start.z, endX, endY, endZ);
	}
	
	public RotateAnimation(int[] array) {
		super((((long) array[0]) << 32) | (array[1] & 0xffffffffL));
		this.startX = Double.longBitsToDouble((((long) array[2]) << 32) | (array[3] & 0xffffffffL));
		this.startY = Double.longBitsToDouble((((long) array[4]) << 32) | (array[5] & 0xffffffffL));
		this.startZ = Double.longBitsToDouble((((long) array[6]) << 32) | (array[7] & 0xffffffffL));
		this.endX = Double.longBitsToDouble((((long) array[8]) << 32) | (array[9] & 0xffffffffL));
		this.endY = Double.longBitsToDouble((((long) array[10]) << 32) | (array[11] & 0xffffffffL));
		this.endZ = Double.longBitsToDouble((((long) array[12]) << 32) | (array[13] & 0xffffffffL));
	}
	
	@Override
	public void tick(AnimationState state) {
		if (state.rotation == null)
			return;
		
		state.rotation.x = startX + ((endX - startX) / duration) * tick;
		state.rotation.y = startY + ((endY - startY) / duration) * tick;
		state.rotation.z = startZ + ((endZ - startZ) / duration) * tick;
	}
	
	@Override
	public void end(AnimationState state) {
		if (state.rotation == null)
			return;
		
		state.rotation.x = endX;
		state.rotation.y = endY;
		state.rotation.z = endZ;
	}
	
	public int[] getArray() {
		long startX = Double.doubleToLongBits(this.startX);
		long startY = Double.doubleToLongBits(this.startY);
		long startZ = Double.doubleToLongBits(this.startZ);
		long endX = Double.doubleToLongBits(this.endX);
		long endY = Double.doubleToLongBits(this.endY);
		long endZ = Double.doubleToLongBits(this.endZ);
		return new int[] { (int) (duration >> 32), (int) duration, (int) (startX >> 32), (int) startX, (int) (startY >> 32), (int) startY, (int) (startZ >> 32), (int) startZ, (int) (endX >> 32), (int) endX, (int) (endY >> 32), (int) endY, (int) (endZ >> 32), (int) endZ };
	}
	
}
