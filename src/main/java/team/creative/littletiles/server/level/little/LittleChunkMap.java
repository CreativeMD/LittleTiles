package team.creative.littletiles.server.level.little;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DataFixer;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import team.creative.creativecore.common.util.unsafe.CreativeHackery;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.level.little.LittleChunkSerializer;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.packet.entity.LittleVanillaPacket;

public class LittleChunkMap extends ChunkMap {
    
    private LittleServerChunkCache chunkCache;
    private ChunkScanAccess fakeAccess;
    private DistanceManager distanceManager;
    private ThreadedLevelLightEngine lightEngine;
    private ChunkTaskPriorityQueueSorter queueSorter;
    
    public LittleChunkMap(ServerLevel level, LevelStorageSource.LevelStorageAccess access, DataFixer fixer, StructureTemplateManager templateManager, Executor exe, BlockableEventLoop<Runnable> loop, LightChunkGetter lightGetter, ChunkGenerator generator, ChunkProgressListener progress, ChunkStatusUpdateListener status, Supplier<DimensionDataStorage> supplier, int viewDistance, boolean sync) {
        super(level, access, fixer, templateManager, exe, loop, lightGetter, generator, progress, status, supplier, viewDistance, sync);
    }
    
    public void init(ServerLevel level, LittleServerChunkCache chunkCache, LightChunkGetter lightGetter, Executor exe) {
        this.chunkCache = chunkCache;
        this.fakeAccess = (pos, visitor) -> CompletableFuture.runAsync(() -> {});
        ProcessorMailbox<Runnable> mailLight = ProcessorMailbox.create(exe, "light");
        this.queueSorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(mailLight), exe, Integer.MAX_VALUE);
        this.lightEngine = new ThreadedLevelLightEngine(lightGetter, this, level.dimensionType().hasSkyLight(), mailLight, queueSorter.getProcessor(mailLight, false));
        this.distanceManager = CreativeHackery.allocateInstance(LittleDistanceManager.class);
    }
    
    public LittleChunkHolder addChunkLevel(LittleChunkHolder holder) {
        holder.chunk.setLoaded(true);
        holder.chunk.registerAllBlockEntitiesAfterLevelLoad();
        holder.chunk.registerTickContainerInLevel(this.chunkCache.level);
        return holder;
    }
    
    public LittleChunkHolder createHolder(ChunkPos pos) {
        return addChunkLevel(new LittleChunkHolder(chunkCache.level, pos, this.lightEngine));
    }
    
    public LittleChunkHolder createHolder(ChunkPos pos, CompoundTag nbt) {
        return addChunkLevel(new LittleChunkHolder(LittleChunkSerializer.read((LittleLevel) chunkCache.level, nbt), this.lightEngine));
    }
    
    public ClientboundLevelChunkWithLightPacket createPacket(LevelChunk chunk) {
        return new ClientboundLevelChunkWithLightPacket(chunk, this.lightEngine, (BitSet) null, (BitSet) null);
    }
    
    @Override
    protected ChunkHolder getUpdatingChunkIfPresent(long pos) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ChunkHolder getVisibleChunkIfPresent(long pos) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected IntSupplier getChunkQueueLevel(long pos) {
        return () -> ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1;
    }
    
    @Override
    public ThreadedLevelLightEngine getLightEngine() {
        return lightEngine;
    }
    
    @Override
    public String getChunkDebugData(ChunkPos pos) {
        return "";
    }
    
    @Override
    public ReportedException debugFuturesAndCreateReportedException(IllegalStateException exception, String message) {
        CrashReport crashreport = CrashReport.forThrowable(exception, "Chunk loading");
        CrashReportCategory crashreportcategory = crashreport.addCategory("Chunk loading");
        crashreportcategory.setDetail("Details", message);
        return new ReportedException(crashreport);
    }
    
    @Override
    public void close() throws IOException {}
    
    @Override
    public boolean hasWork() {
        return false;
    }
    
    @Override
    /** the holder class has no use in this scenario, so all methods related to it are empty */
    public CompletableFuture<ChunkResult<LevelChunk>> prepareEntityTickingChunk(ChunkHolder holder) {
        return null;
    }
    
    @Override
    public CompletableFuture<ChunkAccess> applyStep(GenerationChunkHolder holder, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache) {
        return null;
    }
    
    @Override
    /** the holder class has no use in this scenario, so all methods related to it are empty */
    public CompletableFuture<ChunkResult<LevelChunk>> prepareTickingChunk(ChunkHolder holder) {
        return null;
    }
    
    @Override
    /** the holder class has no use in this scenario, so all methods related to it are empty */
    public CompletableFuture<ChunkResult<LevelChunk>> prepareAccessibleChunk(ChunkHolder p_143110_) {
        return null;
    }
    
    @Override
    public int getTickingGenerated() {
        return 0;
    }
    
    @Override
    public int size() {
        return chunkCache.getLoadedChunksCount();
    }
    
    @Override
    public DistanceManager getDistanceManager() {
        return this.distanceManager;
    }
    
    @Override
    public List<ServerPlayer> getPlayersCloseForSpawning(ChunkPos pos) {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public void move(ServerPlayer player) {}
    
    @Override
    public List<ServerPlayer> getPlayers(ChunkPos pos, boolean all) {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public String getStorageName() {
        return "";
    }
    
    @Override
    public boolean isOldChunkAround(ChunkPos pos, int range) {
        return false;
    }
    
    @Override
    public CompletableFuture<Optional<CompoundTag>> read(ChunkPos pos) {
        return null;
    }
    
    @Override
    public CompletableFuture<Void> write(ChunkPos pos, CompoundTag tag) {
        return null;
    }
    
    @Override
    public void flushWorker() {}
    
    @Override
    public ChunkScanAccess chunkScanner() {
        return fakeAccess;
    }
    
    @Override
    public void tick() {}
    
    @Override
    public void removeEntity(Entity entity) {}
    
    @Override
    public void addEntity(Entity entity) {}
    
    @Override
    protected void broadcastAndSend(Entity entity, Packet<?> packet) {
        LittleLevel level = (LittleLevel) chunkCache.getLevel();
        LittleTiles.NETWORK.sendToClientTrackingAndSelf(new LittleVanillaPacket(level, packet), level.getHolder());
    }
    
    @Override
    public void broadcast(Entity entity, Packet<?> packet) {
        LittleLevel level = (LittleLevel) chunkCache.getLevel();
        LittleTiles.NETWORK.sendToClientTracking(new LittleVanillaPacket(level, packet), level.getHolder());
    }
    
}
