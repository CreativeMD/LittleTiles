package team.creative.littletiles.client.render.mc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;

public interface VertexBufferExtender {
    
    public void setFormat(VertexFormat format);
    
    public VertexFormat.Mode getMode();
    
    public void setMode(VertexFormat.Mode mode);
    
    public int getIndexCount();
    
    public void setIndexCount(int count);
    
    public void setIndexType(VertexFormat.IndexType indexType);
    
    public RenderSystem.AutoStorageIndexBuffer getSequentialIndices();
    
    public void setSequentialIndices(RenderSystem.AutoStorageIndexBuffer indexBuffer);
    
    public void setLastUploadedLength(int length);
    
    public int getLastUploadedLength();
    
    public int getVertexBufferId();
}
