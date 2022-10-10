package team.creative.littletiles.client.render.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.chunk.VisibilitySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
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
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.littletiles.mixin.CompiledChunkAccessor;

public class LittleRenderChunk {
    
    public final LittleLevelRenderManager manager;
    public final SectionPos section;
    public final AtomicReference<ChunkRenderDispatcher.CompiledChunk> compiled = new AtomicReference<>(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
    private AABB bb;
    private final AtomicInteger initialCompilationCancelCount = new AtomicInteger(0);
    @Nullable
    private RebuildTask lastRebuildTask;
    @Nullable
    private ResortTransparencyTask lastResortTransparencyTask;
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    private final Map<RenderType, VertexBuffer> buffers = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap(x -> x, (x) -> new VertexBuffer()));
    private boolean dirty = true;
    private final SectionPos[] neighbors;
    private boolean playerChanged;
    
    public LittleRenderChunk(SectionPos pos) {
        this.section = pos;
        BlockPos realOrigin = section.origin();
        this.bb = new AABB(realOrigin.getX(), realOrigin.getY(), realOrigin.getZ(), realOrigin.getX() + 16, realOrigin.getY() + 16, realOrigin.getZ() + 16);
        this.neighbors = new SectionPos[Direction.values().length];
        for (int i = 0; i < neighbors.length; i++) {
            Direction direction = Direction.values()[i];
            neighbors[i] = SectionPos.of(section.getX() + direction.getStepX(), section.getY() + direction.getStepY(), section.getZ() + direction.getStepZ());
        }
    }
    
