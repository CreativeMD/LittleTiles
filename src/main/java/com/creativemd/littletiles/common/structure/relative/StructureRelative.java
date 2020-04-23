package com.creativemd.littletiles.common.structure.relative;

import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.structure.directional.StructureDirectionalField;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.place.PlacePreviewRelative;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.IGridBased;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public class StructureRelative implements IGridBased {
	
	public StructureRelative(LittleBox box, LittleGridContext context) {
		this.box = box;
		this.context = context;
	}
	
	public StructureRelative(int[] array) {
		this.box = new LittleBox(array[0], array[1], array[2], array[3], array[4], array[5]);
		this.context = LittleGridContext.get(array[6]);
	}
	
	protected LittleGridContext context;
	protected LittleBox box;
	
	public LittleVec getDoubledCenterVec() {
		return new LittleVec(box.maxX + box.minX, box.maxY + box.minY, box.maxZ + box.minZ);
	}
	
	public Vector3d getCenter() {
		return new Vector3d(context.toVanillaGrid(box.maxX + box.minX) / 2D, context.toVanillaGrid(box.maxY + box.minY) / 2D, context.toVanillaGrid(box.maxZ + box.minZ) / 2D);
	}
	
	public boolean isEven() {
		return box.getSize(Axis.X) > 1;
	}
	
	public LittleBox getBox() {
		return box;
	}
	
	@Override
	public LittleGridContext getContext() {
		return context;
	}
	
	@Override
	public void convertTo(LittleGridContext context) {
		box.convertTo(this.context, context);
		this.context = context;
	}
	
	@Override
	public void convertToSmallest() {
		int grid = box.getSmallestContext(context);
		if (grid < this.context.size)
			convertTo(LittleGridContext.get(grid));
	}
	
	public int[] write() {
		return new int[] { box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, context.size };
	}
	
	public void setBox(BlockPos pos, LittleBox box, LittleGridContext context) {
		this.box = box;
		this.context = context;
		add(pos);
	}
	
	public LittleVecContext getMinVec() {
		return new LittleVecContext(box.getMinVec(), context);
	}
	
	public PlacePreview getPlacePreview(LittlePreviews previews, StructureDirectionalField type) {
		return new PlacePreviewRelative(box, this, type);
	}
	
	public void move(LittleGridContext context, LittleVec offset) {
		int scale = 1;
		if (context.size > this.context.size)
			convertTo(context);
		else if (context.size < this.context.size)
			scale = this.context.size / context.size;
		
		box.add(offset.x * scale, offset.y * scale, offset.z * scale);
		
		convertToSmallest();
	}
	
	public void flip(LittleGridContext context, Axis axis, LittleVec doubledCenter) {
		if (context.size > this.context.size)
			convertTo(context);
		else if (context.size < this.context.size) {
			doubledCenter = doubledCenter.copy();
			doubledCenter.convertTo(context, this.context);
		}
		
		box.flipBox(axis, doubledCenter);
		
		convertToSmallest();
	}
	
	public void rotate(LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
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
		box.add(new LittleVec(context, pos));
	}
	
	public void add(LittleVecContext contextVec) {
		int scale = 1;
		if (contextVec.getContext().size > this.context.size)
			convertTo(contextVec.getContext());
		else if (contextVec.getContext().size < this.context.size)
			scale = this.context.size / contextVec.getContext().size;
		
		box.add(contextVec.getVec().x * scale, contextVec.getVec().y * scale, contextVec.getVec().z * scale);
		
		convertToSmallest();
	}
	
	public void sub(BlockPos pos) {
		box.sub(new LittleVec(context, pos));
	}
	
	public void sub(LittleVecContext contextVec) {
		int scale = 1;
		if (contextVec.getContext().size > this.context.size)
			convertTo(contextVec.getContext());
		else if (contextVec.getContext().size < this.context.size)
			scale = this.context.size / contextVec.getContext().size;
		
		box.sub(contextVec.getVec().x * scale, contextVec.getVec().y * scale, contextVec.getVec().z * scale);
		
		convertToSmallest();
	}
	
}
