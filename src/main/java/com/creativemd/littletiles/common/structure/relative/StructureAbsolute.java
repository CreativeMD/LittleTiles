package com.creativemd.littletiles.common.structure.relative;

import javax.vecmath.Vector3d;

import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class StructureAbsolute extends StructureRelative {
	
	public static int intFloorDiv(int coord, int bucketSize) {
		return coord < 0 ? -((-coord - 1) / bucketSize) - 1 : coord / bucketSize;
	}
	
	public final LittleVecContext inBlockOffset;
	public final BlockPos baseOffset;
	public final BlockPos chunkOffset;
	public final BlockPos inChunkOffset;
	
	public final Vector3d rotationCenter;
	public final Vector3d rotationCenterInsideBlock;
	
	public StructureAbsolute(BlockPos pos, LittleBox box, LittleGridContext context) {
		super(box, context);
		
		LittleVecContext minVec = getMinVec();
		BlockPos minPosOffset = minVec.getBlockPos();
		sub(minPosOffset);
		
		this.inBlockOffset = minVec;
		
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
	
	public StructureAbsolute(LittleAbsoluteVec pos, LittleBox box, LittleGridContext context) {
		super(box, context);
		add(pos.getVecContext());
		
		LittleVecContext minVec = getMinVec();
		BlockPos minPosOffset = minVec.getBlockPos();
		sub(minPosOffset);
		minVec.sub(minPosOffset);
		
		this.inBlockOffset = minVec;
		
		this.baseOffset = pos.getPos().add(minPosOffset);
		
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
	
	public StructureAbsolute(LittleAbsoluteVec pos, StructureRelative relative) {
		this(pos, relative.box.copy(), relative.context);
	}
	
	public StructureAbsolute(String name, NBTTagCompound nbt) {
		this(getPos(nbt.getIntArray(name + "_pos")), LittleBox.createBox(nbt.getIntArray(name + "_box")), LittleGridContext.get(nbt.getInteger(name + "_grid")));
	}
	
	public StructureAbsolute(LittleAbsoluteVec axis, LittleVec additional) {
		this(axis.getPos(), convertAxisToBox(axis.getVecContext(), additional), axis.getContext());
	}
	
	public void writeToNBT(String name, NBTTagCompound nbt) {
		nbt.setIntArray(name + "_pos", new int[] { baseOffset.getX(), baseOffset.getY(), baseOffset.getZ() });
		nbt.setInteger(name + "_grid", context.size);
		nbt.setIntArray(name + "_box", box.getArray());
	}
	
	@Override
	public LittleVec getDoubledCenterVec() {
		return new LittleVec((box.maxX * 2 - box.minX * 2) / 2, (box.maxY * 2 - box.minY * 2) / 2, (box.maxZ * 2 - box.minZ * 2) / 2);
	}
	
	private static BlockPos getPos(int[] array) {
		return new BlockPos(array[0], array[1], array[2]);
	}
	
	public static LittleBox convertAxisToBox(LittleVecContext vec, LittleVec additional) {
		if (additional.x == 0)
			return new LittleBox(vec.getVec().x - 1, vec.getVec().y - 1, vec.getVec().z - 1, vec.getVec().x + 1, vec.getVec().y + 1, vec.getVec().z + 1);
		return new LittleBox(additional.x > 0 ? vec.getVec().x : vec.getVec().x - 1, additional.y > 0 ? vec.getVec().y : vec.getVec().y - 1, additional.z > 0 ? vec.getVec().z : vec.getVec().z - 1, additional.x > 0 ? vec.getVec().x + 1 : vec.getVec().x, additional.y > 0 ? vec.getVec().y + 1 : vec.getVec().y, additional.z > 0 ? vec.getVec().z + 1 : vec.getVec().z);
	}
}
