package com.creativemd.littletiles.common.tile.place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileList;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.PlacementMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

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
	public List<LittleRenderingCube> getPreviews(LittleGridContext context) {
		List<LittleRenderingCube> cubes = new ArrayList<>();
		LittleRenderingCube cube = new LittleRenderingCube(box.getCube(context), box, LittleTiles.coloredBlock, 0);
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
	public List<LittleTile> placeTile(EntityPlayer player, BlockPos pos, LittleGridContext context, TileEntityLittleTiles teLT, TileList list, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, PlacementMode mode, EnumFacing facing, boolean requiresCollisionTest, LittleStructure structure) {
		return Collections.EMPTY_LIST;
	}
	
}
