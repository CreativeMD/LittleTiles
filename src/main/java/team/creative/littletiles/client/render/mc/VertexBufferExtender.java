package team.creative.littletiles.client.render.mc;

import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;

public interface VertexBufferExtender {
    
    public ChunkLayerUploadManager getManager();
    
    public void setManager(ChunkLayerUploadManager manager);
    
    public int getLastUploadedLength();
    
    public int getVertexBufferId();
    
}
