package team.creative.littletiles.client.render.mc;

import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;

public interface VertexBufferLittle {
    
    public ChunkLayerUploadManager getManager();
    
    public void setManager(ChunkLayerUploadManager manager);
    
    public int getIndexCount();
    
    public int getVertexBufferId();
    
}
