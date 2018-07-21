package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.box.ColoredCube;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.structure.LittleDoor.LittleRelativeDoubledAxis;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVecContext;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlacePreviewTileAxis extends PlacePreviewTile{
	
	public static int red = ColorUtils.VecToInt(new Vec3d(1, 0, 0));
	public EnumFacing.Axis axis;
	public LittleTileVec additionalOffset;

	public PlacePreviewTileAxis(LittleTileBox box, LittleTilePreview preview, EnumFacing.Axis axis, LittleTileVec additionalOffset) {
		super(box, preview);
		this.axis = axis;
		this.additionalOffset = additionalOffset;
	}
	
	@Override
	public boolean needsCollisionTest()
	{
		return false;
	}
	
	@Override
	public PlacePreviewTile copy()
	{
		return new PlacePreviewTileAxis(box.copy(), null, axis, additionalOffset.copy());
	}
	
	@Override
	public List<LittleRenderingCube> getPreviews(LittleGridContext context)
	{
		ArrayList<LittleRenderingCube> cubes = new ArrayList<>();
		LittleTileBox preview = box.copy();
		int max = 40*context.size;
		int min = -max;
		switch(axis)
		{
		case X:
			preview.minX = min;
			preview.maxX = max;
			break;
		case Y:
			preview.minY = min;
			preview.maxY = max;
			break;
		case Z:
			preview.minZ = min;
			preview.maxZ = max;
			break;
		default:
			break;
		}
		LittleRenderingCube cube = preview.getRenderingCube(context, null, 0);
		cube.sub(new Vec3d(context.gridMCLength/2, context.gridMCLength/2, context.gridMCLength/2));
		cube.add(additionalOffset.getVec(context).scale(0.5));
		cube.color = red;
		cubes.add(cube);
		return cubes;
	}
	
	@Override
	public List<LittleTile> placeTile(EntityPlayer player, ItemStack stack, BlockPos pos, LittleGridContext context, TileEntityLittleTiles teLT, LittleStructure structure, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, PlacementMode mode, EnumFacing facing, boolean requiresCollisionTest)
	{
		if(structure instanceof LittleDoor)
		{
			LittleDoor door = (LittleDoor) structure;
			LittleTilePos absolute = new LittleTilePos(pos, context, box.getMinVec());
			if(door.getMainTile() == null)
				door.selectMainTile();
			LittleTileVecContext vec = absolute.getRelative(door.getMainTile().getAbsolutePos());
			door.doubledRelativeAxis = new LittleRelativeDoubledAxis(vec.context, vec.vec, additionalOffset.copy());
		}
		return Collections.EMPTY_LIST;
	}

}
