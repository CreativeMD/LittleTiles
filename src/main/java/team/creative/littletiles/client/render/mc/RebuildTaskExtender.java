package team.creative.littletiles.client.render.mc;

import java.util.HashMap;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;

public interface RebuildTaskExtender {
    
    public BufferBuilder builder(RenderType layer);
    
    public HashMap<RenderType, ChunkLayerCache> getLayeredCache();
    
    public ChunkLayerCache getOrCreate(RenderType layer);
    
    public void clear();
    
}
