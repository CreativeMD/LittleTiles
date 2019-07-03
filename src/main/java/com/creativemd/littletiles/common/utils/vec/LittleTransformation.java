package com.creativemd.littletiles.common.utils.vec;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVecContext;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public class LittleTransformation {
	
	public BlockPos center;
	
	public int rotX;
	public int rotY;
	public int rotZ;
	
	public LittleTileVec doubledRotationCenter;
	
	public LittleTileVecContext offset;
	
	public LittleTransformation(int[] array) {
		if (array.length != 13)
			throw new IllegalArgumentException("Invalid array when creating door transformation!");
		
		center = new BlockPos(array[0], array[1], array[2]);
		rotX = array[3];
		rotY = array[4];
		rotZ = array[5];
		doubledRotationCenter = new LittleTileVec(array[6], array[7], array[8]);
		offset = new LittleTileVecContext(LittleGridContext.get(array[12]), new LittleTileVec(array[9], array[10], array[11]));
	}
	
	public LittleTransformation(BlockPos center, int rotX, int rotY, int rotZ, LittleTileVec doubledRotationCenter, LittleTileVecContext offset) {
		this.center = center;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.doubledRotationCenter = doubledRotationCenter;
		this.offset = offset;
	}
	
	public LittleTransformation(BlockPos center, Rotation rotation) {
		this.center = center;
		this.rotX = rotation.axis == Axis.X ? (rotation.clockwise ? 1 : -1) : 0;
		this.rotY = rotation.axis == Axis.Y ? (rotation.clockwise ? 1 : -1) : 0;
		this.rotZ = rotation.axis == Axis.Z ? (rotation.clockwise ? 1 : -1) : 0;
		this.doubledRotationCenter = new LittleTileVec(0, 0, 0);
		this.offset = new LittleTileVecContext();
	}
	
	public Rotation getRotation(Axis axis) {
		switch (axis) {
		case X:
			if (rotX == 0)
				return null;
			return Rotation.getRotation(axis, rotX > 0);
		case Y:
			if (rotY == 0)
				return null;
			return Rotation.getRotation(axis, rotY > 0);
		case Z:
			if (rotZ == 0)
				return null;
			return Rotation.getRotation(axis, rotZ > 0);
		}
		return null;
	}
	
	public BlockPos transform(BlockPos pos) {
		pos = pos.subtract(center);
		if (rotX != 0) {
			Rotation rotation = getRotation(Axis.X);
			for (int i = 0; i < Math.abs(rotX); i++)
				pos = RotationUtils.rotate(pos, rotation);
		}
		if (rotY != 0) {
			Rotation rotation = getRotation(Axis.Y);
			for (int i = 0; i < Math.abs(rotY); i++)
				pos = RotationUtils.rotate(pos, rotation);
		}
		if (rotZ != 0) {
			Rotation rotation = getRotation(Axis.Z);
			for (int i = 0; i < Math.abs(rotZ); i++)
				pos = RotationUtils.rotate(pos, rotation);
		}
		
		pos = pos.add(center);
		
		if (offset != null)
			pos = pos.add(offset.getBlockPos());
		return pos;
	}
	
	public void transform(LittleAbsolutePreviews previews) {
		if (rotX != 0) {
			Rotation rotation = getRotation(Axis.X);
			for (int i = 0; i < Math.abs(rotX); i++)
				previews.rotatePreviews(rotation, doubledRotationCenter);
		}
		if (rotY != 0) {
			Rotation rotation = getRotation(Axis.Y);
			for (int i = 0; i < Math.abs(rotY); i++)
				previews.rotatePreviews(rotation, doubledRotationCenter);
		}
		if (rotZ != 0) {
			Rotation rotation = getRotation(Axis.Z);
			for (int i = 0; i < Math.abs(rotZ); i++)
				previews.rotatePreviews(rotation, doubledRotationCenter);
		}
		
		if (offset != null)
			previews.movePreviews(offset.context, offset.vec);
	}
	
	public int[] array() {
		return new int[] { center.getX(), center.getY(), center.getZ(), rotX, rotY, rotZ, doubledRotationCenter.x,
		        doubledRotationCenter.y, doubledRotationCenter.z, offset.vec.x, offset.vec.y, offset.vec.z,
		        offset.context.size };
	}
	
	@Override
	public String toString() {
		return "center:" + center.getX() + "," + center.getY() + "," + center.getZ() + ";rotation:" + rotX + "," + rotY + "," + rotZ + ";offset:" + offset.vec.x + "," + offset.vec.y + "," + offset.vec.z + ";context:" + offset.context;
	}
}
