package com.creativemd.littletiles.common.tiles.place;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.utils.ColoredCube;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.jcraft.jorbis.Block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlacePreviewTile {
	
	public static final Vec3d white = new Vec3d(1, 1, 1);

	public LittleTileBox box;
	public LittleTilePreview preview;
	
	public PlacePreviewTile(LittleTileBox box, LittleTilePreview preview)
	{
		this.box = box;
		this.preview = preview;
	}
	
	/**NEEDS TO BE OVERRIDEN! ALWAYS!**/
	public PlacePreviewTile copy()
	{
		return new PlacePreviewTile(box.copy(), preview.copy());
	}
	
	/**If false it will be placed after all regular tiles have been placed**/
	public boolean needsCollisionTest()
	{
		return true;
	}
	
	public ArrayList<ColoredCube> getPreviews()
	{
		ArrayList<ColoredCube> cubes = new ArrayList<>();
		cubes.add(new ColoredCube(box.getCube()));
		return cubes;
	}
	
	public LittleTile placeTile(@Nullable EntityPlayer player, ItemStack stack, BlockPos pos, TileEntityLittleTiles teLT, LittleStructure structure, ArrayList<LittleTile> unplaceableTiles, boolean forced, EnumFacing facing, boolean requiresCollisionTest)
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
			structure.addTile(LT);
		}
		
		if(!requiresCollisionTest || teLT.isSpaceForLittleTile(box.copy()))
		{
			LT.place();
			LT.onPlaced(player, stack, facing);
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
				newTile.onPlaced(player, stack, facing);
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
	
	public boolean split(HashMapList<BlockPos, PlacePreviewTile> tiles, BlockPos pos)
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
					
					LittleTileBox box = this.box.copy();
					if(i > 0)
						box.minX =	0;
					else
						box.minX = spaceX;
					if(i*LittleTile.gridSize+LittleTile.gridSize > spaceX+size.sizeX)
					{
						box.maxX = (box.maxX-box.maxX/LittleTile.gridSize*LittleTile.gridSize);
						if(box.maxX < 0)
							box.maxX = LittleTile.gridSize+box.maxX;
					}
					else
						box.maxX = LittleTile.gridSize;
					
					if(j > 0)
						box.minY =	0;
					else
						box.minY = spaceY;
					if(j*LittleTile.gridSize+LittleTile.gridSize > spaceY+size.sizeY)
					{
						box.maxY = (box.maxY-box.maxY/LittleTile.gridSize*LittleTile.gridSize);
						if(box.maxY < 0)
							box.maxY = LittleTile.gridSize+box.maxY;
					}
					else
						box.maxY = LittleTile.gridSize;
					
					if(h > 0)
						box.minZ =	0;
					else
						box.minZ = spaceZ;
					if(h*LittleTile.gridSize+LittleTile.gridSize > spaceZ+size.sizeZ)
					{
						box.maxZ = (box.maxZ-box.maxZ/LittleTile.gridSize*LittleTile.gridSize);
						if(box.maxZ < 0)
							box.maxZ = LittleTile.gridSize+box.maxZ;
					}
					else
						box.maxZ = LittleTile.gridSize;
					
					if(box.isValidBox())
					{
						PlacePreviewTile tile = this.copy();
						tile.box = box;
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
