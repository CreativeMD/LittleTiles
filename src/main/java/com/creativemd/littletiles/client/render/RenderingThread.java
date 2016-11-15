package com.creativemd.littletiles.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;

import com.creativemd.creativecore.client.rendering.RenderCubeLayerCache;
import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.CreativeCubeConsumer;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.BlockLayerRenderBuffer.RenderOverlapException;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.utils.TileList;
import com.google.common.cache.LoadingCache;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.VertexBufferUploader;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderingThread extends Thread {
	
	private static ConcurrentLinkedQueue<RenderingData> updateCoords = new ConcurrentLinkedQueue<>();
	private static HashMap<BlockPos, AtomicInteger> chunks = new HashMap<>();
	
	private static World lastWorld;
	
	public static Minecraft mc = Minecraft.getMinecraft();
	
	public static BlockPos getChunkCoords(BlockPos coord)
	{
		return new BlockPos(coord.getX() >> 4, 0, coord.getZ() >> 4);
	}
	
	public static void addCoordToUpdate(TileEntityLittleTiles te) //, IBlockState state)
	{
		if(lastWorld != te.getWorld())
			updateCoords.clear();
		lastWorld = te.getWorld();
		if(!updateCoords.contains(te.getPos()))
		{
			BlockPos chunk = getChunkCoords(te.getPos());
			AtomicInteger count = chunks.get(chunk);
			if(count == null)
				count = new AtomicInteger(0);
			//System.out.println("Added 1 + " + count + " = " + (count+1));
			count.addAndGet(1);
			//chunks.put(chunk, count == null ? 1 : +1);
			updateCoords.add(new RenderingData(te, LittleTiles.blockTile.getDefaultState(), te.getPos()));
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
			World world = mc.theWorld;
			
			if(world != null && updateCoords.size() > 0)
			{
				RenderingData data = updateCoords.poll();
				try{
					BlockPos pos = data.pos;
					RenderCubeLayerCache cubeCache = data.te.getCubeCache();
					
					for (int i = 0; i < BlockRenderLayer.values().length; i++) {
						BlockRenderLayer layer = BlockRenderLayer.values()[i];
						if(cubeCache.doesNeedUpdate())
							cubeCache.setCubesByLayer(BlockTile.getRenderingCubes(data.state, data.te, null, layer), layer);
					
						ArrayList<RenderCubeObject> cubes = cubeCache.getCubesByLayer(layer);
						for (int j = 0; j < cubes.size(); j++) {
							RenderCubeObject cube = cubes.get(j);
							if(cube.doesNeedQuadUpdate)
							{
								IBakedModel blockModel = mc.getBlockRendererDispatcher().getModelForState(cube.getBlockState());
								CubeObject uvCube = cube.offset(cube.getOffset());										
								for (int h = 0; h < EnumFacing.VALUES.length; h++) {
									EnumFacing facing = EnumFacing.VALUES[h];
									if(cube.shouldSideBeRendered(facing))
									{
										if(cube.getQuad(facing) == null)
											cube.setQuad(facing, CreativeBakedModel.getBakedQuad(cube, uvCube, cube.getBlockState(), blockModel, facing, 0));
									}else
										cube.setQuad(facing, null);
								}
								cube.doesNeedQuadUpdate = false;
							}
						}
					}
					
					Entity entity = mc.getRenderViewEntity();
					float x = (float)entity.posX;
		            float y = (float)entity.posY + entity.getEyeHeight();
		            float z = (float)entity.posZ;
		            
		            BlockLayerRenderBuffer layerBuffer = new BlockLayerRenderBuffer();
		            if(!layerBuffer.isDrawing())
					{
						try {
							layerBuffer.setDrawing();
							
							//Render vertex buffer
							for (int i = 0; i < BlockRenderLayer.values().length; i++) {
								BlockRenderLayer layer = BlockRenderLayer.values()[i];
								
								ArrayList<RenderCubeObject> cubes = cubeCache.getCubesByLayer(layer);
								VertexBuffer buffer = null;
								if(cubes != null)
									buffer = layerBuffer.createVertexBuffer(cubes.size());
								
								if(buffer != null)
								{
									consumer.setBlockPos(pos);
									consumer.buffer = buffer;
									consumer.setWorld(data.te.getWorld());
									buffer.begin(7, DefaultVertexFormats.BLOCK);
									//buffer.setTranslation((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()));
									
									for (int j = 0; j < cubes.size(); j++) {
										RenderCubeObject cube = cubes.get(j);
										consumer.setState(cube.getBlockState());
										for (int h = 0; h < EnumFacing.VALUES.length; h++) {
											EnumFacing facing = EnumFacing.VALUES[h];
											BakedQuad quad = cube.getQuad(facing);
											if(quad != null)
											{
												consumer.cube = cube;
												renderQuad(buffer, quad);
												consumer.cube = null;
											}
										}
									}
									
									if (layer == BlockRenderLayer.TRANSLUCENT)
							        {
							        	buffer.sortVertexData(x, y, z);
							            //compiledChunkIn.setState(worldRendererIn.getVertexState());
							        }

							        buffer.finishDrawing();
							        consumer.buffer = null; 
							        
							        layerBuffer.setTemporaryBufferByLayer(buffer, layer);
								}
							}
							
							
							
							setRendered(data.te, data.pos);
							layerBuffer.setFinishedDrawing();
							
							data.te.setBuffer(layerBuffer);
							
						} catch (RenderOverlapException e) {
							updateCoords.add(data);
						} catch (Exception e) {
							e.printStackTrace();
							updateCoords.add(data);
							if(layerBuffer != null)
								layerBuffer.setFinishedDrawing();
						}
					}else
						updateCoords.add(data);
				}catch(Exception e){
					updateCoords.add(data);
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
			try {
				sleep(1);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}
	
	private static LoadingCache<Pair<VertexFormat, VertexFormat>, int[]> formatMaps;
	
	private static LoadingCache<Pair<VertexFormat, VertexFormat>, int[]> getFormatMaps()
	{
		if(formatMaps == null)
			formatMaps = ReflectionHelper.getPrivateValue(LightUtil.class, null, "formatMaps");
		return formatMaps;
	}
	
	private CreativeCubeConsumer consumer = new CreativeCubeConsumer(DefaultVertexFormats.BLOCK, mc.getBlockColors());
	
	private synchronized void renderQuad(VertexBuffer buffer, BakedQuad quad)
	{
		if(quad.hasTintIndex())
        {
			consumer.setQuadTint(quad.getTintIndex());
        }
		consumer.setApplyDiffuseLighting(quad.shouldApplyDiffuseLighting());
        //int[] eMap = mapFormats(consumer.getVertexFormat(), DefaultVertexFormats.ITEM);
        float[] data = new float[4];
        VertexFormat formatFrom = consumer.format;
        VertexFormat formatTo = quad.getFormat();
        int countFrom = formatFrom.getElementCount();
        int countTo = formatTo.getElementCount();
        int[] eMap = getFormatMaps().getUnchecked(Pair.of(formatFrom, formatTo));
        for(int v = 0; v < 4; v++)
        {
            for(int e = 0; e < countFrom; e++)
            {
                if(eMap[e] != countTo)
                {
                    LightUtil.unpack(quad.getVertexData(), data, quad.getFormat(), v, eMap[e]);
                    
                    consumer.put(e, data);
                }
                else
                {
                    consumer.put(e);
                }
            }
        }
	}
	
	public synchronized void setRendered(TileEntityLittleTiles te, BlockPos coord)
	{
		te.isRendering = false;
		
		BlockPos chunk = getChunkCoords(coord);
		AtomicInteger count = chunks.get(chunk);
		if(count != null)
			count.addAndGet(-1);
		
		if(count == null || count.intValue() <= 0)
		{
			chunks.remove(chunk);
			//te.forceChunkRenderUpdate = true;
		}
	}
	
	private static class RenderingData {
		
		public TileEntityLittleTiles te;
		public IBlockState state;
		public BlockPos pos;
		
		public RenderingData(TileEntityLittleTiles te, IBlockState state, BlockPos pos) {
			this.te = te;
			this.state = state;
			this.pos = pos;
		}
		
		
	}
}