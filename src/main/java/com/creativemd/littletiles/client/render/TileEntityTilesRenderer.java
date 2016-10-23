package com.creativemd.littletiles.client.render;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TileEntityTilesRenderer extends TileEntitySpecialRenderer<TileEntityLittleTiles> {
	
	@Override
	public void renderTileEntityAt(TileEntityLittleTiles te, double x, double y, double z, float partialTicks, int destroyStage) {
		for (LittleTile	tile : te.getRenderTiles()) {
			LittleTileVec cornerVec = tile.cornerVec;
			//tile.renderTick(x+cornerVec.getPosX(), y+cornerVec.getPosY(), z+cornerVec.getPosZ(), partialTicks);
			tile.renderTick(x, y, z, partialTicks);
		}
		
	}	
}
