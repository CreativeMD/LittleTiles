package team.creative.littletiles.client.mod.embeddium.entity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.embeddedt.embeddium.impl.gl.attribute.GlVertexAttributeBinding;
import org.embeddedt.embeddium.impl.gl.attribute.GlVertexFormat;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderInterface;
import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData.SortState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.embeddium.buffer.RenderedBufferRubidium;
import team.creative.littletiles.client.mod.embeddium.renderer.DefaultChunkRendererExtender;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.entity.LittleAnimationRenderManager;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;

@OnlyIn(Dist.CLIENT)
public class LittleAnimationRenderManagerEmbeddium extends LittleAnimationRenderManager {
    
    private GlVertexAttributeBinding[] vertexAttributeBindings;
    private GlVertexFormat format;
    
    public LittleAnimationRenderManagerEmbeddium(LittleAnimationEntity entity) {
        super(entity);
    }
    
    public void prepare(GlVertexAttributeBinding[] vertexAttributeBindings, GlVertexFormat format) {
        this.vertexAttributeBindings = vertexAttributeBindings;
        this.format = format;
    }
    
    @Override
    public void compileSections(Camera camera) {
        if (!needsUpdate || vertexAttributeBindings == null)
            return;
        
        needsUpdate = false;
        hasBlocks.clear();
        renderableBlockEntities.clear();
        RebuildTask rebuild = new RebuildTask();
        Vec3 cam = camera.getPosition();
        CompileResults results = rebuild.compile((float) cam.x, (float) cam.y, (float) cam.z);
        globalBlockEntities.clear();
        globalBlockEntities.addAll(results.globalBlockEntities);
        renderableBlockEntities = results.blockEntities;
        prepareUpload();
        for (Tuple<RenderType, RenderedBufferRubidium> entry : results.buffers.tuples()) {
            VertexBuffer buffer = getVertexBuffer(entry.key);
            if (!buffer.isInvalid() && buffer instanceof VertexBufferExtender ex) {
                buffer.bind();
                
                ex.setFormat(null);
                int length = entry.value.byteBuffer().limit();
                uploadVertexBuffer(ex, entry.value.byteBuffer());
                ex.setMode(VertexFormat.Mode.QUADS);
                ex.setIndexCount(ex.getMode().indexCount(length / format.getStride()));
                ex.setSequentialIndices(this.uploadIndexBuffer(ex));
                ex.setIndexType(IndexType.INT);
                ex.setLastUploadedLength(length);
                
                BufferCollection buffers = rebuild.getBuffers(entry.key);
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
        for (GlVertexAttributeBinding attrib : vertexAttributeBindings) {
            if (attrib.isIntType())
                GL30C.glVertexAttribIPointer(attrib.getIndex(), attrib.getCount(), attrib.getFormat(), attrib.getStride(), attrib.getPointer());
            else
                GL20C.glVertexAttribPointer(attrib.getIndex(), attrib.getCount(), attrib.getFormat(), attrib.isNormalized(), attrib.getStride(), attrib.getPointer());
            GL20C.glEnableVertexAttribArray(attrib.getIndex());
        }
        RenderSystem.glBufferData(GL20C.GL_ARRAY_BUFFER, byteBuffer, /*this.usage.id*/ 35044);
    }
    
    @Override
    public boolean isSmall() {
        return true;
    }
    
    public void renderChunkLayerEmbeddium(RenderType layer, PoseStack pose, double camx, double camy, double camz, Matrix4fc projectionMatrix, ChunkShaderInterface shader,
            CameraTransform camera) {
        if (hasBlocks.contains(layer)) {
            VertexBuffer vertexbuffer = buffers.get(layer);
            if (vertexbuffer == null)
                return;
            DefaultChunkRendererExtender.setRenderRegionOffset(shader, entity.getCenter().chunkOrigin, camera);
            vertexbuffer.bind();
            vertexbuffer.draw();
        }
    }
    
    @Override
    public SortState getTransparencyState() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public VertexSorting createVertexSorting(double x, double y, double z) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void resortTransparency(RenderType layer, double x, double y, double z) {
        throw new UnsupportedOperationException();
    }
    
    static final class CompileResults {
        
        public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
        public final List<BlockEntity> blockEntities = new ArrayList<>();
        
        public final ChunkLayerMap<RenderedBufferRubidium> buffers = new ChunkLayerMap<>();
        
        public boolean isEmpty() {
            return buffers.isEmpty() && globalBlockEntities.isEmpty() && blockEntities.isEmpty();
        }
    }
    
    private class RebuildTask {
        
        private ChunkLayerMap<BufferCollection> caches;
        
        private CompileResults compile(float x, float y, float z) {
            CompileResults results = new CompileResults();
            LittleRenderPipelineType.startCompile(LittleAnimationRenderManagerEmbeddium.this);
            
            for (BETiles block : getLevel())
                handleBlockEntity(results, block);
            
            if (caches != null)
                for (Tuple<RenderType, BufferCollection> layer : caches.tuples())
                    results.buffers.put(layer.key, new RenderedBufferRubidium(layer.value));
                
            LittleRenderPipelineType.endCompile(LittleAnimationRenderManagerEmbeddium.this);
            return results;
        }
        
        private void handleBlockEntity(CompileResults results, BETiles entity) {
            LittleRenderPipelineType.compileUploaded(LittleAnimationRenderManagerEmbeddium.this.entity.getCenter().chunkOffset.asLong(), entity, x -> getOrCreateBuffers(x));
            BlockEntityRenderer blockentityrenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
            if (blockentityrenderer != null)
                if (blockentityrenderer.shouldRenderOffScreen(entity))
                    results.globalBlockEntities.add(entity);
                else
                    results.blockEntities.add(entity); //FORGE: Fix MC-112730
        }
        
        public BufferCollection getBuffers(RenderType layer) {
            if (caches == null)
                return null;
            return caches.get(layer);
        }
        
        public BufferCollection getOrCreateBuffers(RenderType layer) {
            if (caches == null)
                caches = new ChunkLayerMap<>();
            BufferCollection cache = caches.get(layer);
            if (cache == null)
                caches.put(layer, cache = new BufferCollection());
            return cache;
        }
        
    }
    
}
