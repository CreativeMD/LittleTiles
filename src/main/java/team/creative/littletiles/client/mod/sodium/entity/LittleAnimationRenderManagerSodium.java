package team.creative.littletiles.client.mod.sodium.entity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData.SortState;
import com.mojang.blaze3d.vertex.PoseStack;
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
import team.creative.littletiles.client.mod.sodium.buffer.RenderedBufferSodium;
import team.creative.littletiles.client.mod.sodium.renderer.DefaultChunkRendererExtender;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.entity.LittleAnimationRenderManager;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.mixin.sodium.ChunkBuildBuffersAccessor;
import team.creative.littletiles.mixin.sodium.ChunkBuilderAccessor;
import team.creative.littletiles.mixin.sodium.SodiumWorldRendererAccessor;

@OnlyIn(Dist.CLIENT)
public class LittleAnimationRenderManagerSodium extends LittleAnimationRenderManager {
    
    private GlVertexAttributeBinding[] vertexAttributeBindings;
    private GlVertexFormat format;
    
    public LittleAnimationRenderManagerSodium(LittleAnimationEntity entity) {
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
        for (Tuple<RenderType, RenderedBufferSodium> entry : results.buffers.tuples()) {
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
    
    public void renderChunkLayerSodium(RenderType layer, PoseStack pose, double camx, double camy, double camz, Matrix4fc projectionMatrix, ChunkShaderInterface shader,
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
                
                setHasBlock(layer);
            } else
                LittleTiles.LOGGER.error("Could not upload chunk render data due to invalid buffer");
        }
        VertexBuffer.unbind();
        return true;
    }
    
    static final class CompileResults {
        
        public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
        public final List<BlockEntity> blockEntities = new ArrayList<>();
        
        public final ChunkLayerMap<RenderedBufferSodium> buffers = new ChunkLayerMap<>();
        
        public boolean isEmpty() {
            return buffers.isEmpty() && globalBlockEntities.isEmpty() && blockEntities.isEmpty();
        }
    }
    
    private class RebuildTask {
        
        private ChunkLayerMap<BufferCollection> caches;
        
        private CompileResults compile(float x, float y, float z) {
            CompileResults results = new CompileResults();
            LittleRenderPipelineType.startCompile(LittleAnimationRenderManagerSodium.this);
            
            for (BETiles block : getLevel())
                handleBlockEntity(results, block);
            
            if (caches != null)
                for (Tuple<RenderType, BufferCollection> layer : caches.tuples())
                    results.buffers.put(layer.key, new RenderedBufferSodium(layer.value));
                
            LittleRenderPipelineType.endCompile(LittleAnimationRenderManagerSodium.this);
            return results;
        }
        
        private void handleBlockEntity(CompileResults results, BETiles entity) {
            LittleRenderPipelineType.compileUploaded(LittleAnimationRenderManagerSodium.this.entity.getCenter().chunkOffset.asLong(), entity, x -> getOrCreateBuffers(x));
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
