package team.creative.littletiles.client.render.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.MeshData.SortState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.little.FakeClientLevel;
import team.creative.littletiles.client.mod.embeddium.EmbeddiumManager;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.entity.animation.LittleAnimationLevel;

@OnlyIn(Dist.CLIENT)
public class LittleAnimationRenderManager extends LittleEntityRenderManager<LittleAnimationEntity> implements RenderChunkExtender {
    
    public static LittleEntityRenderManager of(LittleAnimationEntity entity) {
        if (EmbeddiumManager.installed() && !(entity.level() instanceof FakeClientLevel))
            return EmbeddiumManager.createRenderManager(entity);
        return new LittleAnimationRenderManager(entity);
    }
    
    protected final ChunkLayerMap<VertexBuffer> buffers = new ChunkLayerMap<>();
    protected final Set<RenderType> hasBlocks = new ObjectArraySet<>(RenderType.chunkBufferLayers().size());
    protected List<BlockEntity> renderableBlockEntities = new ArrayList<>();
    protected MeshData.SortState transparencyState;
    protected boolean needsUpdate = false;
    
    public ChunkLayerMap<BufferCollection> lastUploaded;
    private volatile int queued;
    
    public LittleAnimationRenderManager(LittleAnimationEntity entity) {
        super(entity);
    }
    
    @Override
    public boolean isSmall() {
        return true;
    }
    
    @Override
    public RenderChunkExtender getRenderChunk(long pos) {
        return this;
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
    public LittleAnimationLevel getLevel() {
        return (LittleAnimationLevel) super.getLevel();
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
        needsUpdate = true;
    }
    
    @Override
    public void compileSections(Camera camera) {
        if (needsUpdate) {
            needsUpdate = false;
            hasBlocks.clear();
            renderableBlockEntities.clear();
            RebuildTask rebuild = new RebuildTask();
            Vec3 cam = camera.getPosition();
            CompileResults results = rebuild.compile((float) cam.x, (float) cam.y, (float) cam.z, LittleTilesClient.ANIMATION_HANDLER.fixedBuffers);
            globalBlockEntities.clear();
            globalBlockEntities.addAll(results.globalBlockEntities);
            renderableBlockEntities = results.blockEntities;
            transparencyState = results.transparencyState;
            prepareUpload();
            for (Entry<RenderType, MeshData> entry : results.renderedLayers.entrySet()) {
                VertexBuffer buffer = getVertexBuffer(entry.getKey());
                if (!buffer.isInvalid()) {
                    buffer.bind();
                    buffer.upload(entry.getValue());
                    VertexBuffer.unbind();
                    
                    BufferCollection buffers = rebuild.getBuffers(entry.getKey());
                    if (buffers != null)
                        uploaded(entry.getKey(), buffers);
                    hasBlocks.add(entry.getKey());
                } else {
                    buffer.close();
                    LittleTiles.LOGGER.error("Could not upload chunk render data due to invalid buffer");
                }
            }
        }
    }
    
    @Override
    protected void renderAllBlockEntities(PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {
        if (renderableBlockEntities != null)
            for (BlockEntity blockEntity : renderableBlockEntities)
                renderBlockEntity(blockEntity, pose, frustum, cam, frameTime, bufferSource);
    }
    
    @Override
    public SortState getTransparencyState() {
        return transparencyState;
    }
    
    @Override
    public void setTransparencyState(SortState state) {
        this.transparencyState = state;
    }
    
    @Override
    public boolean isEmpty(RenderType layer) {
        return !hasBlocks.contains(layer);
    }
    
    @Override
    public void setHasBlock(RenderType layer) {
        hasBlocks.add(layer);
    }
    
    @Override
    public VertexSorting createVertexSorting(double x, double y, double z) {
        return VertexSorting.byDistance((float) x, (float) y, (float) z);
    }
    
    @Override
    public void resortTransparency(RenderType layer, double x, double y, double z) {
        if (transparencyState != null && hasBlocks.contains(RenderType.translucent())) {
            ByteBufferBuilder.Result result = transparencyState.buildSortedIndexBuffer(LittleTilesClient.ANIMATION_HANDLER.fixedBuffers.buffer(RenderType.translucent()),
                createVertexSorting(x, y, z));
            if (result != null) {
                VertexBuffer buffer = getVertexBuffer(layer);
                if (buffer.isInvalid()) {
                    result.close();
                } else {
                    buffer.bind();
                    buffer.uploadIndexBuffer(result);
                    VertexBuffer.unbind();
                }
            }
        }
    }
    
    @Override
    public void renderChunkLayer(RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, Uniform offset) {
        if (hasBlocks.contains(layer)) {
            VertexBuffer vertexbuffer = buffers.get(layer);
            if (vertexbuffer == null)
                return;
            if (offset != null) {
                offset.set((float) -x, (float) -y, (float) -z);
                offset.upload();
            }
            
            vertexbuffer.bind();
            vertexbuffer.draw();
        }
    }
    
    @Override
    protected void setSectionDirty(int x, int y, int z, boolean playerChanged) {
        needsUpdate = true;
    }
    
    @Override
    public void unload() {
        super.unload();
        this.buffers.forEach(VertexBuffer::close);
    }
    
    static final class CompileResults {
        
        public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
        public final List<BlockEntity> blockEntities = new ArrayList<>();
        public final Map<RenderType, MeshData> renderedLayers = new Reference2ObjectArrayMap<>();
        @Nullable
        public MeshData.SortState transparencyState;
        
        public boolean isEmpty() {
            return renderedLayers.isEmpty() && globalBlockEntities.isEmpty() && blockEntities.isEmpty();
        }
    }
    
    protected class RebuildTask {
        
        private ChunkLayerMap<BufferCollection> caches;
        private SectionBufferBuilderPack pack;
        private ChunkLayerMap<BufferBuilder> builders;
        
        private CompileResults compile(float x, float y, float z, SectionBufferBuilderPack pack) {
            this.pack = pack;
            
            CompileResults results = new CompileResults();
            LittleRenderPipelineType.startCompile(LittleAnimationRenderManager.this);
            
            builders = new ChunkLayerMap();
            
            for (BETiles block : getLevel())
                handleBlockEntity(results, block);
            
            for (Tuple<RenderType, BufferBuilder> entry : builders.tuples()) {
                RenderType layer = entry.key;
                MeshData data = entry.value.build();
                if (data != null) {
                    if (layer == RenderType.translucent())
                        results.transparencyState = data.sortQuads(pack.buffer(RenderType.translucent()), createVertexSorting(x, y, z));
                    
                    results.renderedLayers.put(layer, data);
                }
            }
            
            LittleRenderPipelineType.endCompile(LittleAnimationRenderManager.this);
            this.pack = null;
            this.builders = null;
            return results;
        }
        
        private void handleBlockEntity(CompileResults results, BETiles entity) {
            LittleRenderPipelineType.compile(LittleAnimationRenderManager.this.entity.getCenter().chunkOffset.asLong(), entity, x -> (ChunkBufferUploader) builder(x),
                x -> getOrCreateBuffers(x));
            BlockEntityRenderer blockentityrenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
            if (blockentityrenderer != null)
                if (blockentityrenderer.shouldRenderOffScreen(entity))
                    results.globalBlockEntities.add(entity);
                else
                    results.blockEntities.add(entity); //FORGE: Fix MC-112730
        }
        
        public BufferBuilder builder(RenderType layer) {
            BufferBuilder builder = builders.get(layer);
            if (builder == null)
                builders.put(layer, builder = new BufferBuilder(pack.buffer(layer), VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK));
            return builder;
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
