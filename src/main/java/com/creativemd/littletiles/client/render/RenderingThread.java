package com.creativemd.littletiles.client.render;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;

import com.creativemd.creativecore.client.rendering.RenderCubeLayerCache;
import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedQuad;
import com.creativemd.creativecore.client.rendering.model.CreativeConsumer;
import com.creativemd.creativecore.client.rendering.model.CreativeCubeConsumer;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.render.BlockLayerRenderBuffer.RenderOverlapException;
import com.creativemd.littletiles.client.render.optifine.OptifineVertexBuffer;
import com.creativemd.littletiles.common.blocks.BlockLTTransparentColored;
import com.creativemd.littletiles.common.blocks.BlockLTTransparentColored.EnumType;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.utils.TileList;
import com.google.common.cache.LoadingCache;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.VertexBufferUploader;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shadersmod.client.SVertexBuilder;

@SideOnly(Side.CLIENT)
public class RenderingThread extends Thread {
	
	public ConcurrentLinkedQueue<RenderingData> updateCoords = new ConcurrentLinkedQueue<>();
	public ConcurrentHashMap<RenderChunk, AtomicInteger> chunks = new ConcurrentHashMap<>();
	
	//private static World lastWorld;
	
	public static Minecraft mc = Minecraft.getMinecraft();
	
	public static int nearbyRenderDistance = 32*32;
	
	public static void addCoordToUpdate(TileEntityLittleTiles te, double distanceSq, boolean requiresUpdate)
	{
		RenderingThread renderer = nearbyRenderer;
		if(distanceSq > nearbyRenderDistance)
			renderer = distanceRenderer;
		if(!te.rendering.get())
		{
			te.rendering.set(true);
			if(requiresUpdate)
			{
				//BlockPos chunk = RenderUploader.getRenderChunkPos(te.getPos()); //getChunkCoords(te.getPos());
				RenderChunk chunk = te.lastRenderedChunk;
				if(chunk == null)
				{
					chunk = RenderUploader.getRenderChunkByChunkPosition(RenderUploader.getViewFrustum(), RenderUploader.getRenderChunkPos(te.getPos()));
					te.lastRenderedChunk = chunk;
				}
				synchronized (renderer.chunks){
					AtomicInteger count = renderer.chunks.get(chunk);
					if(count == null)
					{
						count = new AtomicInteger(0);
						renderer.chunks.put(chunk, count);
					}
					count.addAndGet(1);
				}
			}
			renderer.updateCoords.add(new RenderingData(te, LittleTiles.blockTile.getDefaultState(), te.getPos(), requiresUpdate));
		}
	}
	
