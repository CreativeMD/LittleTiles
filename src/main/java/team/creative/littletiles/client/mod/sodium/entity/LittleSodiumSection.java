package team.creative.littletiles.client.mod.sodium.entity;

import java.nio.ByteBuffer;
import java.util.Set;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData.SortState;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.sodium.buffer.RenderedBufferSodium;
import team.creative.littletiles.client.mod.sodium.renderer.DefaultChunkRendererExtender;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;
import team.creative.littletiles.client.render.level.RenderAdditional.SectionAdditional;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
import team.creative.littletiles.mixin.sodium.ChunkBuildBuffersAccessor;
import team.creative.littletiles.mixin.sodium.ChunkBuilderAccessor;
import team.creative.littletiles.mixin.sodium.SodiumWorldRendererAccessor;

public class LittleSodiumSection implements RenderChunkExtender {
    
    private final LittleAnimationRenderManagerSodium parent;
    private final SectionPos pos;
    private final BlockPos origin;
    protected final ChunkLayerMap<VertexBuffer> buffers = new ChunkLayerMap<>();
    
    private SectionAdditional additional;
    public ChunkLayerMap<BufferCollection> lastUploaded;
    private volatile int queued;
    
    public LittleSodiumSection(LittleAnimationRenderManagerSodium parent, long pos) {
        this.parent = parent;
        this.pos = SectionPos.of(pos);
        this.origin = this.pos.origin();
    }
    
    @Override
    public SectionAdditional getAdditional() {
        return additional;
    }
    
    @Override
    public void setAdditional(SectionAdditional uploader) {
        this.additional = uploader;
    }
    
    @Override
    public ChunkLayerMap<BufferCollection> getLastUploaded() {
        return lastUploaded;
    }
    
    @Override
    public void setLastUploaded(ChunkLayerMap<BufferCollection> uploaded) {
        this.lastUploaded = uploaded;
    }
    
    @Override
    public int getQueued() {
        return queued;
    }
    
    @Override
    public void setQueued(int queued) {
        this.queued = queued;
    }
    
    @Override
    public VertexBuffer getVertexBuffer(RenderType layer) {
        VertexBuffer buffer = this.buffers.get(layer);
        if (buffer == null)
            this.buffers.put(layer, buffer = new VertexBuffer(VertexBuffer.Usage.STATIC));
        return buffer;
    }
    
    @Override
    public void markReadyForUpdate(boolean playerChanged) {
        parent.needsUpdate = true;
    }
    
