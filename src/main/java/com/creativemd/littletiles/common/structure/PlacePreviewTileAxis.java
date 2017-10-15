package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.ColoredCube;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlacePreviewTileAxis extends PlacePreviewTile{
	
	public static int red = ColorUtils.VecToInt(new Vec3d(1, 0, 0));
	public EnumFacing.Axis axis;

	public PlacePreviewTileAxis(LittleTileBox box, LittleTilePreview preview, EnumFacing.Axis axis) {
		super(box, preview);
		this.axis = axis;
	}
	
	@Override
	public boolean needsCollisionTest()
	{
		return false;
	}
	
	@Override
	public PlacePreviewTile copy()
	{
		return new PlacePreviewTileAxis(box.copy(), null, axis);
	}
	
	@Override
	public List<LittleRenderingCube> getPreviews()
	{
		ArrayList<LittleRenderingCube> cubes = new ArrayList<>();
		LittleTileBox preview = box.copy();
		int max = 40*LittleTile.gridSize;
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
		LittleRenderingCube cube = preview.getRenderingCube(null, 0);
		cube.color = red;
		cubes.add(cube);
		return cubes;
	}
	
	@Override
	public LittleTile placeTile(EntityPlayer player, ItemStack stack, BlockPos pos, TileEntityLittleTiles teLT, LittleStructure structure, ArrayList<LittleTile> unplaceableTiles, boolean forced, EnumFacing facing, boolean requiresCollisionTest)
	{
		if(structure instanceof LittleDoor)
		{
			LittleDoor door = (LittleDoor) structure;
			door.axisVec = box.getMinVec();
			door.axisVec.add(new LittleTileVec(pos));
			if(door.getMainTile() == null)
				door.selectMainTile();
			if(door.getMainTile() != null)
				door.axisVec.sub(door.getMainTile().getAbsoluteCoordinates());
		}
		return null;
	}
	
	/*@Override
	public boolean split(HashMapList<ChunkCoordinates, PreviewTile> tiles, int x, int y, int z)
	{
		tiles.add(new ChunkCoordinates(x, y, z), this);
		return true;
	}*/

}
