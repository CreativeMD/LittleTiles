package com.creativemd.littletiles.common.utils.animation.transformation;

import com.creativemd.littletiles.common.utils.animation.Animation;
import com.creativemd.littletiles.common.utils.animation.OffsetAnimation;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.util.EnumFacing;

public class OffsetTransformation extends Transformation {
	
	public double x;
	public double y;
	public double z;
	
	public OffsetTransformation(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public OffsetTransformation(EnumFacing facing, LittleGridContext context, int distance) {
		switch (facing.getAxis()) {
		case X:
			x = facing.getAxisDirection().getOffset() * context.toVanillaGrid(distance);
			break;
		case Y:
			y = facing.getAxisDirection().getOffset() * context.toVanillaGrid(distance);
			break;
		case Z:
			z = facing.getAxisDirection().getOffset() * context.toVanillaGrid(distance);
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean isAligned() {
		return true;
	}
	
	@Override
	public Transformation copy() {
		return new OffsetTransformation(x, y, z);
	}
	
	@Override
	public Animation createAnimationTo(Transformation transformation, long duration) {
		if (transformation instanceof OffsetTransformation)
			return new OffsetAnimation(duration, (OffsetTransformation) this.copy(), ((OffsetTransformation) transformation).x, ((OffsetTransformation) transformation).y, ((OffsetTransformation) transformation).z);
		return null;
	}
	
	@Override
	public Animation createAnimationToZero(long duration) {
		return new OffsetAnimation(duration, (OffsetTransformation) this.copy(), 0, 0, 0);
	}
	
	@Override
	public Animation createAnimationFromZero(long duration) {
		OffsetTransformation newTransformation = (OffsetTransformation) this.copy();
		newTransformation.x = 0;
		newTransformation.y = 0;
		newTransformation.z = 0;
		return new OffsetAnimation(duration, newTransformation, x, y, z);
	}
	
}
