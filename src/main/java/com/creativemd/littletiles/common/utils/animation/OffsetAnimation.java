package com.creativemd.littletiles.common.utils.animation;

import com.creativemd.littletiles.common.utils.animation.transformation.OffsetTransformation;

public class OffsetAnimation extends Animation {
	
	public final double startX;
	public final double startY;
	public final double startZ;
	
	public final double endX;
	public final double endY;
	public final double endZ;
	
	public OffsetAnimation(long duration, double startX, double startY, double startZ, double endX, double endY, double endZ) {
		super(duration);
		this.startX = startX;
		this.startY = startY;
		this.startZ = startZ;
		
		this.endX = endX;
		this.endY = endY;
		this.endZ = endZ;
	}
	
	public OffsetAnimation(long duration, OffsetTransformation start, double endX, double endY, double endZ) {
		this(duration, start.x, start.y, start.z, endX, endY, endZ);
	}
	
	@Override
	public void tick(AnimationState state) {
		if (state.offset == null)
			return;
		
		state.offset.x = startX + ((endX - startX) / duration) * tick;
		state.offset.y = startY + ((endY - startY) / duration) * tick;
		state.offset.z = startZ + ((endZ - startZ) / duration) * tick;
	}
	
	@Override
	public void end(AnimationState state) {
		if (state.offset == null)
			return;
		
		state.offset.x = endX;
		state.offset.y = endY;
		state.offset.z = endZ;
	}
	
}
