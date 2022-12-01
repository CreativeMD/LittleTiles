package team.creative.littletiles.client.render.mc;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;

public interface RebuildTaskExtender {
    
    public BufferBuilder builder(RenderType layer);
    
    public ChunkLayerCache getOrCreate(RenderType layer);
    
}
