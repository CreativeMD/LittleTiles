package com.creativemd.littletiles.client.render.tiles;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.creativecore.common.utils.math.vec.Ray2d;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleSlice;

import net.minecraft.client.renderer.EnumFaceDirection.VertexInformation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public class LittleDynamicCube {
	
	public LittleDynamicCube(CubeObject defaultCube, LittleSlice slice, LittleTileSize size) {
		this.defaultCube = defaultCube;
		this.slice = slice;
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		this.line = new Ray2d(one, two, new Vector3d(defaultCube.getValueOfFacing(slice.start.x), defaultCube.getValueOfFacing(slice.start.y), defaultCube.getValueOfFacing(slice.start.z)), defaultCube.getSize(one) * slice.getDirectionScale(one), defaultCube.getSize(two) * slice.getDirectionScale(two));
		this.preferedSide = slice.getPreferedSide(defaultCube.getSize());
		/*
		 * for (LittleCorner corner : LittleCorner.values()) {
		 * if(slice.isCornerAffected(corner)) { Vector3f vec = new
		 * Vector3f(defaultCube.getValueOfFacing(corner.x),
		 * defaultCube.getValueOfFacing(corner.y),
		 * defaultCube.getValueOfFacing(corner.z)); slice.sliceVector(corner, vec,
		 * defaultCube, size); set(corner, vec); } }
		 */
	}
	
	public CubeObject defaultCube;
	
	// 1, 1, 1
	/*
	 * public Vector3f EUN; public Vector3f EUS; public Vector3f EDN; public
	 * Vector3f EDS; public Vector3f WUN; public Vector3f WUS; public Vector3f WDN;
	 * // 0, 0, 0 public Vector3f WDS;
	 */
	
	public LittleSlice slice;
	public EnumFacing preferedSide;
	public Ray2d line;
	
	public Vector3f sliceVector(EnumFacing facing, Vector3f vec, BlockPos pos) {
		if (facing.getAxis() == slice.axis) {
			Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
			Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
			
			// Take care of bounds
			float value;
			if (slice.isFacingPositive(two))
				value = Math.min(RotationUtils.get(two, vec), (float) line.get(one, RotationUtils.get(one, vec) + RotationUtils.get(one, pos)) - RotationUtils.get(two, pos));
			else
				value = Math.max(RotationUtils.get(two, vec), (float) line.get(one, RotationUtils.get(one, vec) + RotationUtils.get(one, pos)) - RotationUtils.get(two, pos));
			
			// RotationUtils.setValue(vec, value, two);
			if (value >= defaultCube.getMin(two) && value < defaultCube.getMax(two))
				RotationUtils.setValue(vec, value, two);
			else {
				if (slice.isFacingPositive(one))
					value = Math.min(RotationUtils.get(one, vec), (float) line.get(two, RotationUtils.get(two, vec) + RotationUtils.get(two, pos)) - RotationUtils.get(one, pos));
				else
					value = Math.max(RotationUtils.get(one, vec), (float) line.get(two, RotationUtils.get(two, vec) + RotationUtils.get(two, pos)) - RotationUtils.get(one, pos));
				RotationUtils.setValue(vec, value, one);
			}
			return vec;
		}
		
		if (preferedSide != facing && slice.getEmptySide(facing.getAxis()) != facing)
			return vec;
		
		Axis axis = facing.getAxis();
		Axis different = RotationUtils.getDifferentAxis(facing.getAxis(), slice.axis);
		float value = (float) line.get(different, RotationUtils.get(different, vec) + RotationUtils.get(different, pos)) - RotationUtils.get(axis, pos);
		// if(value >= defaultCube.getMin(axis) && value < defaultCube.getMax(axis))
		RotationUtils.setValue(vec, value, axis);
		// else
		// RotationUtils.setValue(vec, (float) line.get(axis, RotationUtils.get(axis,
		// vec)), different);
		return vec;
	}
	
	public Vector3f get(EnumFacing facing, VertexInformation info, Vector3f output) {
		output.set(defaultCube.getVertexInformationPosition(info.xIndex), defaultCube.getVertexInformationPosition(info.yIndex), defaultCube.getVertexInformationPosition(info.zIndex));
		sliceVector(facing, output, BlockPos.ORIGIN);
		return output;
	}
	
	/*
	 * public float getX(VertexInformation info) { Vector3f point = get(info);
	 * if(point != null) return point.x; return
	 * defaultCube.getVertexInformationPosition(info.xIndex); }
	 * 
	 * public float getY(VertexInformation info) { Vector3f point = get(info);
	 * if(point != null) return point.y; return
	 * defaultCube.getVertexInformationPosition(info.yIndex); }
	 * 
	 * public float getZ(VertexInformation info) { Vector3f point = get(info);
	 * if(point != null) return point.z; return
	 * defaultCube.getVertexInformationPosition(info.zIndex); }
	 * 
	 * public Vector3f get(VertexInformation info, Vector3f output) { Vector3f point
	 * = get(info); if(point != null) output.set(point); else{
	 * output.set(defaultCube.getVertexInformationPosition(info.xIndex),
	 * defaultCube.getVertexInformationPosition(info.yIndex),
	 * defaultCube.getVertexInformationPosition(info.zIndex)); } return output; }
	 * 
	 * public void set(LittleCorner corner, Vector3f vec) { if(corner.x ==
	 * EnumFacing.EAST) { if(corner.y == EnumFacing.UP) if(corner.z ==
	 * EnumFacing.NORTH) EUN = vec; else EUS = vec; else if(corner.z ==
	 * EnumFacing.NORTH) EDN = vec; else EDS = vec; }else{ if(corner.y ==
	 * EnumFacing.UP) if(corner.z == EnumFacing.NORTH) WUN = vec; else WUS = vec;
	 * else if(corner.z == EnumFacing.NORTH) WDN = vec; else WDS = vec; } }
	 */
	
}
