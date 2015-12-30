package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;

import com.creativemd.creativecore.common.entity.EntitySit;
import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class LittleChair extends LittleStructure{

	public LittleChair() {
		
	}

	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		
	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
			
	}
	
	public LittleTileVec getHighestCenterPoint()
	{
		int minYPos = Integer.MAX_VALUE;
		
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		
		int maxYPos = Integer.MIN_VALUE;
		
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		
		HashMapList<ChunkCoordinates, LittleTile> coords = getTilesSortedPerBlock();
		if(coords.sizeOfValues() == 0)
			return null;
		for (int i = 0; i < coords.size(); i++) {
			ChunkCoordinates coord = coords.getKey(i);
			for (int j = 0; j < coords.getValues(i).size(); j++) {
				for (int h = 0; h < coords.getValues(i).get(j).boundingBoxes.size(); h++) {
					LittleTileBox box = coords.getValues(i).get(j).boundingBoxes.get(h);
					minX = Math.min(minX, coord.posX*16+box.minX);
					minY = Math.min(minY, coord.posY*16+box.minY);
					minZ = Math.min(minZ, coord.posZ*16+box.minZ);
					
					maxX = Math.max(maxX, coord.posX*16+box.maxX);
					maxY = Math.max(maxY, coord.posY*16+box.maxY);
					maxZ = Math.max(maxZ, coord.posZ*16+box.maxZ);
				}
				minYPos = Math.min(minYPos, coord.posY);
				maxYPos = Math.max(maxYPos, coord.posY);
			}
			/*
			minX = Math.min(minX, coord.posX);
			minY = Math.min(minY, coord.posY);
			minZ = Math.min(minZ, coord.posZ);
			
			maxX = Math.max(maxX, coord.posX);
			maxY = Math.max(maxY, coord.posY);
			maxZ = Math.max(maxZ, coord.posZ);*/
		}
		
		//double test = Math.floor(((minX+maxX)/16D/2D));
		int centerX = (int) Math.floor((minX+maxX)/16D/2D);
		int centerY = (int) Math.floor((minY+maxY)/16D/2D);
		int centerZ = (int) Math.floor((minZ+maxZ)/16D/2D);
		
		int centerTileX = (int) (Math.floor(minX+maxX)/2D)-centerX*16;
		int centerTileY = (int) (Math.floor(minY+maxY)/2D)-centerY*16;
		int centerTileZ = (int) (Math.floor(minZ+maxZ)/2D)-centerZ*16;
		
		LittleTileVec position = new LittleTileVec((minX+maxX)/2, minYPos*16, (minZ+maxZ)/2);
		//position.y = ;
		for (int y = minYPos; y <= maxYPos; y++) {
			ArrayList<LittleTile> tilesInCenter = coords.getValues(new ChunkCoordinates(centerX, y, centerZ));
			if(tilesInCenter != null)
			{
				LittleTileBox box = new LittleTileBox(centerTileX, LittleTile.minPos, centerTileZ, centerTileX+1, LittleTile.maxPos, centerTileZ+1);
				//int highest = LittleTile.minPos;
				for (int i = 0; i < tilesInCenter.size(); i++) {
					for (int j = 0; j < tilesInCenter.get(i).boundingBoxes.size(); j++) {
						LittleTileBox littleBox = tilesInCenter.get(i).boundingBoxes.get(j);
						if(box.intersectsWith(littleBox))
						{
							position.y = Math.max(y*16+littleBox.maxY, position.y);
							//highest = Math.max(highest, littleBox.maxY);
						}
					}
				}
				
			}
		}
		
		
		return position;
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, int x, int y, int z, EntityPlayer player, int side, float moveX, float moveY, float moveZ)
	{
		if(!world.isRemote)
		{
			LittleTileVec vec = getHighestCenterPoint();
			if(vec != null)
			{
				EntitySit sit = new EntitySit(world, vec.getPosX(), vec.getPosY(), vec.getPosZ());
				player.mountEntity(sit);
				world.spawnEntityInWorld(sit);
			}
			
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createControls(SubGui gui, LittleStructure structure) {
		
	}

	@Override
	@SideOnly(Side.CLIENT)
	public LittleStructure parseStructure(SubGui gui) {
		return new LittleChair();
	}

}
