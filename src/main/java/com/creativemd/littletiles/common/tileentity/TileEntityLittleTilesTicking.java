package com.creativemd.littletiles.common.tileentity;

import java.util.Iterator;

import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.util.ITickable;

public class TileEntityLittleTilesTicking extends TileEntityLittleTiles implements ITickable {
	
	@Override
	public void update() {
		if (getUpdateTiles().isEmpty() && !world.isRemote) {
			customTilesUpdate();
			System.out.println("Ticking tileentity which shouldn't " + pos);
			return;
		}
		
		for (Iterator iterator = getUpdateTiles().iterator(); iterator.hasNext();) {
			LittleTile tile = (LittleTile) iterator.next();
			tile.updateEntity();
		}
	}
	
	@Override
	public boolean isTicking() {
		return true;
	}
}
