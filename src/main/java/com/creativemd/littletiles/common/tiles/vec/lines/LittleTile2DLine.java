package com.creativemd.littletiles.common.tiles.vec.lines;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.advanced.AxisAlignedBBOrdinarySliced;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class LittleTile2DLine {
	
	public double originOne;
	public double originTwo;
	
	public double directionOne;
	public double directionTwo;
	
	public Axis one;
	public Axis two;
	
	public LittleTile2DLine(Axis one, Axis two, Vector3d origin, double directionOne, double directionTwo) {
		this.one = one;
		this.two = two;
		this.originOne = RotationUtils.get(one, origin);
		this.originTwo = RotationUtils.get(two, origin);
		this.directionOne = directionOne;
		this.directionTwo = directionTwo;
	}
	
	public LittleTile2DLine(Axis one, Axis two, LittleTileVec origin, double directionOne, double directionTwo) {
		this.one = one;
		this.two = two;
		this.originOne = origin.getAxis(one);
		this.originTwo = origin.getAxis(two);
		this.directionOne = directionOne;
		this.directionTwo = directionTwo;
	}
	
	public double getOrigin(Axis axis)
	{
		if(one == axis)
			return originOne;
		return originTwo;
	}
	
	public double getDirection(Axis axis)
	{
		if(one == axis)
			return directionOne;
		return directionTwo;
	}
	
	public Axis getOther(Axis axis)
	{
		if(one == axis)
			return two;
		return one;
	}
	
	public double get(Axis axis, double value)
	{
		Axis other = getOther(axis);
		return getOrigin(other) + getDirection(other) * (value - getOrigin(axis))/getDirection(axis);
	}
	
	public boolean isCoordinateOnLine(int one, int two)
	{
		return get(this.one, one) == two;
	}
	
	public boolean isCoordinateToTheRight(int one, int two)
	{
		double tempOne = one - originOne;
		double tempTwo = two - originTwo;
		
		//return directionOne * (-tempTwo) + directionTwo * tempOne > 0;
		
		return directionOne * tempTwo - directionTwo * tempOne < 0;
		
				//d=(x−x1)(y2−y1)−(y−y1)(x2−x1)
				//If d<0d<0 then the point lies on one side of the line, and if d>0d>0 then it lies on the other side. If d=0d=0 then the point lies exactly line.
	}
	
	public Vec3d intersect(LittleTile2DLine line, int thirdValue)
	{
		if(directionOne * line.directionTwo - directionTwo * line.directionOne == 0)
			return null;
		
		Vec3d vec = new Vec3d(thirdValue, thirdValue, thirdValue);
		double t = (((double) line.originTwo - originTwo) * line.directionOne + originOne * line.directionTwo - line.originOne * line.directionTwo) / (line.directionOne * directionTwo - directionOne * line.directionTwo);
		vec = RotationUtils.setValue(vec, originOne + t * directionOne, one);
		vec = RotationUtils.setValue(vec, originTwo + t * directionTwo, two);
		return vec;
	}
}
