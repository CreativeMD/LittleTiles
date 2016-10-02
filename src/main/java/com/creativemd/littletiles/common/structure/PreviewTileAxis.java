package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.PreviewTile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class PreviewTileAxis extends PreviewTile{
	
	public static Vec3d red = new Vec3d(1, 0, 0);
	public EnumFacing.Axis axis;

	public PreviewTileAxis(LittleTileBox box, LittleTilePreview preview, EnumFacing.Axis axis) {
		super(box, preview);
		this.axis = axis;
	}
	
	@Override
	public Vec3d getPreviewColor()
	{
		return red;
	}
	
	@Override
	public boolean needsCollisionTest()
	{
		return false;
	}
	
	@Override
	public PreviewTile copy()
	{
		return new PreviewTileAxis(box.copy(), null, axis);
	}
	
	@Override
	public LittleTileBox getPreviewBox()
	{
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
		return preview;
	}
	
	@Override
	public LittleTile placeTile(EntityPlayer player, ItemStack stack, TileEntityLittleTiles teLT, LittleStructure structure, ArrayList<LittleTile> unplaceableTiles, boolean forced)
	{
		if(structure instanceof LittleDoor)
		{
			LittleDoor door = (LittleDoor) structure;
			door.axisVec = box.getMinVec(); //Check if this would be perfect
			door.axisVec.addVec(new LittleTileVec(teLT.getPos().getX()*LittleTile.gridSize, teLT.getPos().getY()*LittleTile.gridSize, teLT.getPos().getZ()*LittleTile.gridSize));
			door.axisVec.subVec(door.mainTile.cornerVec);
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
