package com.creativemd.littletiles.common.tile.place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.Placement;
import com.creativemd.littletiles.common.util.place.Placement.PlacementBlock;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;

public class PlacePreviewFacing extends PlacePreview {
	
	public EnumFacing facing;
	public int color;
	
	public PlacePreviewFacing(LittleBox box, EnumFacing facing, int color) {
		super(box.copy(), null);
		this.facing = facing;
		this.color = color;
	}
	
	@Override
	public boolean needsCollisionTest() {
		return false;
	}
	
	@Override
	public boolean requiresSplit() {
		return false;
	}
	
	@Override
	public PlacePreview copy() {
		return new PlacePreviewFacing(box.copy(), facing, color);
	}
	
	@Override
	public List<LittleRenderBox> getPreviews(LittleGridContext context) {
		List<LittleRenderBox> cubes = new ArrayList<>();
		LittleRenderBox cube = new LittleRenderBox(box.getCube(context), box, LittleTiles.coloredBlock, 0);
		cube.setColor(color);
		float thickness = 1 / 32F;
		Axis axis = facing.getAxis();
		if (facing.getAxisDirection() == AxisDirection.POSITIVE) {
			cube.setMin(axis, cube.getMax(axis));
			cube.setMax(axis, cube.getMax(axis) + thickness);
		} else {
			cube.setMax(axis, cube.getMin(axis));
			cube.setMin(axis, cube.getMin(axis) - thickness);
		}
		cubes.add(cube);
		return cubes;
	}
	
	@Override
	public List<LittleTile> placeTile(Placement placement, PlacementBlock block, IParentTileList parent, boolean requiresCollisionTest) {
		return Collections.EMPTY_LIST;
	}
	
}
