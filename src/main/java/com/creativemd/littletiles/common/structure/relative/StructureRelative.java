package com.creativemd.littletiles.common.structure.relative;

import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.StructureTypeRelative;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTileRelative;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVecContext;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public class StructureRelative {
	
	public StructureRelative(LittleTileBox box, LittleGridContext context) {
		this.box = box;
		this.context = context;
	}
	
	public StructureRelative(String name, NBTTagCompound nbt) {
		int[] array = nbt.getIntArray(name);
		this.box = new LittleTileBox(array[0], array[1], array[2], array[3], array[4], array[5]);
		this.context = LittleGridContext.get(array[6]);
	}
	
	protected LittleGridContext context;
	protected LittleTileBox box;
	
	public LittleTileVec getDoubledCenterVec() {
		return new LittleTileVec(box.maxX + box.minX, box.maxY + box.minY, box.maxZ + box.minZ);
	}
	
	public Vector3d getCenter() {
		return new Vector3d(context.toVanillaGrid(box.maxX + box.minX) / 2D, context.toVanillaGrid(box.maxY + box.minY) / 2D, context.toVanillaGrid(box.maxZ + box.minZ) / 2D);
	}
	
	public boolean isEven() {
		return box.getSize(Axis.X) > 1;
	}
	
	public LittleTileBox getBox() {
		return box;
	}
	
	public LittleGridContext getContext() {
		return context;
	}
	
	public void convertTo(LittleGridContext context) {
		box.convertTo(this.context, context);
		this.context = context;
	}
	
	public void convertToSmallest() {
		int grid = box.getSmallestContext(context);
		if (grid < this.context.size)
			convertTo(LittleGridContext.get(grid));
	}
	
	public void writeToNBT(String name, NBTTagCompound nbt) {
		nbt.setIntArray(name, new int[] { box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, context.size });
	}
	
	public void setBox(BlockPos pos, LittleTileBox box, LittleGridContext context) {
		this.box = box;
		this.context = context;
		add(pos);
	}
	
	public LittleTileVecContext getMinVec() {
		return new LittleTileVecContext(context, box.getMinVec());
	}
	
	public PlacePreviewTile getPlacePreview(LittlePreviews previews, StructureTypeRelative type) {
		return new PlacePreviewTileRelative(box, previews, this, type);
	}
	
	public void onMove(LittleStructure structure, LittleGridContext context, LittleTileVec offset) {
		int scale = 1;
		if (context.size > this.context.size)
			convertTo(context);
		else if (context.size < this.context.size)
			scale = this.context.size / context.size;
		
		box.add(offset.x * scale, offset.y * scale, offset.z * scale);
		
		convertToSmallest();
	}
	
	public void onFlip(LittleStructure structure, LittleGridContext context, Axis axis, LittleTileVec doubledCenter) {
		if (context.size > this.context.size)
			convertTo(context);
		else if (context.size < this.context.size) {
			doubledCenter = doubledCenter.copy();
			doubledCenter.convertTo(context, this.context);
		}
		
		box.flipBox(axis, doubledCenter);
		
		convertToSmallest();
	}
	
	public void onRotate(LittleStructure structure, LittleGridContext context, Rotation rotation, LittleTileVec doubledCenter) {
		if (context.size > this.context.size)
			convertTo(context);
		else if (context.size < this.context.size) {
			doubledCenter = doubledCenter.copy();
			doubledCenter.convertTo(context, this.context);
		}
		
		box.rotateBox(rotation, doubledCenter);
		
		convertToSmallest();
	}
	
	public BlockPos getOffset() {
		return box.getMinVec().getBlockPos(context);
	}
	
	public void add(BlockPos pos) {
		box.add(new LittleTileVec(context, pos));
	}
	
	public void add(LittleTileVecContext contextVec) {
		int scale = 1;
		if (contextVec.context.size > this.context.size)
			convertTo(contextVec.context);
		else if (contextVec.context.size < this.context.size)
			scale = this.context.size / contextVec.context.size;
		
		box.add(contextVec.vec.x * scale, contextVec.vec.y * scale, contextVec.vec.z * scale);
		
		convertToSmallest();
	}
	
	public void sub(BlockPos pos) {
		box.sub(new LittleTileVec(context, pos));
	}
	
	public void sub(LittleTileVecContext contextVec) {
		int scale = 1;
		if (contextVec.context.size > this.context.size)
			convertTo(contextVec.context);
		else if (contextVec.context.size < this.context.size)
			scale = this.context.size / contextVec.context.size;
		
		box.sub(contextVec.vec.x * scale, contextVec.vec.y * scale, contextVec.vec.z * scale);
		
		convertToSmallest();
	}
	
}
