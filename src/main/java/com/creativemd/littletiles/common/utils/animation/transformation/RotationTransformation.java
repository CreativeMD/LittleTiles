package com.creativemd.littletiles.common.utils.animation.transformation;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.utils.animation.Animation;
import com.creativemd.littletiles.common.utils.animation.RotateAnimation;

public class RotationTransformation extends Transformation {
	
	public double x;
	public double y;
	public double z;
	
	public RotationTransformation(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public RotationTransformation(Rotation rotation, double value) {
		switch (rotation.axis) {
		case X:
			this.x = rotation.direction * value;
			break;
		case Y:
			this.y = rotation.direction * value;
			break;
		case Z:
			this.z = rotation.direction * value;
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean isAligned() {
		return x % 360 == 0 && y % 360 == 0 && z % 360 == 0;
	}
	
	@Override
	public Transformation copy() {
		return new RotationTransformation(x, y, z);
	}
	
	@Override
	public Animation createAnimationTo(Transformation transformation, long duration) {
		if (transformation instanceof RotationTransformation)
			return new RotateAnimation(duration, (RotationTransformation) this.copy(), ((RotationTransformation) transformation).x, ((RotationTransformation) transformation).y, ((RotationTransformation) transformation).z);
		return null;
	}
	
	@Override
	public Animation createAnimationToZero(long duration) {
		return new RotateAnimation(duration, (RotationTransformation) this.copy(), 0, 0, 0);
	}
	
	@Override
	public Animation createAnimationFromZero(long duration) {
		RotationTransformation newTransformation = (RotationTransformation) this.copy();
		newTransformation.x = 0;
		newTransformation.y = 0;
		newTransformation.z = 0;
		return new RotateAnimation(duration, newTransformation, x, y, z);
	}
}
