package team.creative.littletiles.client.render.level;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.MeshData.SortState;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher.CompiledSection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent.AdditionalSectionRenderer;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.entity.LittleLevelRenderManager;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.SectionCompilerResultsExtender;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.mixin.client.render.CompiledSectionAccessor;

@OnlyIn(Dist.CLIENT)
public class LittleRenderChunk implements RenderChunkExtender {
    
    public final LittleLevelRenderManager manager;
    public final SectionPos section;
    public final BlockPos pos;
    public final AtomicReference<CompiledSection> compiled = new AtomicReference<>(CompiledSection.UNCOMPILED);
    private AABB bb;
    public final AtomicBoolean considered = new AtomicBoolean();
    @Nullable
    private RebuildTask lastRebuildTask;
    @Nullable
    private ResortTransparencyTask lastResortTransparencyTask;
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    private final ChunkLayerMap<VertexBuffer> buffers = new ChunkLayerMap(x -> new VertexBuffer(VertexBuffer.Usage.STATIC));
    private boolean dirty = true;
    private final SectionPos[] neighbors;
    private boolean playerChanged;
    
    public ChunkLayerMap<BufferCollection> lastUploaded;
    private volatile int queued;
    
    public LittleRenderChunk(LittleLevelRenderManager manager, SectionPos pos) {
        this.manager = manager;
        this.section = pos;
        this.pos = section.origin();
        this.bb = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 16, pos.getY() + 16, pos.getZ() + 16);
        this.neighbors = new SectionPos[Direction.values().length];
        for (int i = 0; i < neighbors.length; i++) {
            Direction direction = Direction.values()[i];
            neighbors[i] = SectionPos.of(section.getX() + direction.getStepX(), section.getY() + direction.getStepY(), section.getZ() + direction.getStepZ());
        }
    }
    
    public LittleSubLevel level() {
        return manager.getLevel();
    }
    
    private boolean doesChunkExistAt(SectionPos pos) {
        return level().getChunk(pos.getX(), pos.getZ(), ChunkStatus.FULL, false) != null;
    }
    
    public boolean hasAllNeighbors() {
        if (this.getDistToPlayerSqr() <= 576.0D)
            return true;
        return doesChunkExistAt(neighbors[Direction.WEST.ordinal()]) && doesChunkExistAt(neighbors[Direction.NORTH.ordinal()]) && doesChunkExistAt(neighbors[Direction.EAST
                .ordinal()]) && doesChunkExistAt(neighbors[Direction.SOUTH.ordinal()]);
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
    
    public AABB getBB() {
        return this.bb;
    }
    
    @Override
    public VertexBuffer getVertexBuffer(RenderType layer) {
        return this.buffers.get(layer);
    }
    
    protected double getDistToPlayerSqr() {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cam = level().getOrigin().transformPointToFakeWorld(camera.getPosition());
        Vec3 center = bb.getCenter();
        double d0 = center.x - cam.x;
        double d1 = center.y - cam.y;
        double d2 = center.z - cam.z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }
    
    public CompiledSection getCompiledSection() {
        return this.compiled.get();
    }
    
    private void reset() {
        this.cancelTasks();
        this.compiled.set(SectionRenderDispatcher.CompiledSection.UNCOMPILED);
        this.dirty = true;
    }
    
    public void releaseBuffers() {
        this.reset();
        this.buffers.forEach(VertexBuffer::close);
    }
    
    public void setDirty(boolean playerChanged) {
        boolean flag = this.dirty;
        this.dirty = true;
        this.playerChanged = playerChanged | (flag && this.playerChanged);
    }
    
    public void setNotDirty() {
        this.dirty = false;
        this.playerChanged = false;
    }
    
    public boolean isDirty() {
        return this.dirty;
    }
    
    @Override
    public void markReadyForUpdate(boolean playerChanged) {
        setDirty(playerChanged);
    }
    
    public boolean isDirtyFromPlayer() {
        return this.dirty && this.playerChanged;
    }
    
    public boolean resortTransparency(RenderType layer) {
        CompiledSection compiled = this.getCompiledSection();
        if (this.lastResortTransparencyTask != null)
            this.lastResortTransparencyTask.cancel();
        
        if (!((CompiledSectionAccessor) compiled).getHasBlocks().contains(layer))
            return false;
        
        this.lastResortTransparencyTask = new ResortTransparencyTask(this.getDistToPlayerSqr(), compiled);
        manager.schedule(this.lastResortTransparencyTask);
        return true;
    }
    
    protected boolean cancelTasks() {
        boolean flag = false;
        if (this.lastRebuildTask != null) {
            this.lastRebuildTask.cancel();
            this.lastRebuildTask = null;
            flag = true;
        }
        
        if (this.lastResortTransparencyTask != null) {
            this.lastResortTransparencyTask.cancel();
            this.lastResortTransparencyTask = null;
        }
        
        return flag;
    }
    
    public CompileTask createCompileTask(RenderRegionCache cache) {
        boolean canceled = this.cancelTasks();
        var additionalRenderers = ClientHooks.gatherAdditionalRenderers(pos, (Level) manager.getLevel());
        RenderChunkRegion region = cache.createRegion((Level) manager.getLevel(), section, additionalRenderers.isEmpty());
        return this.lastRebuildTask = new RebuildTask(this.getDistToPlayerSqr(), region, canceled || this.compiled.get() != CompiledSection.UNCOMPILED, additionalRenderers);
    }
    
    public void compileASync(RenderRegionCache cache) {
        manager.schedule(createCompileTask(cache));
    }
    
    public void compile(RenderRegionCache cache) {
        this.createCompileTask(cache).doTask(manager.fixedBuffers());
    }
    
    public void updateGlobalBlockEntities(Collection<BlockEntity> blockEntities) {
        Set<BlockEntity> set = Sets.newHashSet(blockEntities);
        Set<BlockEntity> set1;
        synchronized (this.globalBlockEntities) {
            set1 = Sets.newHashSet(this.globalBlockEntities);
            set.removeAll(this.globalBlockEntities);
            set1.removeAll(blockEntities);
            this.globalBlockEntities.clear();
            this.globalBlockEntities.addAll(blockEntities);
        }
        
        manager.updateGlobalBlockEntities(set1, set);
    }
    
    @Override
    public SortState getTransparencyState() {
        return ((CompiledSectionAccessor) getCompiledSection()).getTransparencyState();
    }
    
    @Override
    public void setTransparencyState(SortState state) {
        ((CompiledSectionAccessor) getCompiledSection()).setTransparencyState(state);
    }
    
    @Override
    public void setHasBlock(RenderType layer) {
        CompiledSection compiled = getCompiledSection();
        if (compiled != CompiledSection.UNCOMPILED)
            ((CompiledSectionAccessor) compiled).getHasBlocks().add(layer);
    }
    
    @Override
    public boolean isEmpty(RenderType layer) {
        return getCompiledSection().isEmpty(layer);
    }
    
    @Override
    public VertexSorting createVertexSorting(double x, double y, double z) {
        return VertexSorting.byDistance((float) x - pos.getX(), (float) y - pos.getY(), (float) z - pos.getZ());
    }
    
    public static enum SectionTaskResult {
        SUCCESSFUL,
        CANCELLED;
    }
    
    public abstract class CompileTask implements Comparable<CompileTask> {
        
        protected final double distAtCreation;
        protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
        public final boolean isHighPriority;
        
        public CompileTask(double distAtCreation, boolean isHighPriority) {
            this.distAtCreation = distAtCreation;
            this.isHighPriority = isHighPriority;
        }
        
        public abstract CompletableFuture<SectionTaskResult> doTask(SectionBufferBuilderPack p_112853_);
        
        public abstract void cancel();
        
        public abstract String name();
        
        @Override
        public int compareTo(CompileTask other) {
            return Doubles.compare(this.distAtCreation, other.distAtCreation);
        }
    }
    
    class RebuildTask extends CompileTask {
        
        private final List<AdditionalSectionRenderer> additionalRenderers;
        private RenderChunkRegion region;
        
        public RebuildTask(double distAtCreation, RenderChunkRegion region, boolean isHighPriority, List<AdditionalSectionRenderer> additionalRenderers) {
            super(distAtCreation, isHighPriority);
            this.region = region;
            this.additionalRenderers = additionalRenderers;
        }
        
        @Override
        public String name() {
            return "rend_chk_rebuild";
        }
        
        @Override
        public CompletableFuture<SectionTaskResult> doTask(SectionBufferBuilderPack pack) {
            if (this.isCancelled.get())
                return CompletableFuture.completedFuture(SectionTaskResult.CANCELLED);
            
            /*if (!LittleRenderChunk.this.hasAllNeighbors()) {
                this.level = null;
                LittleRenderChunk.this.setDirty(false);
                this.isCancelled.set(true);
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            }*/
            
            if (this.isCancelled.get())
                return CompletableFuture.completedFuture(SectionTaskResult.CANCELLED);
            
            Vec3d cam = manager.getCameraPosition();
            SectionCompiler.Results results = LittleTilesClient.ANIMATION_HANDLER.sectionCompiler.compile(section, region, createVertexSorting(cam.x, cam.y, cam.z), pack,
                this.additionalRenderers);
            LittleRenderChunk.this.updateGlobalBlockEntities(results.globalBlockEntities);
            
            if (this.isCancelled.get()) {
                results.release();
                return CompletableFuture.completedFuture(SectionTaskResult.CANCELLED);
            }
            
            if (((SectionCompilerResultsExtender) (Object) results).isEmpty()) {
                manager.emptyChunk(LittleRenderChunk.this);
                LittleRenderChunk.this.compiled.set(CompiledSection.UNCOMPILED);
                results.release();
                LittleRenderChunk.this.prepareUpload();
                return CompletableFuture.completedFuture(SectionTaskResult.SUCCESSFUL);
            }
            
            CompiledSection compiled = new CompiledSection();
            ((CompiledSectionAccessor) compiled).setVisibilitySet(results.visibilitySet);
            compiled.getRenderableBlockEntities().addAll(results.blockEntities);
            ((CompiledSectionAccessor) compiled).setTransparencyState(results.transparencyState);
            
            List<CompletableFuture<Void>> list = Lists.newArrayList();;
            for (Entry<RenderType, MeshData> entry : results.renderedLayers.entrySet()) {
                list.add(manager.uploadChunkLayer(entry.getValue(), LittleRenderChunk.this.getVertexBuffer(entry.getKey())));
                ((CompiledSectionAccessor) compiled).getHasBlocks().add(entry.getKey());
            }
            
            return Util.sequenceFailFast(list).handle((voids, throwable) -> {
                if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException))
                    Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering chunk"));
                
                if (this.isCancelled.get())
                    return SectionTaskResult.CANCELLED;
                
                LittleRenderChunk.this.compiled.set(compiled);
                manager.queueChunk(LittleRenderChunk.this);
                return SectionTaskResult.SUCCESSFUL;
            }).whenComplete((result, exception) -> {
                if (result == SectionTaskResult.SUCCESSFUL) {
                    LittleRenderChunk.this.prepareUpload();
                    var caches = ((SectionCompilerResultsExtender) (Object) results).getCaches();
                    if (caches != null)
                        for (Tuple<RenderType, BufferCollection> tuple : caches.tuples())
                            LittleRenderChunk.this.uploaded(tuple.key, tuple.value);
                }
            });
            
        }
        
        @Override
        public void cancel() {
            this.region = null;
            if (this.isCancelled.compareAndSet(false, true))
                LittleRenderChunk.this.setDirty(false);
        }
        
    }
    
    @OnlyIn(Dist.CLIENT)
    class ResortTransparencyTask extends CompileTask {
        
        private final CompiledSection compiledSection;
        
        public ResortTransparencyTask(double distAtCreation, CompiledSection section) {
            super(distAtCreation, true);
            this.compiledSection = section;
        }
        
        @Override
        public String name() {
            return "rend_chk_sort";
        }
        
        @Override
        public CompletableFuture<SectionTaskResult> doTask(SectionBufferBuilderPack pack) {
            if (this.isCancelled.get())
                return CompletableFuture.completedFuture(SectionTaskResult.CANCELLED);
            if (this.isCancelled.get())
                return CompletableFuture.completedFuture(SectionTaskResult.CANCELLED);
            
            if (!LittleRenderChunk.this.hasAllNeighbors()) {
                this.isCancelled.set(true);
                return CompletableFuture.completedFuture(SectionTaskResult.CANCELLED);
            }
            
            Vec3d cam = manager.getCameraPosition();
            MeshData.SortState sortstate = ((CompiledSectionAccessor) this.compiledSection).getTransparencyState();
            if (sortstate != null && !this.compiledSection.isEmpty(RenderType.translucent())) {
                VertexSorting vertexsorting = LittleRenderChunk.this.createVertexSorting(cam.x, cam.y, cam.z);
                ByteBufferBuilder.Result result = sortstate.buildSortedIndexBuffer(pack.buffer(RenderType.translucent()), vertexsorting);
                if (result == null)
                    return CompletableFuture.completedFuture(SectionTaskResult.CANCELLED);
                if (this.isCancelled.get()) {
                    result.close();
                    return CompletableFuture.completedFuture(SectionTaskResult.CANCELLED);
                }
                CompletableFuture<SectionTaskResult> completablefuture = manager.uploadSectionIndexBuffer(result, LittleRenderChunk.this.getVertexBuffer(RenderType.translucent()))
                        .thenApply(x -> SectionTaskResult.CANCELLED);
                return completablefuture.handle((r, exception) -> {
                    if (exception != null && !(exception instanceof CancellationException) && !(exception instanceof InterruptedException))
                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(exception, "Rendering chunk"));
                    
                    return this.isCancelled.get() ? SectionTaskResult.CANCELLED : SectionTaskResult.SUCCESSFUL;
                });
            }
            return CompletableFuture.completedFuture(SectionTaskResult.CANCELLED);
        }
        
        @Override
        public void cancel() {
            this.isCancelled.set(true);
        }
    }
}