    public void upload(CompiledSodiumSection compiled, Set<RenderType> hasBlocks) {
        prepareUpload();
        for (Tuple<RenderType, RenderedBufferSodium> entry : compiled.buffers.tuples()) {
            VertexBuffer buffer = getVertexBuffer(entry.key);
            if (!buffer.isInvalid() && buffer instanceof VertexBufferExtender ex) {
                buffer.bind();
                
                ex.setFormat(null);
                int length = entry.value.byteBuffer().limit();
                uploadVertexBuffer(ex, entry.value.byteBuffer());
                ex.setMode(VertexFormat.Mode.QUADS);
                ex.setIndexCount(ex.getMode().indexCount(length / parent.getFormat().getStride()));
                ex.setSequentialIndices(this.uploadIndexBuffer(ex));
                ex.setIndexType(IndexType.INT);
                ex.setLastUploadedLength(length);
                
                BufferCollection buffers = compiled.getBuffers(entry.key);
                if (buffers != null)
                    uploaded(entry.key, buffers);
                
                try {
                    entry.value.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                hasBlocks.add(entry.key);
            } else
                LittleTiles.LOGGER.error("Could not upload chunk render data due to invalid buffer");
        }
        
        VertexBuffer.unbind();
    }
    
    @Nullable
    private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(VertexBufferExtender ex) {
        RenderSystem.AutoStorageIndexBuffer buffer = RenderSystem.getSequentialBuffer(ex.getMode());
        if (buffer != ex.getSequentialIndices() || !buffer.hasStorage(ex.getIndexCount()))
            buffer.bind(ex.getIndexCount());
        
        return buffer;
    }
    
    private void uploadVertexBuffer(VertexBufferExtender buffer, ByteBuffer byteBuffer) {
        GlStateManager._glBindBuffer(GL20C.GL_ARRAY_BUFFER, buffer.getVertexBufferId());
        for (GlVertexAttributeBinding attrib : parent.getBindings()) {
            if (attrib.isIntType())
                GL30C.glVertexAttribIPointer(attrib.getIndex(), attrib.getCount(), attrib.getFormat(), attrib.getStride(), attrib.getPointer());
            else
                GL20C.glVertexAttribPointer(attrib.getIndex(), attrib.getCount(), attrib.getFormat(), attrib.isNormalized(), attrib.getStride(), attrib.getPointer());
            GL20C.glEnableVertexAttribArray(attrib.getIndex());
        }
        RenderSystem.glBufferData(GL20C.GL_ARRAY_BUFFER, byteBuffer, /*this.usage.id*/ 35044);
    }
    
    @Override
    public boolean appendRenderData(Iterable<? extends LayeredBufferCache> blocks) {
        RenderSectionManager manager = ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager();
        ChunkBuilderAccessor chunkBuilder = (ChunkBuilderAccessor) manager.getBuilder();
        GlVertexFormat format = ((ChunkBuildBuffersAccessor) chunkBuilder.getLocalContext().buffers).getVertexType().getVertexFormat();
        
        for (RenderType layer : RenderType.CHUNK_BUFFER_LAYERS) {
            
            int size = 0;
            for (LayeredBufferCache data : blocks)
                size += data.length(layer);
            
            if (size == 0)
                continue;
            
            VertexBuffer uploadBuffer = getVertexBuffer(layer);
            
            if (uploadBuffer == null)
                return false;
            
            ByteBuffer vanillaBuffer = null;
            if (!isEmpty(layer))
                vanillaBuffer = downloadUploadedData((VertexBufferExtender) uploadBuffer, 0, ((VertexBufferExtender) uploadBuffer).getLastUploadedLength());
            ByteBufferBuilder buffer = new ByteBufferBuilder(((vanillaBuffer != null ? vanillaBuffer.limit() : 0) + size + DefaultVertexFormat.BLOCK.getVertexSize()) / 6); // dividing by 6 is risky and could potentially cause issues
            
            BufferBuilder builder = new BufferBuilder(buffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            if (vanillaBuffer != null)
                ((ChunkBufferUploader) builder).upload(vanillaBuffer);
            
            for (LayeredBufferCache data : blocks) {
                var layerData = data.get(layer);
                if (layerData != null)
                    layerData.upload((ChunkBufferUploader) builder);
            }
            
            var mesh = builder.build();
            
            if (!uploadBuffer.isInvalid() && uploadBuffer instanceof VertexBufferExtender ex && mesh != null) {
                uploadBuffer.bind();
                
                ex.setFormat(null);
                
                var byteBuffer = mesh.vertexBuffer();
                int length = byteBuffer.limit();
                
                uploadVertexBuffer(ex, byteBuffer);
                ex.setMode(VertexFormat.Mode.QUADS);
                ex.setIndexCount(ex.getMode().indexCount(length / format.getStride()));
                ex.setSequentialIndices(this.uploadIndexBuffer(ex));
                ex.setIndexType(IndexType.INT);
                ex.setLastUploadedLength(length);
            } else
                LittleTiles.LOGGER.error("Could not upload chunk render data due to invalid buffer");
        }
        VertexBuffer.unbind();
        return true;
    }
    
    public void renderChunkLayerSodium(RenderType layer, ChunkShaderInterface shader, CameraTransform camera) {
        VertexBuffer vertexbuffer = buffers.get(layer);
        if (vertexbuffer == null)
            return;
        DefaultChunkRendererExtender.setRenderRegionOffset(shader, origin, camera);
        vertexbuffer.bind();
        vertexbuffer.draw();
    }
    
    @Override
    public void setTransparencyState(SortState state) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isEmpty(RenderType layer) {
        return !buffers.containsKey(layer);
    }
    
    @Override
    public void setHasBlock(RenderType layer) {
        parent.setHasBlock(layer);
    }
    
    @Override
    public SortState getTransparencyState() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public VertexSorting createVertexSorting(double x, double y, double z) {
        throw new UnsupportedOperationException();
    }
    
    public void unload() {
        this.buffers.forEach(VertexBuffer::close);
    }
    
}
