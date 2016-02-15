package com.creativemd.littletiles.client.render;

import java.util.ArrayList;

import com.creativemd.creativecore.client.block.BlockRenderHelper;
import com.creativemd.creativecore.client.block.IBlockAccessFake;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.IBlockAccess;

public class LittleBlockRenderHelper {
	
	public static LittleThreadedRenderer renderBlocks = new LittleThreadedRenderer(RenderBlocks.getInstance());
	
	public static IBlockAccessFake fake = null;
	
	public static void renderBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer, TileEntityLittleTiles te)
	{
		te.isRendering = true;
		int lightValue = Minecraft.getMinecraft().theWorld.getBlockLightValue(x, y, z);
		boolean needThread = true;
		
		
		
		for (int i = 0; i < te.tiles.size(); i++) {
			LittleTile tile = te.tiles.get(i);
			ArrayList<CubeObject> cubes = tile.getRenderingCubes();
			boolean threaded = false;
			//if()
				//threaded = ((LittleTileBlock) tile).block.getRenderType() == 0;
			//threaded = false;
			if(tile.canBlockBeThreaded())
				needThread = true;
			else
				BlockRenderHelper.renderCubes(world, cubes, x, y, z, block, renderer, null);				
			
		}
		if(needThread)
		{
			if(te.needFullRenderUpdate || lightValue != te.lightValue || te.lastRendered == null)
			{
				te.lightValue = lightValue;
				RenderingThread.renderer = renderer;
				RenderingThread.addCoordToUpdate(te.getWorldObj(), new ChunkCoordinates(x, y, z));
				//System.out.println("Marked!");
				te.needFullRenderUpdate = false;
			}
			if(te.lastRendered != null)
			{
				//aSystem.out.println("Rendered!");
				for (int j = 0; j < te.lastRendered.size(); j++) {
					te.lastRendered.get(j).renderVertex();
				}
			}
		}
		te.lightValue = lightValue;
		te.isRendering = false;
	}
	
}
