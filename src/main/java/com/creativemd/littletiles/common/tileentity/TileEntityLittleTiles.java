package com.creativemd.littletiles.common.tileentity;

import java.util.ArrayList;

import com.creativemd.littletiles.common.utils.LittleTile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityLittleTiles extends TileEntity{
	
	public ArrayList<LittleTile> tiles = new ArrayList<LittleTile>();
	
	/**Used for**/
	public LittleTile loadedTile = null;
	
	/**Used for placing a tile and can be used if a "cable" can connect to a direction*/
	public boolean isSpaceForLittleTile(AxisAlignedBB alignedBB)
	{
		for (int i = 0; i < tiles.size(); i++) {
			if(alignedBB.intersectsWith(tiles.get(i).getBox()))
				return false;
		}
		return true;
	}
	
	public boolean updateLoadedTile(EntityPlayer player)
	{
		//TODO  add some caculations
		
		return loadedTile != null;
	}
	
	@Override
	public void updateEntity()
	{
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).updateEntity();
		}
	}
}
