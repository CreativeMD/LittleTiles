package team.creative.littletiles.client.render.cache;

import java.nio.ByteBuffer;

import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.BlockRenderLayer;
import team.creative.littletiles.client.render.block.BERenderManager;

public class BlockRenderCache {
    
    public final BERenderManager manager;
    public final int length;
    public final int vertexCount;
    
    public ByteBuffer buffer;
    public BufferLink link;
    
    public BlockRenderCache(BERenderManager manager, int layer, IRenderDataCache cache, ByteBuffer buffer) {
        this.manager = manager;
        this.length = cache.length();
        this.vertexCount = cache.vertexCount();
        this.buffer = buffer;
        if (layer != BlockRenderLayer.TRANSLUCENT.ordinal()) {
            this.link = new BufferLink(buffer, length, vertexCount);
            manager.getBufferCache().setUploaded(link, layer);
        }
    }
    
    public void fill(BufferBuilder builder) {
        if (buffer == null)
            return;
        int index = BufferBuilderUtils.getBufferSizeByte(builder);
        BufferBuilderUtils.addBuffer(builder, buffer, length, vertexCount);
        if (link != null)
            this.link.merged(index);
        buffer = null;
    }
    
    public void fill(ByteBuffer toUpload) {
        buffer.position(0);
        buffer.limit(length);
        if (link != null)
            this.link.merged(toUpload.position());
        toUpload.put(buffer);
        buffer = null;
    }
}
