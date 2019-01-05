package com.creativemd.littletiles.common.structure.relative;

import javax.vecmath.Vector3d;

import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVecContext;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class StructureAbsolute extends StructureRelative {
	
	public static int intFloorDiv(int coord, int bucketSize) {
		return coord < 0 ? -((-coord - 1) / bucketSize) - 1 : coord / bucketSize;
	}
	
	public final LittleTileVecContext inBlockOffset;
	public final BlockPos baseOffset;
	public final BlockPos chunkOffset;
	public final BlockPos inChunkOffset;
	
	public final Vector3d rotationCenter;
	public final Vector3d rotationCenterInsideBlock;
	
	public StructureAbsolute(BlockPos pos, LittleTileBox box, LittleGridContext context) {
		super(box, context);
		
		LittleTileVecContext minVec = getMinVec();
		BlockPos minPosOffset = minVec.getBlockPos();
		sub(minPosOffset);
		
		this.inBlockOffset = getMinVec();
		
		this.baseOffset = pos.add(minPosOffset);
		
		this.chunkOffset = new BlockPos(baseOffset.getX() >> 4, baseOffset.getY() >> 4, baseOffset.getZ() >> 4);
		int chunkX = intFloorDiv(baseOffset.getX(), 16);
		int chunkY = intFloorDiv(baseOffset.getY(), 16);
		int chunkZ = intFloorDiv(baseOffset.getZ(), 16);
		
		this.inChunkOffset = new BlockPos(baseOffset.getX() - (chunkX * 16), baseOffset.getY() - (chunkY * 16), baseOffset.getZ() - (chunkZ * 16));
		
		this.rotationCenterInsideBlock = getCenter();
		this.rotationCenter = new Vector3d(rotationCenterInsideBlock);
		this.rotationCenter.x += baseOffset.getX();
		this.rotationCenter.y += baseOffset.getY();
		this.rotationCenter.z += baseOffset.getZ();
	}
	
	public StructureAbsolute(LittleTilePos pos, LittleTileBox box, LittleGridContext context) {
		super(box, context);
		add(pos.contextVec);
		
		LittleTileVecContext minVec = getMinVec();
		BlockPos minPosOffset = minVec.getBlockPos();
		sub(minPosOffset);
		minVec.sub(minPosOffset);
		
		this.inBlockOffset = minVec;
		
		this.baseOffset = pos.pos.add(minPosOffset);
		
		this.chunkOffset = new BlockPos(baseOffset.getX() >> 4, baseOffset.getY() >> 4, baseOffset.getZ() >> 4);
		int chunkX = intFloorDiv(baseOffset.getX(), 16);
		int chunkY = intFloorDiv(baseOffset.getY(), 16);
		int chunkZ = intFloorDiv(baseOffset.getZ(), 16);
		
		this.inChunkOffset = new BlockPos(baseOffset.getX() - (chunkX * 16), baseOffset.getY() - (chunkY * 16), baseOffset.getZ() - (chunkZ * 16));
		
		this.rotationCenterInsideBlock = getCenter();
		this.rotationCenter = new Vector3d(rotationCenterInsideBlock);
		this.rotationCenter.x += baseOffset.getX();
		this.rotationCenter.y += baseOffset.getY();
		this.rotationCenter.z += baseOffset.getZ();
	}
	
	public StructureAbsolute(LittleTilePos pos, StructureRelative relative) {
		this(pos, relative.box.copy(), relative.context);
	}
	
	public StructureAbsolute(String name, NBTTagCompound nbt) {
		this(getPos(nbt.getIntArray(name + "_pos")), LittleTileBox.createBox(nbt.getIntArray(name + "_box")), LittleGridContext.get(nbt.getInteger(name + "_grid")));
	}
	
	public StructureAbsolute(LittleTilePos axis, LittleTileVec additional) {
		this(axis.pos, convertAxisToBox(axis.contextVec, additional), axis.getContext());
	}
	
	public void writeToNBT(String name, NBTTagCompound nbt) {
		nbt.setIntArray(name + "_pos", new int[] { baseOffset.getX(), baseOffset.getY(), baseOffset.getZ() });
		nbt.setInteger(name + "_grid", context.size);
		nbt.setIntArray(name + "_box", box.getArray());
	}
	
	public LittleTileVec getDoubledCenterVec() {
		return new LittleTileVec((box.maxX * 2 - box.minX * 2) / 2, (box.maxY * 2 - box.minY * 2) / 2, (box.maxZ * 2 - box.minZ * 2) / 2);
	}
	
	private static BlockPos getPos(int[] array) {
		return new BlockPos(array[0], array[1], array[2]);
	}
	
	public static LittleTileBox convertAxisToBox(LittleTileVecContext vec, LittleTileVec additional) {
		if (additional.x == 0)
			return new LittleTileBox(vec.vec.x - 1, vec.vec.y - 1, vec.vec.z - 1, vec.vec.x + 1, vec.vec.y + 1, vec.vec.z + 1);
		return new LittleTileBox(additional.x > 0 ? vec.vec.x : vec.vec.x - 1, additional.y > 0 ? vec.vec.y : vec.vec.y - 1, additional.z > 0 ? vec.vec.z : vec.vec.z - 1, additional.x > 0 ? vec.vec.x + 1 : vec.vec.x, additional.y > 0 ? vec.vec.y + 1 : vec.vec.y, additional.z > 0 ? vec.vec.z + 1 : vec.vec.z);
	}
}
