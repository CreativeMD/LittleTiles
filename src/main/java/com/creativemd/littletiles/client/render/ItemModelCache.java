package com.creativemd.littletiles.client.render;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class ItemModelCache {
	
	private static class ItemModelCacheKey {
		
		public ItemStack stack;
		public EnumFacing facing;
		
		public ItemModelCacheKey(ItemStack stack, EnumFacing facing) {
			this.stack = stack;
			this.facing = facing;
		}
		
		@Override
		public boolean equals(Object object)
		{
			if(object instanceof ItemModelCacheKey)
				return ((ItemModelCacheKey) object).stack.equals(stack) && ((ItemModelCacheKey) object).facing == facing;
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return stack.hashCode() + facing.hashCode();
		}
		
	}
	
	private List<BakedQuad> quads;
	private long lastUsed;
	
	public ItemModelCache(List<BakedQuad> quads) {
		this.quads = quads;
		lastUsed = System.currentTimeMillis();
	}
	
	public boolean expired()
	{
		return System.currentTimeMillis() - lastUsed >= timeToExpire;
	}
	
	public List<BakedQuad> getQuads()
	{
		lastUsed = System.currentTimeMillis();
		return quads;
	}	

	private static HashMap<ItemModelCacheKey, ItemModelCache> caches = new HashMap<>();
	
	public static void cacheModel(ItemStack stack, EnumFacing facing, List<BakedQuad> quads)
	{
		caches.put(new ItemModelCacheKey(stack, facing), new ItemModelCache(quads));
	}
	
	public static List<BakedQuad> getCache(ItemStack stack, EnumFacing facing)
	{
		ItemModelCache cache = caches.get(new ItemModelCacheKey(stack, facing));
		if(cache != null)
			return cache.getQuads();
		return null;
	}
	
	
	private static int ticker = 0;
	public static int timeToExpire = 30000;
	private static int timeToCheck = timeToExpire/50;
	
	public static void tick()
	{
		ticker++;
		if(ticker >= timeToCheck)
		{
			for (Iterator<ItemModelCache> iterator = caches.values().iterator(); iterator.hasNext();) {
				ItemModelCache cache = iterator.next();
				if(cache.expired())
					iterator.remove();
			}
			ticker = 0;
		}
	}
}
