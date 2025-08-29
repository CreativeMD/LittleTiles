package team.creative.littletiles.client.render.cache.pipeline;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.cache.build.RenderingThread.RenderingException;

public abstract class LittleRenderPipeline {
    
    public static final Minecraft MC = Minecraft.getInstance();
    
    public abstract void buildCache(PoseStack pose, ChunkLayerMap<BufferCache> buffers, RenderingBlockContext context, VertexFormat format,
            SingletonList<BakedQuad> bakedQuadWrapper) throws RenderingException;
    
    public abstract void reload();
    
    public abstract void release();
    
}
