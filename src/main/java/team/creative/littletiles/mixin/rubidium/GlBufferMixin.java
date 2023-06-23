package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;

import me.jellysquid.mods.sodium.client.gl.buffer.GlBuffer;
import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;

@Mixin(GlBuffer.class)
public class GlBufferMixin implements VertexBufferExtender {
    
    @Override
    public ChunkLayerUploadManager getManager() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setManager(ChunkLayerUploadManager manager) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getLastUploadedLength() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getVertexBufferId() {
        return ((GlBuffer) (Object) this).handle();
    }
    
}
