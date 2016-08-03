package com.creativemd.littletiles.utils;

import java.util.ArrayList;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PreviewTile {
	
	public static final Vec3d white = new Vec3d(1, 1, 1);

	public LittleTileBox box;
	public LittleTilePreview preview;
	
	public PreviewTile(LittleTileBox box, LittleTilePreview preview)
	{
		this.box = box;
		this.preview = preview;
	}
	
	public PreviewTile copy()
	{
		return new PreviewTile(box.copy(), preview.copy());
	}
	
	public Vec3d getPreviewColor()
	{
		return white;
	}
	
	public boolean needsCollisionTest()
	{
		return true;
	}
	
	public LittleTileBox getPreviewBox()
	{
		return box;
	}
	
	public LittleTile placeTile(EntityPlayer player, ItemStack stack, TileEntityLittleTiles teLT, LittleStructure structure, ArrayList<LittleTile> unplaceableTiles)
	{
		//PreviewTile tile = placeTiles.get(j);
		LittleTile LT = preview.getLittleTile(teLT);
		if(LT == null)
			return null;
		
		LT.boundingBoxes.clear();
		LT.boundingBoxes.add(box.copy());
		LT.updateCorner();
		
		if(structure != null)
		{
			LT.isStructureBlock = true;
			LT.structure = structure;
			structure.getTiles().add(LT);
		}
		
		if(teLT.isSpaceForLittleTile(box.copy()))
		{
			LT.place();
			LT.onPlaced(player, stack);
			return LT;
		}else if(unplaceableTiles != null){
			unplaceableTiles.add(LT);
		}
		return null;
	}
	
	public boolean split(HashMapList<BlockPos, PreviewTile> tiles, int x, int y, int z)
	{
		//box.resort();
		
		if(preview != null && !preview.canSplit && box.needsMultipleBlocks())
			return false;
		
		//if(!box.isValidBox())
			//System.out.println("Invalid box found!");
		//int tilesCount = 0;
		//ArrayList<LittleTileBox> failedBoxes = new ArrayList<LittleTileBox>();
		
		LittleTileSize size = box.getSize();
		
		int offX = box.minX/16;
		if(box.minX < 0)
			offX = (int) Math.floor(box.minX/16D);
		int offY = box.minY/16;
		if(box.minY < 0)
			offY = (int) Math.floor(box.minY/16D);
		int offZ = box.minZ/16;
		if(box.minZ < 0)
			offZ = (int) Math.floor(box.minZ/16D);
		
		int posX = x+offX;
		int posY = y+offY;
		int posZ = z+offZ;
		
		int spaceX = box.minX-offX*16;
		int spaceY = box.minY-offY*16;
		int spaceZ = box.minZ-offZ*16;
		
		for (int i = 0; spaceX+size.sizeX > i*16; i++) {
			posY = y+offY;
			for (int j = 0; spaceY+size.sizeY > j*16; j++) {
				posZ = z+offZ;
				for (int h = 0; spaceZ+size.sizeZ > h*16; h++) {
					
					PreviewTile tile = this.copy();
					if(i > 0)
						tile.box.minX =	0;
					else
						tile.box.minX = spaceX;
					if(i*16+16 > spaceX+size.sizeX)
					{
						tile.box.maxX = (box.maxX-box.maxX/16*16);
						if(box.maxX < 0)
							tile.box.maxX = 16+tile.box.maxX;
					}
					else
						tile.box.maxX = 16;
					
					if(j > 0)
						tile.box.minY =	0;
					else
						tile.box.minY = spaceY;
					if(j*16+16 > spaceY+size.sizeY)
					{
						tile.box.maxY = (box.maxY-box.maxY/16*16);
						if(box.maxY < 0)
							tile.box.maxY = 16+tile.box.maxY;
					}
					else
						tile.box.maxY = 16;
					
					if(h > 0)
						tile.box.minZ =	0;
					else
						tile.box.minZ = spaceZ;
					if(h*16+16 > spaceZ+size.sizeZ)
					{
						tile.box.maxZ = (box.maxZ-box.maxZ/16*16);
						if(box.maxZ < 0)
							tile.box.maxZ = 16+tile.box.maxZ;
					}
					else
						tile.box.maxZ = 16;
					
					if(tile.box.isValidBox())
					{
						tiles.add(new BlockPos(posX, posY, posZ), tile);
						//tilesCount++;
					}//else
						//failedBoxes.add(tile.box);
					posZ++;
				}
				posY++;
			}
			posX++;
		}
		
		/*if(tilesCount == 0)
		{
			System.out.println("Failed to split box!");
			System.out.println("Main Box: " + box.toString());
			for (int i = 0; i < failedBoxes.size(); i++) {
				System.out.println(failedBoxes.get(i).toString());
			}
			System.out.println("==============================");
		}*/
		
		return true;
	}
	
}
