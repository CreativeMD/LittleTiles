package com.creativemd.littletiles.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.creativemd.creativecore.client.block.BlockRenderHelper;
import com.creativemd.creativecore.client.block.IBlockAccessFake;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.utils.TileList;
import com.jcraft.jorbis.Block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Tuple;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

@SideOnly(Side.CLIENT)
public class RenderingThread extends Thread {
	
	private static CopyOnWriteArrayList<ChunkCoordinates> updateCoords = new CopyOnWriteArrayList<>();
	private static HashMap<ChunkCoordinates, AtomicInteger> chunks = new HashMap<>();
	
	private static World lastWorld;
	
	public static RenderBlocks renderer;
	
	public static ChunkCoordinates getChunkCoords(ChunkCoordinates coord)
	{
		return new ChunkCoordinates(coord.posX >> 4, 0, coord.posZ >> 4);
	}
	
	public static void addCoordToUpdate(World world, ChunkCoordinates coord)
	{
		if(lastWorld != world)
			updateCoords.clear();
		lastWorld = world;
		if(!updateCoords.contains(coord))
		{
			ChunkCoordinates chunk = getChunkCoords(coord);
			AtomicInteger count = chunks.get(chunk);
			if(count == null)
				count = new AtomicInteger(0);
			//System.out.println("Added 1 + " + count + " = " + (count+1));
			count.addAndGet(1);
			//chunks.put(chunk, count == null ? 1 : +1);
			updateCoords.add(coord);
		}
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
				ChunkCoordinates coord = updateCoords.get(0);
				updateCoords.remove(0);
				try{
					
					TileEntity tileEntity = world.getTileEntity(coord.posX, coord.posY, coord.posZ);
					if(tileEntity instanceof TileEntityLittleTiles)
					{
						TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
						ArrayList<LittleBlockVertex> vertexes = new ArrayList<>();
						TileList<LittleTile> tiles = te.getTiles();
						for (int i = 0; i < tiles.size(); i++) {
							LittleTile tile = tiles.get(i);
							if(tile instanceof LittleTileBlock && ((LittleTileBlock) tile).canBlockBeThreaded())
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
												LittleBlockRenderHelper.fake = new IBlockAccessFake(Minecraft.getMinecraft().theWorld);//renderer.blockAccess);
												
												LittleBlockRenderHelper.renderBlocks.blockAccess = LittleBlockRenderHelper.fake;
											}
											
											if(LittleBlockRenderHelper.fake.world != Minecraft.getMinecraft().theWorld)
												LittleBlockRenderHelper.fake.world = Minecraft.getMinecraft().theWorld;
											
											LittleBlockRenderHelper.renderBlocks.clearOverrideBlockTexture();
											LittleBlockRenderHelper.renderBlocks.lockBlockBounds = false;
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
						setLastRenderedTiles(vertexes, te, coord);
						
					}
				}catch(Exception e){
					updateCoords.add(coord);
					//e.printStackTrace();
				}
				
				
				try {
					sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else if(updateCoords.size() == 0){
				chunks.clear();
			}
		}
	}
	
	public synchronized void setLastRenderedTiles(ArrayList<LittleBlockVertex> vertexes, TileEntityLittleTiles te, ChunkCoordinates coord)
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
		//System.out.println("Finished rendering!");
		
		ChunkCoordinates chunk = getChunkCoords(coord);
		AtomicInteger count = chunks.get(chunk);
		//System.out.println("Rendered 1/" + count);
		//chunks.put(chunk, count == null ? 0 : count-1);
		if(count != null)
			count.addAndGet(-1);
		
		if(count == null || count.intValue() <= 0)
		{
			//System.out.println("Force update");
			chunks.remove(chunk);
			te.needsRenderingUpdate = true;
			//System.out.println("Force update!");
		}
		//te.updateRender();
		
		/*if(!te.isRendering)
		{
			te.isRendering = true;
			te.lastRendered = vertexes;
			te.isRendering = false;
		}*/
	}
}