    public CreativeLevel level() {
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
    
    public VertexBuffer getBuffer(RenderType layer) {
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
    
    private void beginLayer(BufferBuilder p_112806_) {
        p_112806_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
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
    
    public void setDirty(boolean dirty) {
        boolean flag = this.dirty;
        this.dirty = true;
        this.playerChanged = dirty | (flag && this.playerChanged);
    }
    
    public void setNotDirty() {
        this.dirty = false;
        this.playerChanged = false;
    }
    
    public boolean isDirty() {
        return this.dirty;
    }
    
    public boolean isDirtyFromPlayer() {
        return this.dirty && this.playerChanged;
    }
    
    public BlockPos getRelativeOrigin(Direction p_112825_) {
        return this.relativeOrigins[p_112825_.ordinal()];
    }
    
    public boolean resortTransparency(RenderType layer, ChunkRenderDispatcher dispatcher) {
        CompiledChunk compiled = this.getCompiledChunk();
        if (this.lastResortTransparencyTask != null)
            this.lastResortTransparencyTask.cancel();
        
        if (!((CompiledChunkAccessor) compiled).getHasBlocks().contains(layer))
            return false;
        
        this.lastResortTransparencyTask = new ResortTransparencyTask(new net.minecraft.world.level.ChunkPos(getOrigin()), this.getDistToPlayerSqr(), compiled);
        dispatcher.schedule(this.lastResortTransparencyTask);
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
    
    public ChunkCompileTask createCompileTask(RenderRegionCache p_200438_) {
        boolean flag = this.cancelTasks();
        BlockPos blockpos = this.origin.immutable();
        int i = 1;
        RenderChunkRegion renderchunkregion = p_200438_.createRegion(manager.level, blockpos.offset(-1, -1, -1), blockpos.offset(16, 16, 16), 1);
        boolean flag1 = this.compiled.get() == ChunkRenderDispatcher.CompiledChunk.UNCOMPILED;
        if (flag1 && flag)
            this.initialCompilationCancelCount.incrementAndGet();
        
        this.lastRebuildTask = new RebuildTask(new net.minecraft.world.level.ChunkPos(getOrigin()), this
                .getDistToPlayerSqr(), renderchunkregion, flag || this.compiled.get() != ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
        return this.lastRebuildTask;
    }
    
    public void rebuildChunkAsync(ChunkRenderDispatcher dispatcher, RenderRegionCache cache) {
        ChunkCompileTask task = this.createCompileTask(cache);
        dispatcher.schedule(task);
    }
    
    void updateGlobalBlockEntities(Collection<BlockEntity> p_234466_) {
        Set<BlockEntity> set = Sets.newHashSet(p_234466_);
        Set<BlockEntity> set1;
        synchronized (this.globalBlockEntities) {
            set1 = Sets.newHashSet(this.globalBlockEntities);
            set.removeAll(this.globalBlockEntities);
            set1.removeAll(p_234466_);
            this.globalBlockEntities.clear();
            this.globalBlockEntities.addAll(p_234466_);
        }
        
        renderer.updateGlobalBlockEntities(set1, set);
    }
    
    public void compileSync(RenderRegionCache p_200440_) {
        ChunkCompileTask task = this.createCompileTask(p_200440_);
        task.doTask(fixedBuffers);
    }
    
    private static enum ChunkTaskResult {
        SUCCESSFUL,
        CANCELLED;
    }
    
    private abstract class ChunkCompileTask implements Comparable<ChunkCompileTask> {
        
        protected final double distAtCreation;
        protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
        protected final boolean isHighPriority;
        protected Map<BlockPos, ModelData> modelData;
        
        public ChunkCompileTask(double p_194423_, boolean p_194424_) {
            this(null, p_194423_, p_194424_);
        }
        
        public ChunkCompileTask(@Nullable net.minecraft.world.level.ChunkPos pos, double distAtCreation, boolean isHighPriority) {
            this.distAtCreation = distAtCreation;
            this.isHighPriority = isHighPriority;
            this.modelData = pos == null ? java.util.Collections.emptyMap() : manager.level.getModelDataManager().getAt(pos);
        }
        
        public abstract CompletableFuture<ChunkTaskResult> doTask(ChunkBufferBuilderPack p_112853_);
        
        public abstract void cancel();
        
        protected abstract String name();
        
        @Override
        public int compareTo(ChunkCompileTask other) {
            return Doubles.compare(this.distAtCreation, other.distAtCreation);
        }
        
        public ModelData getModelData(BlockPos pos) {
            return modelData.getOrDefault(pos, ModelData.EMPTY);
        }
    }
    
    class RebuildTask extends ChunkCompileTask {
        @Nullable
        protected RenderChunkRegion region;
        
        @Deprecated
        public RebuildTask(@Nullable double p_194427_, RenderChunkRegion p_194428_, boolean p_194429_) {
            this(null, p_194427_, p_194428_, p_194429_);
        }
        
        public RebuildTask(@Nullable net.minecraft.world.level.ChunkPos pos, double p_194427_, @Nullable RenderChunkRegion p_194428_, boolean p_194429_) {
            super(pos, p_194427_, p_194429_);
            this.region = p_194428_;
        }
        
        @Override
        protected String name() {
            return "rend_chk_rebuild";
        }
        
        @Override
        public CompletableFuture<ChunkTaskResult> doTask(ChunkBufferBuilderPack p_112872_) {
            if (this.isCancelled.get())
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            
            if (this.isCancelled.get())
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            
            if (!LittleRenderChunk.this.hasAllNeighbors()) {
                this.region = null;
                LittleRenderChunk.this.setDirty(false);
                this.isCancelled.set(true);
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            }
            
            Vec3 vec3 = this.getCameraPosition();
            float f = (float) vec3.x;
            float f1 = (float) vec3.y;
            float f2 = (float) vec3.z;
            RebuildTask.CompileResults results = this.compile(f, f1, f2, p_112872_);
            LittleRenderChunk.this.updateGlobalBlockEntities(results.globalBlockEntities);
            if (this.isCancelled.get()) {
                results.renderedLayers.values().forEach(BufferBuilder.RenderedBuffer::release);
                return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
            }
            CompiledChunk compiled = new CompiledChunk();
            compiled.visibilitySet = results.visibilitySet;
            compiled.renderableBlockEntities.addAll(results.blockEntities);
            compiled.transparencyState = results.transparencyState;
            List<CompletableFuture<Void>> list = Lists.newArrayList();
            results.renderedLayers.forEach((layer, buffer) -> {
                list.add(ChunkRenderDispatcher.this.uploadChunkLayer(buffer, LittleRenderChunk.this.getBuffer(layer)));
                compiled.hasBlocks.add(layer);
            });
            return Util.sequenceFailFast(list).handle((p_234474_, p_234475_) -> {
                if (p_234475_ != null && !(p_234475_ instanceof CancellationException) && !(p_234475_ instanceof InterruptedException)) {
                    Minecraft.getInstance().delayCrash(CrashReport.forThrowable(p_234475_, "Rendering chunk"));
                }
                
                if (this.isCancelled.get()) {
                    return ChunkTaskResult.CANCELLED;
                } else {
                    LittleRenderChunk.this.compiled.set(compiled);
                    LittleRenderChunk.this.initialCompilationCancelCount.set(0);
                    ChunkRenderDispatcher.this.renderer.addRecentlyCompiledChunk(LittleRenderChunk.this);
                    return ChunkTaskResult.SUCCESSFUL;
                }
            });
            
        }
        
        private CompileResults compile(float x, float y, float z, ChunkBufferBuilderPack pack) {
            CompileResults results = new CompileResults();
            int i = 1;
            BlockPos blockpos = RenderChunk.this.origin.immutable();
            BlockPos blockpos1 = blockpos.offset(15, 15, 15);
            VisGraph visgraph = new VisGraph();
            RenderChunkRegion renderchunkregion = this.region;
            this.region = null;
            PoseStack posestack = new PoseStack();
            if (renderchunkregion != null) {
                ModelBlockRenderer.enableCaching();
                Set<RenderType> set = new ReferenceArraySet<>(RenderType.chunkBufferLayers().size());
                RandomSource randomsource = RandomSource.create();
                BlockRenderDispatcher blockrenderdispatcher = Minecraft.getInstance().getBlockRenderer();
                
                for (BlockPos blockpos2 : BlockPos.betweenClosed(blockpos, blockpos1)) {
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
                        if (set.add(rendertype))
                            LittleRenderChunk.this.beginLayer(bufferbuilder);
                        
                        blockrenderdispatcher.renderLiquid(blockpos2, renderchunkregion, bufferbuilder, blockstate1, fluidstate);
                    }
                    
                    if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                        var model = blockrenderdispatcher.getBlockModel(blockstate);
                        var modelData = getModelData(blockpos2);
                        randomsource.setSeed(blockstate.getSeed(blockpos2));
                        for (RenderType rendertype2 : model.getRenderTypes(blockstate, randomsource, modelData)) {
                            BufferBuilder bufferbuilder2 = pack.builder(rendertype2);
                            if (set.add(rendertype2))
                                LittleRenderChunk.this.beginLayer(bufferbuilder2);
                            
                            posestack.pushPose();
                            posestack.translate(blockpos2.getX() & 15, blockpos2.getY() & 15, blockpos2.getZ() & 15);
                            blockrenderdispatcher.renderBatched(blockstate, blockpos2, renderchunkregion, posestack, bufferbuilder2, true, randomsource, modelData, rendertype2);
                            posestack.popPose();
                        }
                    }
                }
                
                if (set.contains(RenderType.translucent())) {
                    BufferBuilder bufferbuilder1 = pack.builder(RenderType.translucent());
                    if (!bufferbuilder1.isCurrentBatchEmpty()) {
                        bufferbuilder1.setQuadSortOrigin(x - blockpos.getX(), y - blockpos.getY(), z - blockpos.getZ());
                        results.transparencyState = bufferbuilder1.getSortState();
                    }
                }
                
                for (RenderType rendertype1 : set) {
                    BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = pack.builder(rendertype1).endOrDiscardIfEmpty();
                    if (bufferbuilder$renderedbuffer != null)
                        results.renderedLayers.put(rendertype1, bufferbuilder$renderedbuffer);
                }
                
                ModelBlockRenderer.clearCache();
            }
            
            results.visibilitySet = visgraph.resolve();
            return results;
        }
        
        private <E extends BlockEntity> void handleBlockEntity(CompileResults results, E entity) {
            BlockEntityRenderer<E> blockentityrenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
            if (blockentityrenderer != null)
                if (blockentityrenderer.shouldRenderOffScreen(entity))
                    results.globalBlockEntities.add(entity);
                else
                    results.blockEntities.add(entity); //FORGE: Fix MC-112730
        }
        
        @Override
        public void cancel() {
            this.region = null;
            if (this.isCancelled.compareAndSet(false, true))
                LittleRenderChunk.this.setDirty(false);
            
        }
        
        @OnlyIn(Dist.CLIENT)
        static final class CompileResults {
            public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
            public final List<BlockEntity> blockEntities = new ArrayList<>();
            public final Map<RenderType, BufferBuilder.RenderedBuffer> renderedLayers = new Reference2ObjectArrayMap<>();
            public VisibilitySet visibilitySet = new VisibilitySet();
            @Nullable
            public BufferBuilder.SortState transparencyState;
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    class ResortTransparencyTask extends ChunkCompileTask {
        private final ChunkRenderDispatcher.CompiledChunk compiledChunk;
        
        @Deprecated
        public ResortTransparencyTask(double p_112889_, ChunkRenderDispatcher.CompiledChunk p_112890_) {
            this(null, p_112889_, p_112890_);
        }
        
        public ResortTransparencyTask(@Nullable net.minecraft.world.level.ChunkPos pos, double p_112889_, ChunkRenderDispatcher.CompiledChunk p_112890_) {
            super(pos, p_112889_, true);
            this.compiledChunk = p_112890_;
        }
        
        @Override
        protected String name() {
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
            
            Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
            float f = (float) vec3.x;
            float f1 = (float) vec3.y;
            float f2 = (float) vec3.z;
            BufferBuilder.SortState bufferbuilder$sortstate = this.compiledChunk.transparencyState;
            if (bufferbuilder$sortstate != null && !this.compiledChunk.isEmpty(RenderType.translucent())) {
                BufferBuilder bufferbuilder = p_112893_.builder(RenderType.translucent());
                LittleRenderChunk.this.beginLayer(bufferbuilder);
                bufferbuilder.restoreSortState(bufferbuilder$sortstate);
                bufferbuilder.setQuadSortOrigin(f - RenderChunk.this.origin.getX(), f1 - RenderChunk.this.origin.getY(), f2 - RenderChunk.this.origin.getZ());
                this.compiledChunk.transparencyState = bufferbuilder.getSortState();
                BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = bufferbuilder.end();
                if (this.isCancelled.get()) {
                    bufferbuilder$renderedbuffer.release();
                    return CompletableFuture.completedFuture(ChunkTaskResult.CANCELLED);
                }
                CompletableFuture<ChunkTaskResult> completablefuture = ChunkRenderDispatcher.this
                        .uploadChunkLayer(bufferbuilder$renderedbuffer, LittleRenderChunk.this.getBuffer(RenderType.translucent())).thenApply(x -> ChunkTaskResult.CANCELLED);
                return completablefuture.handle((p_234491_, p_234492_) -> {
                    if (p_234492_ != null && !(p_234492_ instanceof CancellationException) && !(p_234492_ instanceof InterruptedException)) {
                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(p_234492_, "Rendering chunk"));
                    }
                    
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
