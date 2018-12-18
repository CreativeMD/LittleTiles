package com.creativemd.littletiles.common.utils.animation.transformation;

import com.creativemd.littletiles.common.utils.animation.Animation;

public abstract class Transformation {
	
	public abstract Animation createAnimationTo(Transformation transformation, long duration);
	
	public abstract Animation createAnimationToZero(long duration);
	
	public abstract Animation createAnimationFromZero(long duration);
	
	public abstract boolean isAligned();
	
	public abstract Transformation copy();
}
