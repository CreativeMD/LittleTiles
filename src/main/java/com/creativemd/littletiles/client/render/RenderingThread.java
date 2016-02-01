package com.creativemd.littletiles.client.render;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import com.creativemd.creativecore.client.block.BlockRenderHelper;
import com.creativemd.creativecore.client.block.IBlockAccessFake;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.jcraft.jorbis.Block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

@SideOnly(Side.CLIENT)
public class RenderingThread extends Thread {
	
	private static CopyOnWriteArrayList<ChunkCoordinates> updateCoords = new CopyOnWriteArrayList<>();
	
	private static World lastWorld;
	
	public static RenderBlocks renderer;
	
	public static void addCoordToUpdate(World world, ChunkCoordinates coord)
	{
		if(lastWorld != world)
			updateCoords.clear();
		lastWorld = world;
		if(!updateCoords.contains(coord))
			updateCoords.add(coord);
	}
	
	public static RenderingThread instance = new RenderingThread();
	
	public RenderingThread() {
		start();
	}
	
	public boolean active = true;
	
	@Override
	public void run()
	{
		while(active)
		{
			World world = Minecraft.getMinecraft().theWorld;
			
			if(world != null && updateCoords.size() > 0)
			{
				try{
					ChunkCoordinates coord = updateCoords.get(0);
					TileEntity tileEntity = world.getTileEntity(coord.posX, coord.posY, coord.posZ);
					if(tileEntity instanceof TileEntityLittleTiles)
					{
						TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
						ArrayList<LittleBlockVertex> vertexes = new ArrayList<>();
						for (int i = 0; i < te.tiles.size(); i++) {
							LittleTile tile = te.tiles.get(i);
							if(tile instanceof LittleTileBlock && ((LittleTileBlock) tile).block.getRenderType() == 0)
							{
								
								ArrayList<CubeObject> cubes = tile.getRenderingCubes();
								//tile.isRendering = true;
								for (int j = 0; j < cubes.size(); j++) {
									CubeObject cube = cubes.get(j);
									if(cube.block != null)
									{
										if(cube.meta != -1)
										{
											//if(renderer.blockAccess == null)
												//continue;
												//renderer.blockAccess = Minecraft.getMinecraft().theWorld;
											if(LittleBlockRenderHelper.fake == null)
											{
												LittleBlockRenderHelper.fake = new IBlockAccessFake(renderer.blockAccess);
												LittleBlockRenderHelper.renderBlocks.blockAccess = LittleBlockRenderHelper.fake;
											}
											
											if(LittleBlockRenderHelper.fake.world != renderer.blockAccess)
												LittleBlockRenderHelper.fake.world = renderer.blockAccess;
											
											LittleBlockRenderHelper.renderBlocks.clearOverrideBlockTexture();
											LittleBlockRenderHelper.renderBlocks.setRenderBounds(cube.minX, cube.minY, cube.minZ, cube.maxX, cube.maxY, cube.maxZ);
											LittleBlockRenderHelper.renderBlocks.meta = cube.meta;					
											LittleBlockRenderHelper.fake.overrideMeta = cube.meta;
											LittleBlockRenderHelper.renderBlocks.color = cube.color;
											LittleBlockRenderHelper.renderBlocks.lockBlockBounds = true;
											//LittleBlockRenderHelper.renderBlocks.enableAO = false;
											LittleBlockRenderHelper.renderBlocks.blockVertex = new LittleBlockVertex();
											//LittleBlockRenderHelper.renderBlocks.blockVertex.enableAO = LittleBlockRenderHelper.renderBlocks.enableAO;
											LittleBlockRenderHelper.renderBlocks.renderBlockAllFaces(cube.block, te.xCoord, te.yCoord, te.zCoord);
											vertexes.add(LittleBlockRenderHelper.renderBlocks.blockVertex);
											LittleBlockRenderHelper.renderBlocks.lockBlockBounds = false;
											LittleBlockRenderHelper.renderBlocks.color = ColorUtils.WHITE;
											
										}
									}
								}
								
								//tile.isRendering = false;
								//System.out.println("Rendered " + i + " tile of " + te.tiles.size());
								
							}
						}
						//System.out.println("Done rendering block");
						setLastRenderedTiles(vertexes, te);
						te.updateRender();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				updateCoords.remove(0);
				
				try {
					sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void setLastRenderedTiles(ArrayList<LittleBlockVertex> vertexes, TileEntityLittleTiles te)
	{
		while(te.isRendering)
		{
			try {
				sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		te.lastRendered = vertexes;
		/*if(!te.isRendering)
		{
			te.isRendering = true;
			te.lastRendered = vertexes;
			te.isRendering = false;
		}*/
	}
}
