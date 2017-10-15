package com.creativemd.littletiles.client.tiles;

import javax.vecmath.Vector3f;

import com.creativemd.creativecore.common.utils.Rotation;

import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.EnumFaceDirection.VertexInformation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;

public enum LittleCorner {
	
	EUN(EnumFacing.EAST, EnumFacing.UP, EnumFacing.NORTH),
	EUS(EnumFacing.EAST, EnumFacing.UP, EnumFacing.SOUTH),
	EDN(EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.NORTH),
	EDS(EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.SOUTH),
	WUN(EnumFacing.WEST, EnumFacing.UP, EnumFacing.NORTH),
	WUS(EnumFacing.WEST, EnumFacing.UP, EnumFacing.SOUTH),
	WDN(EnumFacing.WEST, EnumFacing.DOWN, EnumFacing.NORTH),
	WDS(EnumFacing.WEST, EnumFacing.DOWN, EnumFacing.SOUTH);
	
	public final VertexInformation info;
	
	public final EnumFacing x;
	public final EnumFacing y;
	public final EnumFacing z;
	
	private LittleCorner(EnumFacing x, EnumFacing y, EnumFacing z) {
		this.info = getInfoFromFacings(x, y, z);
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public boolean isFacingPositive(Axis axis)
	{
		return getFacing(axis).getAxisDirection() == AxisDirection.POSITIVE;
	}
	
	public EnumFacing getFacing(Axis axis)
	{
		switch(axis)
		{
		case X:
			return x;
		case Y:
			return y;
		case Z:
			return z;
		}
		return null;
	}
	
	public LittleCorner flip(Axis axis)
	{
		switch(axis)
		{
		case X:
			return getCorner(x.getOpposite(), y, z);
		case Y:
			return getCorner(x, y.getOpposite(), z);
		case Z:
			return getCorner(x, y, z.getOpposite());
		}
		return null;
	}
	
	public LittleCorner rotate(Rotation rotation)
	{
		int normalX = x.getAxisDirection().getOffset();
		int normalY = y.getAxisDirection().getOffset();
		int normalZ = z.getAxisDirection().getOffset();
		return getCorner(EnumFacing.getFacingFromAxis(rotation.getMatrix().getX(normalX, normalY, normalZ) > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, Axis.X),
				EnumFacing.getFacingFromAxis(rotation.getMatrix().getY(normalX, normalY, normalZ) > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, Axis.Y),
				EnumFacing.getFacingFromAxis(rotation.getMatrix().getZ(normalX, normalY, normalZ) > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, Axis.Z));
	}
	
	public static LittleCorner getCornerUnsorted(EnumFacing facing)
	{
		switch(facing.getAxis())
		{
		case X:
			return getCorner(facing, EnumFacing.UP, EnumFacing.SOUTH);
		case Y:
			return getCorner(EnumFacing.EAST, facing, EnumFacing.SOUTH);
		case Z:
			return getCorner(EnumFacing.EAST, EnumFacing.UP, facing);
		}
		return null;
	}
	
	public static LittleCorner getCornerUnsorted(EnumFacing facing, EnumFacing facing2, EnumFacing facing3)
	{
		return getCorner(facing.getAxis() != Axis.X ? facing2.getAxis() != Axis.X ? facing3 : facing2 : facing,
				facing.getAxis() != Axis.Y ? facing2.getAxis() != Axis.Y ? facing3 : facing2 : facing, 
				facing.getAxis() != Axis.Z ? facing2.getAxis() != Axis.Z ? facing3 : facing2 : facing);
	}
	
	public static LittleCorner getCorner(EnumFacing x, EnumFacing y, EnumFacing z)
	{
		for (LittleCorner corner : LittleCorner.values()) {
			if(corner.x == x && corner.y == y && corner.z == z)
				return corner;
		}
		return null;
	}
	
	public static VertexInformation getInfoFromFacings(EnumFacing x, EnumFacing y, EnumFacing z)
	{
		for (EnumFaceDirection direction : EnumFaceDirection.values()) {
			for (int i = 0; i < 4; i++) {
				VertexInformation info = direction.getVertexInformation(i);
				if(info.xIndex == x.ordinal() && info.yIndex == y.ordinal() && info.zIndex == z.ordinal())
					return info;
			}
		}
		return null;
	}
	
}
