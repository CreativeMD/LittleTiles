package com.creativemd.littletiles.common.util.place;

import java.util.List;

import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** PlacementPosition + Previews -> PlacementPreview (can be rendered) + Player/ Cause -> Placement */
public class PlacementPreview {
	
	public final World world;
	public final LittlePreviews previews;
	public final LittleGridContext context;
	public final PlacementMode mode;
	public final LittleBox box;
	public final LittleVec size;
	public final boolean fixed;
	public final BlockPos pos;
	public final LittleVec inBlockOffset;
	public final LittleVec cachedOffset;
	public final EnumFacing facing;
	
	public PlacementPreview(World world, LittlePreviews previews, PlacementMode mode, LittleBox box, boolean fixed, BlockPos pos, LittleVec inBlockOffset, EnumFacing facing) {
		this.world = world;
		this.previews = previews;
		this.context = previews.getContext();
		if (previews.hasStructureIncludeChildren() && mode.canPlaceStructures())
			mode = PlacementMode.getStructureDefault();
		this.mode = mode;
		this.box = box;
		this.size = box.getSize();
		this.fixed = fixed;
		this.pos = pos;
		if (fixed)
			this.inBlockOffset = null;
		else
			this.inBlockOffset = inBlockOffset;
		this.cachedOffset = inBlockOffset;
		this.facing = facing;
	}
	
	public List<PlacePreview> getPreviews() {
		return this.previews.getPlacePreviewsIncludingChildren(inBlockOffset);
	}
	
}
