package team.creative.littletiles.client.render.level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.chunk.VisibilitySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;
import team.creative.littletiles.client.render.entity.LittleLevelRenderManager;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.mixin.client.render.CompiledChunkAccessor;

public class LittleRenderChunk implements RenderChunkExtender {
    
    public final LittleLevelRenderManager manager;
    public final SectionPos section;
    public final BlockPos pos;
    public final AtomicReference<CompiledChunk> compiled = new AtomicReference<>(CompiledChunk.UNCOMPILED);
    private AABB bb;
    public final AtomicBoolean considered = new AtomicBoolean();
    @Nullable
    private RebuildTask lastRebuildTask;
    @Nullable
    private ResortTransparencyTask lastResortTransparencyTask;
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    private final Map<RenderType, VertexBuffer> buffers = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap(x -> x, (x) -> new VertexBuffer()));
    private boolean dirty = true;
    private final SectionPos[] neighbors;
    private boolean playerChanged;
    private boolean dynamicLightUpdate = false;
    
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
    
    @Override
    public boolean dynamicLightUpdate() {
        return dynamicLightUpdate;
    }
    
    @Override
    public void dynamicLightUpdate(boolean value) {
        dynamicLightUpdate = value;
    }
    
    public LittleLevel level() {
        return manager.level;
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
    
    public ChunkRenderDispatcher.CompiledChunk getCompiledChunk() {
        return this.compiled.get();
    }
    
    @Override
    public void begin(BufferBuilder builder) {
        beginLayer(builder);
    }
    
    private void beginLayer(BufferBuilder builder) {
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
    }
    
    private void reset() {
        this.cancelTasks();
        this.compiled.set(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
        this.dirty = true;
    }
    
    public void releaseBuffers() {
        this.reset();
        this.buffers.values().forEach(VertexBuffer::close);
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
        CompiledChunk compiled = this.getCompiledChunk();
        if (this.lastResortTransparencyTask != null)
            this.lastResortTransparencyTask.cancel();
        
        if (!((CompiledChunkAccessor) compiled).getHasBlocks().contains(layer))
            return false;
        
        this.lastResortTransparencyTask = new ResortTransparencyTask(section.chunk(), this.getDistToPlayerSqr(), compiled);
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
    
    public ChunkCompileTask createCompileTask() {
        boolean canceled = this.cancelTasks();
        this.lastRebuildTask = new RebuildTask(section.chunk(), this.getDistToPlayerSqr(), manager.level.asLevel(), canceled || this.compiled.get() != CompiledChunk.UNCOMPILED);
        return this.lastRebuildTask;
    }
    
    public void compileASync() {
        manager.schedule(createCompileTask());
    }
    
    public void compile() {
        this.createCompileTask().doTask(manager.fixedBuffers());
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
        
        LittleTilesClient.ANIMATION_HANDLER.updateGlobalBlockEntities(set1, set);
    }
    
    public static enum ChunkTaskResult {
        SUCCESSFUL,
        CANCELLED;
    }
    
    public abstract class ChunkCompileTask implements Comparable<ChunkCompileTask> {
        
        protected final double distAtCreation;
        protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
        public final boolean isHighPriority;
        protected Map<BlockPos, ModelData> modelData;
        
        public ChunkCompileTask(@Nullable ChunkPos pos, double distAtCreation, boolean isHighPriority) {
            this.distAtCreation = distAtCreation;
            this.isHighPriority = isHighPriority;
            this.modelData = pos == null ? java.util.Collections.emptyMap() : manager.level.getModelDataManager().getAt(pos);
        }
        
        public abstract CompletableFuture<ChunkTaskResult> doTask(ChunkBufferBuilderPack p_112853_);
        
        public abstract void cancel();
        
        public abstract String name();
        
        @Override
        public int compareTo(ChunkCompileTask other) {
            return Doubles.compare(this.distAtCreation, other.distAtCreation);
        }
        
        public ModelData getModelData(BlockPos pos) {
            return modelData.getOrDefault(pos, ModelData.EMPTY);
        }
    }
    
    class RebuildTask extends ChunkCompileTask implements RebuildTaskExtender {
        
        @Nullable
        protected Level level;
        private HashMap<RenderType, ChunkLayerCache> caches;
        private ChunkBufferBuilderPack pack;
        private Set<RenderType> renderTypes;
        
        @Deprecated
        public RebuildTask(@Nullable double distAtCreation, Level level, boolean isHighPriority) {
            this(null, distAtCreation, level, isHighPriority);
        }
        
        public RebuildTask(@Nullable ChunkPos pos, double distAtCreation, @Nullable Level level, boolean isHighPriority) {
            super(pos, distAtCreation, isHighPriority);
            this.level = level;
        }
        
        @Override
        public String name() {
            return "rend_chk_rebuild";
        }
        
        @Override
        public CompletableFuture<ChunkTaskResult> doTask(ChunkBufferBuilderPack pack) {
            if (this.isCancelled.get())
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            
            /*if (!LittleRenderChunk.this.hasAllNeighbors()) {
                this.level = null;
                LittleRenderChunk.this.setDirty(false);
                this.isCancelled.set(true);
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            }*/
            
            if (this.isCancelled.get())
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            
            Vec3d cam = manager.getCameraPosition();
            RebuildTask.CompileResults results = this.compile((float) cam.x, (float) cam.y, (float) cam.z, pack);
            LittleRenderChunk.this.updateGlobalBlockEntities(results.globalBlockEntities);
            
            if (this.isCancelled.get()) {
                results.renderedLayers.values().forEach(BufferBuilder.RenderedBuffer::release);
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            }
            
            if (results.isEmpty()) {
                manager.emptyChunk(LittleRenderChunk.this);
                LittleRenderChunk.this.compiled.set(CompiledChunk.UNCOMPILED);
                results.renderedLayers.values().forEach(BufferBuilder.RenderedBuffer::release);
                return CompletableFuture.completedFuture(ChunkTaskResult.SUCCESSFUL);
            }
            
            CompiledChunk compiled = new CompiledChunk();
            ((CompiledChunkAccessor) compiled).setVisibilitySet(results.visibilitySet);
            compiled.getRenderableBlockEntities().addAll(results.blockEntities);
            ((CompiledChunkAccessor) compiled).setTransparencyState(results.transparencyState);
            
            List<CompletableFuture<Void>> list = Lists.newArrayList();
            results.renderedLayers.forEach((layer, buffer) -> {
                list.add(manager.uploadChunkLayer(buffer, LittleRenderChunk.this.getVertexBuffer(layer)));
                ((CompiledChunkAccessor) compiled).getHasBlocks().add(layer);
            });
            
            return Util.sequenceFailFast(list).handle((voids, throwable) -> {
                if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException))
                    Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering chunk"));
                
                if (this.isCancelled.get())
                    return ChunkTaskResult.CANCELLED;
                
                LittleRenderChunk.this.compiled.set(compiled);
                manager.queueChunk(LittleRenderChunk.this);
                return ChunkTaskResult.SUCCESSFUL;
            });
            
        }
        
        private CompileResults compile(float x, float y, float z, ChunkBufferBuilderPack pack) {
            this.pack = pack;
            
            LittleChunkDispatcher.startCompile(LittleRenderChunk.this);
            CompileResults results = new CompileResults();
            BlockPos maxPos = pos.offset(15, 15, 15);
            VisGraph visgraph = new VisGraph();
            Level renderchunkregion = this.level;
            this.level = null;
            PoseStack posestack = new PoseStack();
            if (renderchunkregion != null) {
                ModelBlockRenderer.enableCaching();
                renderTypes = new ReferenceArraySet<>(RenderType.chunkBufferLayers().size());
                RandomSource randomsource = RandomSource.create();
                BlockRenderDispatcher blockrenderdispatcher = Minecraft.getInstance().getBlockRenderer();
                
                for (BlockPos blockpos2 : BlockPos.betweenClosed(pos, maxPos)) {
                    BlockState blockstate = renderchunkregion.getBlockState(blockpos2);
                    if (blockstate.isSolidRender(renderchunkregion, blockpos2))
                        visgraph.setOpaque(blockpos2);
                    
                    if (blockstate.hasBlockEntity()) {
                        BlockEntity blockentity = renderchunkregion.getBlockEntity(blockpos2);
                        if (blockentity != null)
                            this.handleBlockEntity(results, blockentity);
                    }
                    
                    BlockState blockstate1 = renderchunkregion.getBlockState(blockpos2);
                    FluidState fluidstate = blockstate1.getFluidState();
                    if (!fluidstate.isEmpty()) {
                        RenderType rendertype = ItemBlockRenderTypes.getRenderLayer(fluidstate);
                        BufferBuilder bufferbuilder = pack.builder(rendertype);
                        if (renderTypes.add(rendertype))
                            LittleRenderChunk.this.beginLayer(bufferbuilder);
                        
                        blockrenderdispatcher.renderLiquid(blockpos2, renderchunkregion, bufferbuilder, blockstate1, fluidstate);
                    }
                    
                    if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                        var model = blockrenderdispatcher.getBlockModel(blockstate);
                        var modelData = getModelData(blockpos2);
                        randomsource.setSeed(blockstate.getSeed(blockpos2));
                        for (RenderType rendertype2 : model.getRenderTypes(blockstate, randomsource, modelData)) {
                            BufferBuilder bufferbuilder2 = pack.builder(rendertype2);
                            if (renderTypes.add(rendertype2))
                                LittleRenderChunk.this.beginLayer(bufferbuilder2);
                            
                            posestack.pushPose();
                            posestack.translate(blockpos2.getX() & 15, blockpos2.getY() & 15, blockpos2.getZ() & 15);
                            blockrenderdispatcher.renderBatched(blockstate, blockpos2, renderchunkregion, posestack, bufferbuilder2, true, randomsource, modelData, rendertype2);
                            posestack.popPose();
                        }
                    }
                }
                
                if (renderTypes.contains(RenderType.translucent())) {
                    BufferBuilder bufferbuilder1 = pack.builder(RenderType.translucent());
                    if (!bufferbuilder1.isCurrentBatchEmpty()) {
                        bufferbuilder1.setQuadSortOrigin(x - pos.getX(), y - pos.getY(), z - pos.getZ());
                        results.transparencyState = bufferbuilder1.getSortState();
                    }
                }
                
                for (RenderType rendertype1 : renderTypes) {
                    BufferBuilder.RenderedBuffer rendered = pack.builder(rendertype1).endOrDiscardIfEmpty();
                    if (rendered != null)
                        results.renderedLayers.put(rendertype1, rendered);
                }
                
                ModelBlockRenderer.clearCache();
            }
            
            results.visibilitySet = visgraph.resolve();
            LittleChunkDispatcher.endCompile(LittleRenderChunk.this, this);
            return results;
        }
        
        private <E extends BlockEntity> void handleBlockEntity(CompileResults results, E entity) {
            if (entity instanceof BETiles tiles)
                LittleChunkDispatcher.add(LittleRenderChunk.this, tiles, this);
            BlockEntityRenderer<E> blockentityrenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
            if (blockentityrenderer != null)
                if (blockentityrenderer.shouldRenderOffScreen(entity))
                    results.globalBlockEntities.add(entity);
                else
                    results.blockEntities.add(entity); //FORGE: Fix MC-112730
        }
        
        @Override
        public void cancel() {
            this.level = null;
            if (this.isCancelled.compareAndSet(false, true))
                LittleRenderChunk.this.setDirty(false);
            
        }
        
        @Override
        public void clear() {
            this.pack = null;
            this.renderTypes = null;
        }
        
        @Override
        public BufferBuilder builder(RenderType layer) {
            BufferBuilder builder = pack.builder(layer);
            if (renderTypes.add(layer))
                LittleRenderChunk.this.begin(builder);
            return builder;
        }
        
        @Override
        public HashMap<RenderType, ChunkLayerCache> getLayeredCache() {
            return caches;
        }
        
        @Override
        public ChunkLayerCache getOrCreate(RenderType layer) {
            if (caches == null)
                caches = new HashMap<>();
            ChunkLayerCache cache = caches.get(layer);
            if (cache == null)
                caches.put(layer, cache = new ChunkLayerCache());
            return cache;
        }
        
        @OnlyIn(Dist.CLIENT)
        static final class CompileResults {
            
            public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
            public final List<BlockEntity> blockEntities = new ArrayList<>();
            public final Map<RenderType, BufferBuilder.RenderedBuffer> renderedLayers = new Reference2ObjectArrayMap<>();
            public VisibilitySet visibilitySet = new VisibilitySet();
            @Nullable
            public BufferBuilder.SortState transparencyState;
            
            public boolean isEmpty() {
                return renderedLayers.isEmpty() && globalBlockEntities.isEmpty() && blockEntities.isEmpty();
            }
        }
        
    }
    
    @OnlyIn(Dist.CLIENT)
    class ResortTransparencyTask extends ChunkCompileTask {
        
        private final CompiledChunk compiledChunk;
        
        @Deprecated
        public ResortTransparencyTask(double distAtCreation, CompiledChunk chunk) {
            this(null, distAtCreation, chunk);
        }
        
        public ResortTransparencyTask(@Nullable ChunkPos pos, double distAtCreation, CompiledChunk chunk) {
            super(pos, distAtCreation, true);
            this.compiledChunk = chunk;
        }
        
        @Override
        public String name() {
            return "rend_chk_sort";
        }
        
        @Override
        public CompletableFuture<ChunkTaskResult> doTask(ChunkBufferBuilderPack p_112893_) {
            if (this.isCancelled.get())
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            if (this.isCancelled.get())
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            
            if (!LittleRenderChunk.this.hasAllNeighbors()) {
                this.isCancelled.set(true);
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            }
            
            Vec3d cam = manager.getCameraPosition();
            BufferBuilder.SortState sortstate = ((CompiledChunkAccessor) this.compiledChunk).getTransparencyState();
            if (sortstate != null && !this.compiledChunk.isEmpty(RenderType.translucent())) {
                BufferBuilder bufferbuilder = p_112893_.builder(RenderType.translucent());
                LittleRenderChunk.this.beginLayer(bufferbuilder);
                bufferbuilder.restoreSortState(sortstate);
                bufferbuilder.setQuadSortOrigin((float) cam.x - pos.getX(), (float) cam.y - pos.getY(), (float) cam.z - pos.getZ());
                ((CompiledChunkAccessor) this.compiledChunk).setTransparencyState(bufferbuilder.getSortState());
                BufferBuilder.RenderedBuffer rendered = bufferbuilder.end();
                if (this.isCancelled.get()) {
                    rendered.release();
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                CompletableFuture<ChunkTaskResult> completablefuture = manager.uploadChunkLayer(rendered, LittleRenderChunk.this.getVertexBuffer(RenderType.translucent()))
                        .thenApply(x -> ChunkTaskResult.CANCELLED);
                return completablefuture.handle((result, exception) -> {
                    if (exception != null && !(exception instanceof CancellationException) && !(exception instanceof InterruptedException))
                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(exception, "Rendering chunk"));
                    
                    return this.isCancelled.get() ? ChunkTaskResult.CANCELLED : ChunkTaskResult.SUCCESSFUL;
                });
            }
            return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
        }
        
        @Override
        public void cancel() {
            this.isCancelled.set(true);
        }
    }
}
