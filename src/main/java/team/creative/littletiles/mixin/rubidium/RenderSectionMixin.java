package team.creative.littletiles.mixin.rubidium;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import com.mojang.blaze3d.vertex.VertexBuffer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.jellysquid.mods.sodium.client.gl.arena.GlBufferSegment;
import me.jellysquid.mods.sodium.client.gl.buffer.GlBuffer;
import me.jellysquid.mods.sodium.client.gl.util.ElementRange;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkGraphicsState;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;
import team.creative.littletiles.client.render.cache.buffer.UploadableBufferHolder;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
import team.creative.littletiles.client.rubidium.RubidiumManager;
import team.creative.littletiles.client.rubidium.buffer.RubidiumUploadableBufferHolder;
import team.creative.littletiles.client.rubidium.data.ChunkRenderDataExtender;

@Mixin(RenderSection.class)
public abstract class RenderSectionMixin implements RenderChunkExtender {
    
    @Shadow(remap = false)
    private int chunkX;
    
    @Shadow(remap = false)
    private int chunkY;
    
    @Shadow(remap = false)
    private int chunkZ;
    
    @Shadow(remap = false)
    private int chunkId;
    
    @Shadow(remap = false)
    private ChunkRenderData data;
    
    @Unique
    private BlockPos origin;
    
    @Unique
    private volatile int queued;
    
    @Shadow(remap = false)
    public abstract void markForUpdate(ChunkUpdateType type);
    
    @Shadow(remap = false)
    public abstract ChunkGraphicsState getGraphicsState(BlockRenderPass pass);
    
    @Override
    public void begin(BufferBuilder builder) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public VertexBuffer getVertexBuffer(RenderType layer) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void markReadyForUpdate(boolean playerChanged) {
        markForUpdate(playerChanged ? ChunkUpdateType.IMPORTANT_REBUILD : ChunkUpdateType.REBUILD);
    }
    
    @Override
    public void setQuadSorting(BufferBuilder builder, double x, double y, double z) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isEmpty(RenderType layer) {
        return getGraphicsState(RubidiumManager.getPass(layer)) == null;
    }
    
    @Override
    public SortState getTransparencyState() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setHasBlock(RenderType layer) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public BlockPos standardOffset() {
        if (origin == null)
            origin = new BlockPos(chunkX * 16, chunkY * 16, chunkZ * 16);
        return origin;
    }
    
    @Override
    public LittleRenderPipelineType getPipeline() {
        return RubidiumManager.PIPELINE;
    }
    
    @Override
    public int chunkId() {
        return chunkId;
    }
    
    @Inject(at = @At("HEAD"), method = "setData(Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderData;)V", remap = false, require = 1)
    public void setData(ChunkRenderData data, CallbackInfo info) {
        ChunkLayerMap<ChunkLayerCache> map = ((ChunkRenderDataExtender) data).getCaches();
        if (map == null)
            return;
        for (ChunkLayerCache cache : map)
            cache.uploaded(queued == 0);
    }
    
    public ByteBuffer downloadSegment(GlBufferSegment segment) {
        GlBuffer buffer = ((GlBufferSegmentAccessor) segment).getArena().getBufferObject();
        return downloadUploadedData((VertexBufferExtender) buffer, segment.getOffset(), segment.getLength());
    }
    
    @Override
    public void backToRAM() {
        ChunkLayerMap<ChunkLayerCache> caches = ((ChunkRenderDataExtender) data).getCaches();
        if (caches == null || caches.isEmpty())
            return;
        
        Runnable run = () -> {
            synchronized (this) {
                for (Tuple<RenderType, ChunkLayerCache> tuple : caches.tuples()) {
                    ChunkGraphicsState state = getGraphicsState(RubidiumManager.getPass(tuple.key));
                    if (state == null)
                        continue;
                    
                    ByteBuffer vertexData = downloadSegment(state.getVertexSegment());
                    ByteBuffer indexData = downloadSegment(state.getIndexSegment());
                    if (vertexData == null || indexData == null) {
                        tuple.value.discard();
                        continue;
                    }
                    tuple.value.download(vertexData);
                    
                    for (UploadableBufferHolder buffer : tuple.value) {
                        RubidiumUploadableBufferHolder holder = (RubidiumUploadableBufferHolder) buffer;
                        holder.prepareFacingBufferDownload();
                        
                        for (int i = 0; i < ModelQuadFacing.COUNT; i++) {
                            ModelQuadFacing facing = ModelQuadFacing.VALUES[i];
                            ElementRange range = state.getModelPart(facing);
                            int size = holder.facingIndexCount(facing);
                            int index = holder.facingIndexOffset(facing);
                            if (indexData.capacity() >= range.elementPointer() + (index + size) * Integer.BYTES) {
                                indexData.position(range.elementPointer() + index * Integer.BYTES);
                                IntArrayList list = new IntArrayList(size);
                                for (int j = 0; j < size; j++)
                                    list.add(indexData.getInt());
                                holder.downloadFacingBuffer(list, facing);
                            } else {
                                holder.invalidate();
                                break;
                            }
                        }
                    }
                    
                }
                
                caches.clear();
            }
        };
        try {
            CompletableFuture.runAsync(run, Minecraft.getInstance());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    
    @Override
    public void startBuilding(RebuildTaskExtender task) {
        if (data == null)
            return;
        queued++;
        backToRAM();
    }
    
    @Override
    public void endBuilding(RebuildTaskExtender task) {
        queued--;
    }
}
