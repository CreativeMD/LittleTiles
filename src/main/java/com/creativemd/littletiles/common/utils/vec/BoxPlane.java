package com.creativemd.littletiles.common.utils.vec;

import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.math.BoxUtils.BoxFace;
import com.creativemd.creativecore.common.utils.math.RotationUtils;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;

public class BoxPlane {
	
	public final BoxFace face;
	public final Vector3d normal;
	public final Vector3d origin;
	
	public BoxPlane(Vector3d[] corners, BoxFace face)
	{
		this.face = face;
		this.origin = face.first(corners);
		this.normal = face.normal(corners);
	}
	
	public double getIntersectingScale(Vector3d rayOrigin, Vector3d ray)
	{
		Double result = linePlaneIntersection(ray, rayOrigin, normal, origin);
		if(result == null || result < 0)
			return Double.MAX_VALUE;
		return result;
	}
	
	public static Double linePlaneIntersection(Vector3d ray, Vector3d rayOrigin, Vector3d normal, Vector3d origin) {
		// get d value
		Double d = normal.dot(origin);
		
		if (normal.dot(ray) == 0) {
			return null; // No intersection, the line is parallel to the plane
		}
		
		// Compute the X value for the directed line ray intersecting the plane
		return (d - normal.dot(rayOrigin)) / normal.dot(ray);
	}
	
	public static BoxPlane createPlane(Axis axis, Vector3d direction, Vector3d[] corners)
	{
		double value = RotationUtils.get(axis, direction);
		if(value == 0)
			return null;
		return new BoxPlane(corners, BoxFace.getFace(axis, value > 0));
	}
	
	public static BoxPlane createOppositePlane(Axis axis, Vector3d direction, Vector3d[] corners)
	{
		double value = RotationUtils.get(axis, direction);
		if(value == 0)
			return null;
		return new BoxPlane(corners, BoxFace.getFace(axis, value < 0));
	}
	
}
