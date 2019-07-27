package com.creativemd.littletiles.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

public class ItemModelCache {
	
	private static class ItemModelCacheKey {
		
		public ItemStack stack;
		public EnumFacing facing;
		
		public ItemModelCacheKey(ItemStack stack, EnumFacing facing) {
			this.stack = stack;
			this.facing = facing;
		}
		
		@Override
		public boolean equals(Object object) {
			if (object instanceof ItemModelCacheKey)
				return ((ItemModelCacheKey) object).stack.equals(stack) && ((ItemModelCacheKey) object).facing == facing;
			return false;
		}
		
		@Override
		public int hashCode() {
			return stack.hashCode() + facing.hashCode();
		}
		
	}
	
	private List<BakedQuad> quads;
	private long lastUsed;
	
	public ItemModelCache(List<BakedQuad> quads) {
		this.quads = quads;
		lastUsed = System.currentTimeMillis();
	}
	
	public boolean expired() {
		return System.currentTimeMillis() - lastUsed >= timeToExpire;
	}
	
	public List<BakedQuad> getQuads() {
		lastUsed = System.currentTimeMillis();
		return quads;
	}
	
	private static HashMap<ItemModelCacheKey, ItemModelCache> caches = new HashMap<>();
	
	public static void cacheModel(ItemStack stack, EnumFacing facing, List<BakedQuad> quads) {
		cacheModel(new ItemModelCacheKey(stack, facing), quads);
	}
	
	private static void cacheModel(ItemModelCacheKey key, List<BakedQuad> quads) {
		synchronized (caches) {
			caches.put(key, new ItemModelCache(quads));
		}
	}
	
	public static List<BakedQuad> requestCache(ItemStack stack, EnumFacing facing) {
		synchronized (caches) {
			ItemModelCacheKey key = new ItemModelCacheKey(stack, facing);
			ItemModelCache cache = caches.get(key);
			if (cache != null)
				return cache.getQuads();
			thread.items.add(key);
			return null;
		}
	}
	
	private static int ticker = 0;
	public static int timeToExpire = 30000;
	private static int timeToCheck = timeToExpire / 50;
	
	public static void tick(World world) {
		if (world == null) {
			caches.clear();
			return;
		}
		
		ticker++;
		if (ticker >= timeToCheck) {
			for (Iterator<ItemModelCache> iterator = caches.values().iterator(); iterator.hasNext();) {
				ItemModelCache cache = iterator.next();
				if (cache.expired())
					iterator.remove();
			}
			ticker = 0;
		}
	}
	
	public static void unload() {
		caches.clear();
	}
	
	public static RenderingThreadItem thread = new RenderingThreadItem();
	private static Minecraft mc = Minecraft.getMinecraft();
	
	public static class RenderingThreadItem extends Thread {
		
		public ConcurrentLinkedQueue<ItemModelCacheKey> items = new ConcurrentLinkedQueue<>();
		
		public RenderingThreadItem() {
			start();
		}
		
		@Override
		public void run() {
			IBlockAccess world = mc.world;
			
			if (world != null && !items.isEmpty()) {
				ItemModelCacheKey data = items.poll();
				ICreativeRendered renderer = (ICreativeRendered) data.stack.getItem();
				
				BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
				cacheModel(data, CreativeBakedModel.getBlockQuads(renderer.getRenderingCubes(null, null, data.stack), new ArrayList<>(), null, data.facing, null, layer, null, null, 0, data.stack, true));
			} else if (world == null)
				items.clear();
		}
		
	}
}
