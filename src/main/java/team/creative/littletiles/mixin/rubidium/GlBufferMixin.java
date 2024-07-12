package team.creative.littletiles.mixin.rubidium;

import org.embeddedt.embeddium.impl.gl.buffer.GlBuffer;
import org.spongepowered.asm.mixin.Mixin;

import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import team.creative.littletiles.client.render.mc.VertexBufferExtender;

@Mixin(GlBuffer.class)
public class GlBufferMixin implements VertexBufferExtender {
    
    @Override
    public int getLastUploadedLength() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getVertexBufferId() {
        return ((GlBuffer) (Object) this).handle();
    }
    
    @Override
    public void setFormat(VertexFormat format) {}
    
    @Override
    public Mode getMode() {
        return null;
    }
    
    @Override
    public void setMode(Mode mode) {}
    
    @Override
    public int getIndexCount() {
        return 0;
    }
    
    @Override
    public void setIndexCount(int count) {}
    
    @Override
    public void setIndexType(IndexType indexType) {}
    
    @Override
    public AutoStorageIndexBuffer getSequentialIndices() {
        return null;
    }
    
    @Override
    public void setSequentialIndices(AutoStorageIndexBuffer indexBuffer) {}
    
    @Override
    public void setLastUploadedLength(int length) {}
    
}
