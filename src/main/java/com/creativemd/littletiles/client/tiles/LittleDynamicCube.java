package com.creativemd.littletiles.client.tiles;

import javax.vecmath.Vector3f;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleSlice;

import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.EnumFaceDirection.VertexInformation;
import net.minecraft.util.EnumFacing;

public class LittleDynamicCube {
	
	public LittleDynamicCube(CubeObject defaultCube, LittleSlice slice, LittleTileSize size)
	{
		this.defaultCube = defaultCube;
		for (LittleCorner corner : LittleCorner.values()) {
			if(slice.isCornerAffected(corner.info))
			{
				Vector3f vec = new Vector3f(defaultCube.getVertexInformationPosition(corner.info.xIndex), defaultCube.getVertexInformationPosition(corner.info.yIndex), defaultCube.getVertexInformationPosition(corner.info.zIndex));
				slice.sliceVector(corner.info, vec, defaultCube, size);
				set(corner.info, vec);
			}
		}
	}
	
	public CubeObject defaultCube;
	
	// 1, 1, 1
	public Vector3f EUN;
	public Vector3f EUS;
	public Vector3f EDN;
	public Vector3f EDS;
	public Vector3f WUN;
	public Vector3f WUS;
	public Vector3f WDN;
	// 0, 0, 0
	public Vector3f WDS;
	
	public float getX(VertexInformation info)
	{
		Vector3f point = get(info);
		if(point != null)
			return point.x;
		return defaultCube.getVertexInformationPosition(info.xIndex);
	}
	
	public float getY(VertexInformation info)
	{
		Vector3f point = get(info);
		if(point != null)
			return point.y;
		return defaultCube.getVertexInformationPosition(info.yIndex);
	}
	
	public float getZ(VertexInformation info)
	{
		Vector3f point = get(info);
		if(point != null)
			return point.z;
		return defaultCube.getVertexInformationPosition(info.zIndex);
	}
	
	public Vector3f get(VertexInformation info, Vector3f output)
	{
		Vector3f point = get(info);
		if(point != null)
			output.set(point);
		else{
			output.set(defaultCube.getVertexInformationPosition(info.xIndex), defaultCube.getVertexInformationPosition(info.yIndex), defaultCube.getVertexInformationPosition(info.zIndex));
		}
		return output;
	}
	
	public void set(VertexInformation info, Vector3f vec)
	{
		if(info.xIndex == EnumFaceDirection.Constants.EAST_INDEX)
		{
			if(info.yIndex == EnumFaceDirection.Constants.UP_INDEX)
				if(info.zIndex == EnumFaceDirection.Constants.NORTH_INDEX)
					EUN = vec;
				else
					EUS = vec;
			else
				if(info.zIndex == EnumFaceDirection.Constants.NORTH_INDEX)
					EDN = vec;
				else
					EDS = vec;
		}else{
			if(info.yIndex == EnumFaceDirection.Constants.UP_INDEX)
				if(info.zIndex == EnumFaceDirection.Constants.NORTH_INDEX)
					WUN = vec;
				else
					WUS = vec;
			else
				if(info.zIndex == EnumFaceDirection.Constants.NORTH_INDEX)
					WDN = vec;
				else
					WDS = vec;
		}
	}
	
	private Vector3f get(VertexInformation info)
	{
		if(info.xIndex == EnumFaceDirection.Constants.EAST_INDEX)
		{
			if(info.yIndex == EnumFaceDirection.Constants.UP_INDEX)
				return info.zIndex == EnumFaceDirection.Constants.NORTH_INDEX ? EUN : EUS;
			else
				return info.zIndex == EnumFaceDirection.Constants.NORTH_INDEX ? EDN : EDS;
		}else{
			if(info.yIndex == EnumFaceDirection.Constants.UP_INDEX)
				return info.zIndex == EnumFaceDirection.Constants.NORTH_INDEX ? WUN : WUS;
			else
				return info.zIndex == EnumFaceDirection.Constants.NORTH_INDEX ? WDN : WDS;
		}
	}
	
}
