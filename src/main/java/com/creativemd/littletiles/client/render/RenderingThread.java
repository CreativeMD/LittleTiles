package com.creativemd.littletiles.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.RenderCubeObject;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.utils.TileList;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderingThread extends Thread {
	
	private static ConcurrentLinkedQueue<RenderingData> updateCoords = new ConcurrentLinkedQueue<>();
	private static HashMap<BlockPos, AtomicInteger> chunks = new HashMap<>();
	
	private static World lastWorld;
	
	public static BlockPos getChunkCoords(BlockPos coord)
	{
		return new BlockPos(coord.getX() >> 4, 0, coord.getZ() >> 4);
	}
	
	public static void addCoordToUpdate(TileEntityLittleTiles te, IBlockState state)
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
			updateCoords.add(new RenderingData(te, state, te.getPos()));
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
				RenderingData data = updateCoords.poll();
				try{
					for (int i = 0; i < EnumFacing.VALUES.length; i++) {
						CreativeBakedModel.getBlockQuads(data.state, EnumFacing.VALUES[i], 0, true);
					}
					setRendered(data.te, data.pos);
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
		}
	}
	
	public synchronized void setRendered(TileEntityLittleTiles te, BlockPos coord)
	{
		/*while(te.isRendering)
		{
			try {
				sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		te.cachedCubes = cachedCubes;
		te.cachedQuads = cachedQuads;*/
		te.isRendering = false;
		//System.out.println("Finished rendering!");
		
		BlockPos chunk = getChunkCoords(coord);
		AtomicInteger count = chunks.get(chunk);
		//System.out.println("Rendered 1/" + count);
		//chunks.put(chunk, count == null ? 0 : count-1);
		if(count != null)
			count.addAndGet(-1);
		
		if(count == null || count.intValue() <= 0)
		{
			//System.out.println("Force update");
			chunks.remove(chunk);
			te.forceChunkRenderUpdate = true;
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