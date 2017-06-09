package com.creativemd.littletiles.common.world;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class WorldInteractor {
	
	public static List<TileEntity> createTileEntity()
	{
		return new WorldChunkedTileEntityList();
	}
	
	public static void removeTileEntities(World world)
	{
		List<Chunk> chunks;
		try {
			chunks = (List<Chunk>) chunksToBeRemoved.get(world);
			if(chunks.isEmpty())
				return ;
			
			for (Iterator iterator = chunks.iterator(); iterator.hasNext();) {
				Chunk chunk = (Chunk) iterator.next();
				((WorldChunkedTileEntityList) world.loadedTileEntityList).removeChunk(world, chunk, true);
				((WorldChunkedTileEntityList) world.tickableTileEntities).removeChunk(world, chunk, false);
			}
			
			chunks.clear();
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static Field chunksToBeRemoved = ReflectionHelper.findField(World.class, "tileEntitiesFromChunkToBeRemoved");
	
	public static void addChunkToBeRemoved(World world, Chunk chunk)
	{
		List<Chunk> chunks;
		try {
			chunks = (List<Chunk>) chunksToBeRemoved.get(world);
			chunks.add(chunk);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
