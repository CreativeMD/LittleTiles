package com.creativemd.littletiles.common.tile.math.box;

import java.util.HashMap;
import java.util.Map.Entry;

import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;

public class LittleVolumes {
	
	public LittleGridContext context;
	private HashMap<LittleVolume, Double> volumes = new HashMap<>();
	
	public LittleVolumes(LittleGridContext context) {
		this.context = context;
	}
	
	public void convertTo(LittleGridContext context) {
		double ratio = (double) context.size / this.context.size;
		for (Entry<LittleVolume, Double> entry : volumes.entrySet()) {
			entry.setValue(entry.getValue() * ratio);
		}
	}
	
	public void ensureContext(LittleGridContext context) {
		if (this.context.size < context.size)
			convertTo(context);
	}
	
	public void addPreviews(LittlePreviews previews) {
		ensureContext(previews.getContext());
		
		for (LittlePreview preview : previews) {
			addPreviewDirectly(context, preview);
		}
	}
	
	public void addPreview(LittleGridContext context, LittlePreview preview) {
		ensureContext(context);
		
		addPreviewDirectly(context, preview);
	}
	
	private void addPreviewDirectly(LittleGridContext context, LittlePreview preview) {
		double volume = preview.getVolume();
		if (context.size < this.context.size)
			volume *= this.context.size / context.size;
		
		LittleVolume type = new LittleVolume(preview.getBlock(), preview.getMeta());
		Double exist = volumes.get(type);
		if (exist == null)
			exist = volume;
		else
			exist += volume;
		
		volumes.put(type, exist);
	}
	
	@Override
	public int hashCode() {
		return volumes.size();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LittleVolumes) {
			LittleGridContext beforeThis = context;
			ensureContext(((LittleVolumes) obj).context);
			
			LittleGridContext beforeTheirs = ((LittleVolumes) obj).context;
			((LittleVolumes) obj).ensureContext(context);
			
			boolean result = ((LittleVolumes) obj).volumes.equals(this.volumes);
			
			if (beforeThis != context)
				convertTo(beforeThis);
			
			if (beforeTheirs != ((LittleVolumes) obj).context)
				((LittleVolumes) obj).convertTo(beforeTheirs);
			
			return result;
		}
		return false;
	}
	
	public static class LittleVolume {
		
		public final Block block;
		public final int meta;
		
		public LittleVolume(Block block, int meta) {
			this.block = block;
			this.meta = meta;
		}
		
		@Override
		public int hashCode() {
			return block.hashCode() + meta;
		}
		
		@Override
		public boolean equals(Object object) {
			if (object instanceof LittleVolume)
				return ((LittleVolume) object).block == block && ((LittleVolume) object).meta == meta;
			return false;
		}
		
	}
	
}
