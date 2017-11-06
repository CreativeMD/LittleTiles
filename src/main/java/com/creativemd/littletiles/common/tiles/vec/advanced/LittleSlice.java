package com.creativemd.littletiles.common.tiles.vec.advanced;

import java.util.Arrays;

import javax.vecmath.Vector3f;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.littletiles.client.tiles.LittleCorner;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;

import net.minecraft.client.renderer.EnumFaceDirection.VertexInformation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public enum LittleSlice {
	
	X_US_DN_RIGHT(Axis.X, true, 2, 0, LittleCorner.EUS, LittleCorner.EDN),
	X_US_DN_LEFT(Axis.X, false, 0, 2, LittleCorner.EUS, LittleCorner.EDN),	
	X_DS_UN_RIGHT(Axis.X, true, 0, 2, LittleCorner.EDS, LittleCorner.EUN),
	X_DS_UN_LEFT(Axis.X, false, 2, 0, LittleCorner.EDS, LittleCorner.EUN),
	
	Y_ES_WN_RIGHT(Axis.Y, true, 1, 0, LittleCorner.EUS, LittleCorner.WUN),
	Y_ES_WN_LEFT(Axis.Y, false, 0, 1, LittleCorner.EUS, LittleCorner.WUN),	
	Y_WS_EN_RIGHT(Axis.Y, true, 0, 1, LittleCorner.WUS, LittleCorner.EUN),
	Y_WS_EN_LEFT(Axis.Y, false, 1, 0, LittleCorner.WUS, LittleCorner.EUN),
	
	Z_WU_ED_RIGHT(Axis.Z, true, 2, 0, LittleCorner.WUS, LittleCorner.EDS),
	Z_WU_ED_LEFT(Axis.Z, false, 0, 2, LittleCorner.WUS, LittleCorner.EDS),	
	Z_WD_EU_RIGHT(Axis.Z, true, 0, 2, LittleCorner.WDS, LittleCorner.EUS),
	Z_WD_EU_LEFT(Axis.Z, false, 2, 0, LittleCorner.WDS, LittleCorner.EUS);
	
	public final Axis axis;
	public final Vec3i sliceVec;
	public final EnumFacing emptySideOne;
	public final EnumFacing emptySideSecond;
	public final boolean isRight;
	
	public final LittleCorner start;
	public final LittleCorner end;
	
	public final int traingleOrderPositive;
	public final int traingleOrderNegative;
	private final int[] normal;
	
	public static LittleSlice getSliceFromNormal(int[] normal)
	{
		for (LittleSlice slice : values()) {
			if(Arrays.equals(slice.normal, normal))
				return slice;
		}
		return null;
	}
	
	public static int getDirectionBetweenFacing(EnumFacing facing, EnumFacing facing2)
	{
		if(facing.getAxisDirection() == facing2.getAxisDirection())
			return 0;
		return facing.getAxisDirection() == AxisDirection.POSITIVE ? -1 : 1;
	}
	
	private LittleSlice(Axis axis, boolean isRight, int traingleOrderPositive, int traingleOrderNegative, LittleCorner start, LittleCorner end)
	{
		this.axis = axis;
		this.isRight = isRight;
		this.start = start;
		this.end = end;
		this.traingleOrderPositive = traingleOrderPositive;
		this.traingleOrderNegative = traingleOrderNegative;
		this.sliceVec = new Vec3i(getDirectionBetweenFacing(start.x, end.x), getDirectionBetweenFacing(start.y, end.y), getDirectionBetweenFacing(start.z, end.z));
		Vec3i temp = RotationUtils.rotateVec(sliceVec, Rotation.getRotation(axis, isRight));
		this.normal = new int[]{temp.getX(), temp.getY(), temp.getZ()};
		switch(axis)
		{
		case X:
			this.emptySideOne = EnumFacing.getFacingFromAxis(normal[Axis.Y.ordinal()] == 1 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, Axis.Y);
			this.emptySideSecond = EnumFacing.getFacingFromAxis(normal[Axis.Z.ordinal()] == 1 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, Axis.Z);
			break;
		case Y:
			this.emptySideOne = EnumFacing.getFacingFromAxis(normal[Axis.X.ordinal()] == 1 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, Axis.X);
			this.emptySideSecond = EnumFacing.getFacingFromAxis(normal[Axis.Z.ordinal()] == 1 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, Axis.Z);
			break;
		case Z:
			this.emptySideOne = EnumFacing.getFacingFromAxis(normal[Axis.Y.ordinal()] == 1 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, Axis.Y);
			this.emptySideSecond = EnumFacing.getFacingFromAxis(normal[Axis.X.ordinal()] == 1 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, Axis.X);
			break;
		default:
			emptySideOne = EnumFacing.UP;
			emptySideSecond = EnumFacing.EAST;
			break;
		}
	}
	
	public LittleSlice getOpposite()
	{
		return getSliceFromNormal(new int[]{-normal[0], -normal[1], -normal[2]});
	}
	
	public int getDirectionScale(Axis axis)
	{
		return RotationUtils.get(axis, sliceVec);
	}
	
	public boolean isFacingPositive(Axis axis)
	{
		return normal[axis.ordinal()] > 0;
	}
	
	public EnumFacing getEmptySide(Axis axis)
	{
		return EnumFacing.getFacingFromAxis(normal[axis.ordinal()] > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, axis);
	}
	
	public int[] getNormal()
	{
		return normal;
	}
	
	public Vec3d getNormalVec()
	{
		return new Vec3d(normal[0], normal[1], normal[2]).normalize();
	}
	
	public LittleSlice rotate(Rotation rotation)
	{
		return getSliceFromNormal(new int[]{rotation.getMatrix().getX(normal), rotation.getMatrix().getY(normal), rotation.getMatrix().getZ(normal)});
	}
	
	public LittleSlice flip(Axis axis)
	{
		int[] newNormal = Arrays.copyOf(normal, normal.length);
		newNormal[axis.ordinal()] = -newNormal[axis.ordinal()];
		return getSliceFromNormal(newNormal);
	}
	
	public EnumFacing getPreferedSide(Vec3d size)
	{
		double sizeOne = RotationUtils.get(emptySideOne.getAxis(), size);
		double sizeTwo = RotationUtils.get(emptySideSecond.getAxis(), size);
		if(sizeOne > sizeTwo)
			return emptySideSecond;
		else if(sizeOne < sizeTwo)
			return emptySideOne;
		
		return emptySideOne.getAxis() == Axis.Y ? emptySideOne : emptySideSecond;
	}
	
	public EnumFacing getPreferedSide(LittleTileSize size)
	{		
		int sizeOne = size.get(emptySideOne.getAxis());
		int sizeTwo = size.get(emptySideSecond.getAxis());
		if(sizeOne > sizeTwo)
			return emptySideSecond;
		else if(sizeOne < sizeTwo)
			return emptySideOne;
		
		return emptySideOne.getAxis() == Axis.Y ? emptySideOne : emptySideSecond;
	}
	
	public boolean shouldRenderSide(EnumFacing facing, LittleTileSize size)
	{
		if(normal[facing.getAxis().ordinal()] == facing.getAxisDirection().getOffset())
		{
			Axis otherAxis = RotationUtils.getDifferentAxisFirst(axis);
			if(otherAxis == facing.getAxis())
				otherAxis = RotationUtils.getDifferentAxisSecond(axis);
			
			int sizeOne = size.get(facing.getAxis());
			int sizeTwo = size.get(otherAxis);
			if(sizeOne > sizeTwo)
				return false;
			else if(sizeOne < sizeTwo)
				return true;
			else
				return axis == Axis.Y ? RotationUtils.getDifferentAxisFirst(axis) == facing.getAxis() : otherAxis != Axis.Y;
		}
		return true;
	}
	
	/**
	 * Theoretically there are two corners, but it will always return the one with a postive direction
	 */
	public LittleCorner getFilledCorner()
	{
		return LittleCorner.getCornerUnsorted(RotationUtils.getFacing(axis), emptySideOne.getOpposite(), emptySideSecond.getOpposite());
	}
	
	public boolean isCornerAffected(LittleCorner corner)
	{
		return (corner.x == emptySideOne || corner.y == emptySideOne || corner.z == emptySideOne) &&
				(corner.x == emptySideSecond || corner.y == emptySideSecond || corner.z == emptySideSecond);
	}
	
	public void sliceVector(LittleCorner corner, Vector3f vec, CubeObject cube, LittleTileSize size)
	{
		if(isCornerAffected(corner))
		{
			EnumFacing side = getPreferedSide(size);
			switch(side.getAxis())
			{
			case X:
				vec.x = cube.getVertexInformationPosition(side.getOpposite().ordinal());
				break;
			case Y:
				vec.y = cube.getVertexInformationPosition(side.getOpposite().ordinal());
				break;
			case Z:
				vec.z = cube.getVertexInformationPosition(side.getOpposite().ordinal());
				break;
			}
		}
	}
}
