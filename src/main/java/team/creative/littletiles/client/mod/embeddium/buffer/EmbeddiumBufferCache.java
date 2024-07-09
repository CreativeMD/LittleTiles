package team.creative.littletiles.client.mod.embeddium.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.embeddedt.embeddium.impl.gl.attribute.GlVertexAttributeFormat;
import org.embeddedt.embeddium.impl.model.quad.properties.ModelQuadFacing;
import org.embeddedt.embeddium.impl.render.chunk.compile.buffers.ChunkModelBuilder;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkMeshAttribute;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.impl.CompactChunkVertex;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.mod.embeddium.pipeline.LittleRenderPipelineEmbeddium;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferDownloader;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;
import team.creative.littletiles.mixin.embeddium.ChunkMeshBufferBuilderAccessor;
import team.creative.littletiles.mixin.embeddium.TranslucentQuadAnalyzerAccessor;

public class EmbeddiumBufferCache implements BufferCache {
    
    private final BufferHolder[] buffers;
    private List<TextureAtlasSprite> textures;
    private int groupCount;
    private boolean invalid;
    
    public EmbeddiumBufferCache(BufferHolder[] buffers, List<TextureAtlasSprite> textures, int groupCount) {
        this.buffers = buffers;
        this.textures = textures;
        this.groupCount = groupCount;
    }
    
    public BufferHolder buffer(ModelQuadFacing facing) {
        return buffers[facing.ordinal()];
    }
    
    public List<TextureAtlasSprite> getUsedTextures() {
        return textures;
    }
    
    @Override
    public BufferCache extract(int index) {
        BufferHolder[] buffers = new BufferHolder[ModelQuadFacing.COUNT];
        for (int i = 0; i < buffers.length; i++)
            if (this.buffers[i] != null)
                buffers[i] = this.buffers[i].extract(index);
        groupCount--;
        return new EmbeddiumBufferCache(buffers, textures, 1);
    }
    
    @Override
    public BufferCache combine(BufferCache cache) {
        if (cache instanceof EmbeddiumBufferCache r) {
            List<TextureAtlasSprite> sprites = new ArrayList<>();
            for (TextureAtlasSprite texture : r.getUsedTextures())
                if (!sprites.contains(texture))
                    sprites.add(texture);
            BufferHolder[] buffers = new BufferHolder[ModelQuadFacing.COUNT];
            for (int i = 0; i < buffers.length; i++)
                buffers[i] = BufferHolder.combine(this.buffers[i], r.buffer(ModelQuadFacing.VALUES[i]));
            return new EmbeddiumBufferCache(buffers, sprites, groupCount + r.groupCount());
        }
        
        if (!(cache instanceof BufferHolder))
            return null;
        
        BufferHolder[] buffers = Arrays.copyOf(this.buffers, ModelQuadFacing.COUNT);
        
        int un = ModelQuadFacing.UNASSIGNED.ordinal();
        buffers[un] = BufferHolder.combine(this.buffers[un], (BufferHolder) cache);
        
        return new EmbeddiumBufferCache(buffers, textures, groupCount + cache.groupCount());
    }
    
    @Override
    public void applyOffset(Vec3 vec) {
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null)
                buffers[i].applyOffset(vec);
    }
    
    @Override
    public boolean isInvalid() {
        return invalid;
    }
    
    @Override
    public void invalidate() {
        invalid = true;
    }
    
    @Override
    public boolean isAvailable() {
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && !buffers[i].isAvailable())
                return false;
        return true;
    }
    
    @Override
    public int lengthToUpload() {
        int length = 0;
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && buffers[i].isAvailable())
                length += buffers[i].lengthToUpload();
        return length;
    }
    
    @Override
    public int lengthToUpload(int facing) {
        if (buffers[facing] != null && buffers[facing].isAvailable())
            return buffers[facing].lengthToUpload();
        return 0;
    }
    
    @Override
    public int groupCount() {
        return groupCount;
    }
    
    @Override
    public void eraseBuffer() {
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null)
                buffers[i].eraseBuffer();
    }
    
    @Override
    public boolean upload(ChunkBufferUploader uploader) {
        for (TextureAtlasSprite texture : textures)
            uploader.addSprite(texture);
        
        if (uploader.hasFacingSupport()) {
            if (uploader.isSorted()) {
                ChunkMeshBufferBuilderAccessor builder = (ChunkMeshBufferBuilderAccessor) ((ChunkModelBuilder) uploader).getVertexBuffer(ModelQuadFacing.UNASSIGNED);
                var centers = ((TranslucentQuadAnalyzerAccessor) builder.getAnalyzer()).getQuadCenters();
                int index = ModelQuadFacing.UNASSIGNED.ordinal();
                if (buffers[index] == null)
                    return false;
                
                ByteBuffer buffer = buffers[0] != null ? buffers[0].byteBuffer() : null;
                if (buffer == null || buffers[index].byteBuffer() == null) { // Recalculate centers
                    ChunkVertexType type = LittleRenderPipelineEmbeddium.getType();
                    ByteBuffer renderData = buffers[index].byteBuffer();
                    var positionAttribute = type.getVertexFormat().getAttribute(ChunkMeshAttribute.POSITION_MATERIAL_MESH);
                    boolean compact = positionAttribute.getFormat() == GlVertexAttributeFormat.UNSIGNED_SHORT.typeId();
                    int stride = type.getVertexFormat().getStride();
                    int strideRemaining = stride - (compact ? GlVertexAttributeFormat.UNSIGNED_SHORT.size() : GlVertexAttributeFormat.FLOAT.size()) * 3;
                    ChunkVertexEncoder.Vertex vertex = new ChunkVertexEncoder.Vertex();
                    var trans = builder.getAnalyzer();
                    
                    while (renderData.hasRemaining()) {
                        if (compact) {
                            vertex.x = CompactChunkVertex.decodePosition(renderData.getShort());
                            vertex.y = CompactChunkVertex.decodePosition(renderData.getShort());
                            vertex.z = CompactChunkVertex.decodePosition(renderData.getShort());
                        } else {
                            vertex.x = renderData.getFloat();
                            vertex.y = renderData.getFloat();
                            vertex.z = renderData.getFloat();
                        }
                        trans.capture(vertex);
                        int newPosition = renderData.position() + strideRemaining;
                        if (newPosition >= renderData.limit())
                            break;
                        renderData.position(newPosition);
                    }
                    renderData.rewind();
                } else {
                    while (buffer.hasRemaining())
                        centers.add(buffer.getFloat());
                    buffer.rewind();
                }
                
                buffers[0] = null;
                
                return buffers[index].upload(index, uploader);
            }
            for (int i = 0; i < buffers.length; i++)
                if (buffers[i] != null && !buffers[i].upload(i, uploader))
                    return false;
            return true;
        }
        
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && !buffers[i].upload(uploader))
                return false;
        return true;
    }
    
    @Override
    public boolean download(ChunkBufferDownloader downloader) {
        if (downloader.hasFacingSupport()) {
            for (int i = 0; i < buffers.length; i++)
                if (buffers[i] != null && !buffers[i].download(downloader.downloaded(i)))
                    return false;
            return true;
        }
        
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && !buffers[i].download(downloader.downloaded()))
                return false;
        return true;
    }
    
}
