package com.creativemd.littletiles.common.tileentity;

import net.minecraft.util.ITickable;

public class TileEntityLittleTilesTicking extends TileEntityLittleTiles implements ITickable {
	
	@Override
	public void update() {
		if (!tiles.hasTicking() && !world.isRemote) {
			customTilesUpdate();
			System.out.println("Ticking tileentity which shouldn't " + pos);
			return;
		}
		
		tick();
	}
	
	@Override
	public boolean isTicking() {
		return true;
	}
}