	public static void addCoordToUpdate(TileEntityLittleTiles te) //, IBlockState state)
	{
		try{
		addCoordToUpdate(te, mc.getRenderViewEntity().getDistanceSq(te.getPos()), true);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static RenderingThread nearbyRenderer = new RenderingThread();
	public static RenderingThread distanceRenderer = new RenderingThread();
	
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
						//if(cubeCache.doesNeedUpdate())
						if(data.te.getWorld() == null || data.te.getTiles().size() == 0)
						{
							throw new RenderingException("Tileentity is not loaded yet!");
						}
						
						
						cubeCache.setCubesByLayer(BlockTile.getRenderingCubes(data.state, data.te, null, layer), layer);
						/*if(cubeCache.getCubesByLayer(BlockRenderLayer.SOLID) != null && cubeCache.getCubesByLayer(BlockRenderLayer.SOLID).size() == 0)
						{
							System.out.println("ISSUE!");
						}*/					
						
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
		            
		            BlockLayerRenderBuffer layerBuffer = new BlockLayerRenderBuffer();
		            if(!layerBuffer.isDrawing())
					{
		            	data.te.renderIndex = LittleChunkDispatcher.currentRenderIndex.get();
						try {
							layerBuffer.setDrawing();
							
							if(consumer.format != LittleTilesClient.getBlockVertexFormat())
								consumer = new CreativeCubeConsumer(LittleTilesClient.getBlockVertexFormat(), mc.getBlockColors());
							
							consumer.setWorld(data.te.getWorld());
							consumer.setBlockPos(pos);
							consumer.getBlockInfo().updateLightMatrix();
							
							//Render vertex buffer
							for (int i = 0; i < BlockRenderLayer.values().length; i++) {
								BlockRenderLayer layer = BlockRenderLayer.values()[i];
								
								ArrayList<RenderCubeObject> cubes = cubeCache.getCubesByLayer(layer);
								VertexBuffer buffer = null;
								if(cubes != null && cubes.size() > 0)
									buffer = layerBuffer.createVertexBuffer(cubes.size());
								
								if(buffer != null)
								{
									consumer.buffer = buffer;
									consumer.layer = layer;
									
									buffer.begin(7, LittleTilesClient.getBlockVertexFormat());
									int chunkX = MathHelper.bucketInt(pos.getX(), 16);
									int chunkY = MathHelper.bucketInt(pos.getY(), 16);
									int chunkZ = MathHelper.bucketInt(pos.getZ(), 16);
									int offsetX = pos.getX() - (chunkX*16);//MathHelper.bucketInt(pos.getX(), 16);
							        int offsetY = pos.getY() - (chunkY*16);//MathHelper.bucketInt(pos.getY(), 16);
							        int offsetZ = pos.getZ() - (chunkZ*16);//MathHelper.bucketInt(pos.getZ(), 16);
									//buffer.setTranslation(16-offsetX, 16-offsetY, 16-offsetZ);
									//System.out.println(offsetX+","+offsetY+","+offsetZ + ",pos=" + pos);
									buffer.setTranslation(offsetX, offsetY, offsetZ);
									//buffer.setTranslation((double)(-chunk.getPosition().getX()), (double)(-chunk.getPosition().getY()), (double)(-chunk.getPosition().getZ()));
									
									for (int j = 0; j < cubes.size(); j++) {
										RenderCubeObject cube = cubes.get(j);
										consumer.cube = cube;
										consumer.setState(cube.getBlockState());
										consumer.getBlockInfo().updateShift(false);
										
										if(FMLClientHandler.instance().hasOptifine() && OptifineVertexBuffer.isShaders())
										{
											IBlockState state = cube.getBlockState();
											if(state.getBlock() instanceof BlockLTTransparentColored && state.getValue(BlockLTTransparentColored.VARIANT) == EnumType.water)
											{
												//System.out.println("simulating water");
												state = Blocks.WATER.getDefaultState();
											}
											SVertexBuilder.pushEntity(state, pos, data.te.getWorld(), buffer);
										}
										
										for (int h = 0; h < EnumFacing.VALUES.length; h++) {
											List<BakedQuad> quads = cube.getQuad(EnumFacing.VALUES[h]);
											if(quads != null && !quads.isEmpty())
											{
												for (int k = 0; k < quads.size(); k++) {
													BakedQuad quad = quads.get(k);
													consumer.quad = (CreativeBakedQuad) quad;
													renderQuad(buffer, quad);
												}
											}
											
										}
										
										if(FMLClientHandler.instance().hasOptifine() && OptifineVertexBuffer.isShaders())
											SVertexBuilder.popEntity(buffer);
									}
									
									consumer.quad = null;
									consumer.cube = null;
									
									if(FMLClientHandler.instance().hasOptifine() && OptifineVertexBuffer.isShaders())
										SVertexBuilder.calcNormalChunkLayer(buffer);
									
							        buffer.finishDrawing();
							        consumer.buffer = null; 
							        
							        layerBuffer.setBufferByLayer(buffer, layer);
								}
							}
							
							layerBuffer.setFinishedDrawing();
							setRendered(data.te, layerBuffer);							
							
						} catch (RenderOverlapException e) {
							updateCoords.add(data);
						} catch (Exception e) {
							//e.printStackTrace();
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
			}else if(world == null && (!updateCoords.isEmpty() || !chunks.isEmpty())){
				updateCoords.clear();
				chunks.clear();
			}else if(world != null && !chunks.isEmpty()){
				/*synchronized (chunks){
					for (Iterator iterator = chunks.keySet().iterator(); iterator.hasNext();) {
						RenderUploader.finishChunkUpdate((RenderChunk) iterator.next());
					}
					chunks.clear();
				}*/
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
	
	private CreativeCubeConsumer consumer = new CreativeCubeConsumer(LittleTilesClient.getBlockVertexFormat(), mc.getBlockColors());
	
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
	
	public synchronized void setRendered(TileEntityLittleTiles te, BlockLayerRenderBuffer buffer)
	{
		te.rendering.set(false);
		
		RenderChunk chunk = te.lastRenderedChunk;
		if(chunk == null)
		{
			chunks.clear();
			te.setBuffer(buffer);
			return ;
		}
		synchronized (chunks){
			AtomicInteger count = chunks.get(chunk);
			if(count != null)
				count.getAndDecrement();
			
			boolean uploadDirectly = te.getBuffer() == null;
			uploadDirectly = false;
			te.setBuffer(buffer);
			
			if(count == null || count.intValue() <= 0)
			{
				chunks.remove(chunk);
				chunk.setNeedsUpdate(true);
			}
		}
	}
	
	public static class RenderingException extends Exception {
		
		public RenderingException(String arg0) {
			super(arg0);
		}
	}
	
	private static class RenderingData {
		
		public TileEntityLittleTiles te;
		public IBlockState state;
		public BlockPos pos;
		
		public boolean requiresUpdate;
		
		public RenderingData(TileEntityLittleTiles te, IBlockState state, BlockPos pos, boolean requiresUpdate) {
			this.te = te;
			this.state = state;
			this.pos = pos;
			this.requiresUpdate = requiresUpdate;
		}
		
		
	}
}