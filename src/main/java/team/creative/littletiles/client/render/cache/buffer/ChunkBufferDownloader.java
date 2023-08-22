package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

public interface ChunkBufferDownloader {
    
    public ByteBuffer downloaded();
    
    public boolean hasFacingSupport();
    
    public ByteBuffer downloaded(int facing);
    
    public static class SimpleChunkBufferDownloader implements ChunkBufferDownloader {
        
        public ByteBuffer buffer;
        
        @Override
        public ByteBuffer downloaded() {
            return buffer;
        }
        
        @Override
        public boolean hasFacingSupport() {
            return false;
        }
        
        @Override
        public ByteBuffer downloaded(int facing) {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
