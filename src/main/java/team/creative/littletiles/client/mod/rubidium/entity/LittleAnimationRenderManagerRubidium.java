package team.creative.littletiles.client.mod.rubidium.entity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.client.render.chunk.LocalSectionIndex;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.viewport.CameraTransform;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.rubidium.RubidiumInteractor;
import team.creative.littletiles.client.mod.rubidium.buffer.RenderedBufferRubidium;
import team.creative.littletiles.client.mod.rubidium.renderer.DefaultChunkRendererExtender;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.entity.LittleAnimationRenderManager;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;

@OnlyIn(Dist.CLIENT)
public class LittleAnimationRenderManagerRubidium extends LittleAnimationRenderManager {
    
    private GlVertexAttributeBinding[] vertexAttributeBindings;
    private GlVertexFormat format;
    
    public LittleAnimationRenderManagerRubidium(LittleAnimationEntity entity) {
        super(entity);
    }
    
    public void prepare(GlVertexAttributeBinding[] vertexAttributeBindings, GlVertexFormat format) {
        this.vertexAttributeBindings = vertexAttributeBindings;
        this.format = format;
    }
    
    @Override
    public void compileChunks(Camera camera) {
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
    
    @Unique
    private void uploadVertexBuffer(VertexBufferExtender buffer, ByteBuffer byteBuffer) {
        GlStateManager._glBindBuffer(34962, buffer.getVertexBufferId());
        for (GlVertexAttributeBinding attrib : vertexAttributeBindings) {
            if (attrib.isIntType())
                GL30C.glVertexAttribIPointer(attrib.getIndex(), attrib.getCount(), attrib.getFormat(), attrib.getStride(), attrib.getPointer());
            else
                GL20C.glVertexAttribPointer(attrib.getIndex(), attrib.getCount(), attrib.getFormat(), attrib.isNormalized(), attrib.getStride(), attrib.getPointer());
            GL20C.glEnableVertexAttribArray(attrib.getIndex());
        }
        RenderSystem.glBufferData(34962, byteBuffer, /*this.usage.id*/ 35044);
    }
    
    @Override
    public int sectionIndex() {
        int rX = entity.getCenter().chunkOffset.getX() & (RenderRegion.REGION_WIDTH - 1);
        int rY = entity.getCenter().chunkOffset.getY() & (RenderRegion.REGION_HEIGHT - 1);
        int rZ = entity.getCenter().chunkOffset.getZ() & (RenderRegion.REGION_LENGTH - 1);
        
        return LocalSectionIndex.pack(rX, rY, rZ);
    }
    
    @Override
    public void prepareModelOffset(MutableBlockPos modelOffset, BlockPos pos) {
        modelOffset.set(pos.getX() - (entity.getCenter().chunkOffset.getX() << 4), pos.getY() - (entity.getCenter().chunkOffset.getY() << 4), pos.getZ() - (entity
                .getCenter().chunkOffset.getZ() << 4));
    }
    
    public void renderChunkLayerRubidium(RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, ChunkShaderInterface shader, CameraTransform camera) {
        if (hasBlocks.contains(layer)) {
            VertexBuffer vertexbuffer = buffers.get(layer);
            if (vertexbuffer == null)
                return;
            DefaultChunkRendererExtender.setRenderRegionOffset(shader, entity.getCenter().baseOffset, camera);
            vertexbuffer.bind();
            vertexbuffer.draw();
        }
    }
    
    @Override
    public SortState getTransparencyState() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setQuadSorting(BufferBuilder builder, double x, double y, double z) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void resortTransparency(RenderType layer, double x, double y, double z) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public LittleRenderPipelineType getPipeline() {
        return RubidiumInteractor.PIPELINE;
    }
    
    @Override
    public Vec3 offsetCorrection(RenderChunkExtender chunk) {
        return null;
    }
    
    static final class CompileResults {
        
        public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
        public final List<BlockEntity> blockEntities = new ArrayList<>();
        
        public final ChunkLayerMap<RenderedBufferRubidium> buffers = new ChunkLayerMap<>();
        
        public boolean isEmpty() {
            return buffers.isEmpty() && globalBlockEntities.isEmpty() && blockEntities.isEmpty();
        }
    }
    
    private class RebuildTask implements RebuildTaskExtender {
        
        private ChunkLayerMap<BufferCollection> caches;
        
        private CompileResults compile(float x, float y, float z) {
            CompileResults results = new CompileResults();
            LittleRenderPipelineType.startCompile(LittleAnimationRenderManagerRubidium.this, this);
            
            for (BETiles block : getLevel())
                handleBlockEntity(results, block);
            
            if (caches != null)
                for (Tuple<RenderType, BufferCollection> layer : caches.tuples())
                    results.buffers.put(layer.key, new RenderedBufferRubidium(layer.value));
                
            LittleRenderPipelineType.endCompile(LittleAnimationRenderManagerRubidium.this, this);
            return results;
        }
        
        private void handleBlockEntity(CompileResults results, BETiles entity) {
            LittleRenderPipelineType.compile(LittleAnimationRenderManagerRubidium.this, entity, this);
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
        
        @Override
        public BufferCache upload(RenderType layer, BufferCache cache) {
            getOrCreateBuffers(layer).queueForUpload(cache);
            return cache;
        }
        
    }
    
}
