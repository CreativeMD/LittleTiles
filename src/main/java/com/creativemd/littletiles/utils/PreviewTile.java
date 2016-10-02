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
	
	public LittleTile placeTile(EntityPlayer player, ItemStack stack, TileEntityLittleTiles teLT, LittleStructure structure, ArrayList<LittleTile> unplaceableTiles, boolean forced)
	{
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
		}else if(forced){
			ArrayList<LittleTileBox> newBoxes = new ArrayList<>();
			ArrayList<LittleTileBox> unplaceableBoxes = new ArrayList<>();
			for (int littleX = box.minX; littleX < box.maxX; littleX++) {
				for (int littleY = box.minY; littleY < box.maxY; littleY++) {
					for (int littleZ = box.minZ; littleZ < box.maxZ; littleZ++) {
						LittleTileBox newBox = new LittleTileBox(littleX, littleY, littleZ, littleX+1, littleY+1, littleZ+1);
						if(teLT.isSpaceForLittleTile(newBox))
							newBoxes.add(newBox);
						else
							unplaceableBoxes.add(newBox);
					}
				}
			}
			
			LittleTileBox.combineBoxes(newBoxes);
			LittleTile first = null;
			for (int i = 0; i < newBoxes.size(); i++) {
				LittleTile newTile = LT.copy();
				newTile.boundingBoxes.clear();
				newTile.boundingBoxes.add(newBoxes.get(i));
				newTile.place();
				newTile.onPlaced(player, stack);
				if(i == 0)
					first = newTile;
			}
			
			LittleTileBox.combineBoxes(unplaceableBoxes);
			for (int i = 0; i < unplaceableBoxes.size(); i++) {
				LittleTile newTile = LT.copy();
				newTile.boundingBoxes.clear();
				newTile.boundingBoxes.add(unplaceableBoxes.get(i));
				unplaceableTiles.add(newTile);
			}
			
			return first;			
		}else if(unplaceableTiles != null){
			unplaceableTiles.add(LT);
		}
		return null;
	}
	
	public boolean split(HashMapList<BlockPos, PreviewTile> tiles, BlockPos pos)
	{		
		if(preview != null && !preview.canSplit && box.needsMultipleBlocks())
			return false;
		
		LittleTileSize size = box.getSize();
		
		int offX = box.minX/LittleTile.gridSize;
		if(box.minX < 0)
			offX = (int) Math.floor(box.minX/(double)LittleTile.gridSize);
		int offY = box.minY/LittleTile.gridSize;
		if(box.minY < 0)
			offY = (int) Math.floor(box.minY/(double)LittleTile.gridSize);
		int offZ = box.minZ/LittleTile.gridSize;
		if(box.minZ < 0)
			offZ = (int) Math.floor(box.minZ/(double)LittleTile.gridSize);
		
		int posX = pos.getX()+offX;
		int posY = pos.getY()+offY;
		int posZ = pos.getZ()+offZ;
		
		int spaceX = box.minX-offX*LittleTile.gridSize;
		int spaceY = box.minY-offY*LittleTile.gridSize;
		int spaceZ = box.minZ-offZ*LittleTile.gridSize;
		
		for (int i = 0; spaceX+size.sizeX > i*LittleTile.gridSize; i++) {
			posY = pos.getY()+offY;
			for (int j = 0; spaceY+size.sizeY > j*LittleTile.gridSize; j++) {
				posZ = pos.getZ()+offZ;
				for (int h = 0; spaceZ+size.sizeZ > h*LittleTile.gridSize; h++) {
					
					PreviewTile tile = this.copy();
					if(i > 0)
						tile.box.minX =	0;
					else
						tile.box.minX = spaceX;
					if(i*LittleTile.gridSize+LittleTile.gridSize > spaceX+size.sizeX)
					{
						tile.box.maxX = (box.maxX-box.maxX/LittleTile.gridSize*LittleTile.gridSize);
						if(box.maxX < 0)
							tile.box.maxX = LittleTile.gridSize+tile.box.maxX;
					}
					else
						tile.box.maxX = LittleTile.gridSize;
					
					if(j > 0)
						tile.box.minY =	0;
					else
						tile.box.minY = spaceY;
					if(j*LittleTile.gridSize+LittleTile.gridSize > spaceY+size.sizeY)
					{
						tile.box.maxY = (box.maxY-box.maxY/LittleTile.gridSize*LittleTile.gridSize);
						if(box.maxY < 0)
							tile.box.maxY = LittleTile.gridSize+tile.box.maxY;
					}
					else
						tile.box.maxY = LittleTile.gridSize;
					
					if(h > 0)
						tile.box.minZ =	0;
					else
						tile.box.minZ = spaceZ;
					if(h*LittleTile.gridSize+LittleTile.gridSize > spaceZ+size.sizeZ)
					{
						tile.box.maxZ = (box.maxZ-box.maxZ/LittleTile.gridSize*LittleTile.gridSize);
						if(box.maxZ < 0)
							tile.box.maxZ = LittleTile.gridSize+tile.box.maxZ;
					}
					else
						tile.box.maxZ = LittleTile.gridSize;
					
					if(tile.box.isValidBox())
					{
						tiles.add(new BlockPos(posX, posY, posZ), tile);
					}
					posZ++;
				}
				posY++;
			}
			posX++;
		}
		
		return true;
	}
	
}
