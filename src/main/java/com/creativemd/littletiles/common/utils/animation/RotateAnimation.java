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
	
}
