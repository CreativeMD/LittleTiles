package com.creativemd.littletiles.client.render.world;

import java.util.LinkedHashMap;

import com.creativemd.creativecore.client.rendering.IRenderChunkSupplier;
import com.creativemd.littletiles.client.render.entity.LittleRenderChunk;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleRenderChunkSuppilier implements IRenderChunkSupplier {
	
	@SideOnly(Side.CLIENT)
	public LinkedHashMap<BlockPos, LittleRenderChunk> renderChunks = new LinkedHashMap<>();
	
	@SideOnly(Side.CLIENT)
	public void unloadRenderCache() {
		if (renderChunks == null)
			return;
		for (LittleRenderChunk chunk : renderChunks.values()) {
			chunk.unload();
		}
		renderChunks.clear();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public LittleRenderChunk getRenderChunk(World world, BlockPos pos) {
		synchronized (renderChunks) {
			BlockPos renderChunkPos = RenderUtils.getRenderChunkPos(pos);
			LittleRenderChunk chunk = renderChunks.get(renderChunkPos);
			if (chunk == null) {
				chunk = new LittleRenderChunk(renderChunkPos);
				renderChunks.put(renderChunkPos, chunk);
			}
			return chunk;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		unloadRenderCache();
	}
}
