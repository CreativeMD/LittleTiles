package team.creative.littletiles.server.level.little;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.datafixers.DataFixer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import team.creative.creativecore.common.util.type.itr.FunctionIterator;
import team.creative.littletiles.mixin.server.level.ServerChunkCacheAccessor;

public class LittleServerChunkCache extends ServerChunkCache implements Iterable<LevelChunk> {
    
    private final HashMap<Long, LittleChunkHolder> chunks = new HashMap<>();
    private final MainThreadExecutor mainThreadProcessor;
    
    public LittleServerChunkCache(LittleServerLevel level, LevelStorageAccess storageAccess, DataFixer dataFixer, StructureTemplateManager structureTemplate, Executor exe, ChunkGenerator generator, int viewDistance, int simulationDistance, boolean sync, ChunkProgressListener progress, ChunkStatusUpdateListener status, Supplier<DimensionDataStorage> supplier) {
        super(level, storageAccess, dataFixer, structureTemplate, exe, generator, viewDistance, simulationDistance, sync, progress, status, supplier);
        mainThreadProcessor = new MainThreadExecutor(level);
    }
    
    @Override
    public int getTickingGenerated() {
        return this.chunkMap.getTickingGenerated();
    }
    
    private ChunkResult<ChunkAccess> getChunkMainThread(int x, int z, boolean create) {
        LittleChunkHolder holder = chunks.get(ChunkPos.asLong(x, z));
        if (holder == null && create)
            chunks.put(ChunkPos.asLong(x, z), holder = ((LittleChunkMap) chunkMap).createHolder(new ChunkPos(x, z)));
        return ChunkResult.of(holder.chunk);
    }
    
    @Override
    @Nullable
    public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean create) {
        if (Thread.currentThread() != ((ServerChunkCacheAccessor) this).getMainThread())
            return CompletableFuture.supplyAsync(() -> this.getChunk(x, z, status, create), this.mainThreadProcessor).join();
        LittleChunkHolder holder = chunks.get(ChunkPos.asLong(x, z));
        if (holder == null && create)
            chunks.put(ChunkPos.asLong(x, z), holder = ((LittleChunkMap) chunkMap).createHolder(new ChunkPos(x, z)));
        return holder == null ? null : holder.chunk;
    }
    
    @Override
    @Nullable
    public LevelChunk getChunkNow(int x, int z) {
        if (Thread.currentThread() != ((ServerChunkCacheAccessor) this).getMainThread())
            return null;
        return getChunk(x, z, true);
    }
    
    @Override
    public CompletableFuture<ChunkResult<ChunkAccess>> getChunkFuture(int x, int z, ChunkStatus status, boolean create) {
        CompletableFuture<ChunkResult<ChunkAccess>> completablefuture;
        if (Thread.currentThread() == ((ServerChunkCacheAccessor) this).getMainThread()) {
            completablefuture = CompletableFuture.supplyAsync(() -> getChunkMainThread(x, z, create));
            this.mainThreadProcessor.managedBlock(completablefuture::isDone);
        } else
            completablefuture = CompletableFuture.supplyAsync(() -> getChunkMainThread(x, z, create), this.mainThreadProcessor);
        return completablefuture;
    }
    
    @Override
    public boolean hasChunk(int x, int z) {
        return chunks.containsKey(ChunkPos.asLong(x, z));
    }
    
    @Override
    public LightChunk getChunkForLighting(int x, int z) {
        return getChunk(x, z, ChunkStatus.EMPTY, false);
    }
    
    @Override
    public boolean pollTask() {
        return mainThreadProcessor.pollTask();
    }
    
    @Override
    public boolean isPositionTicking(long pos) {
        if (!level.shouldTickBlocksAt(pos))
            return false;
        return chunks.containsKey(pos);
    }
    
    @Override
    public void save(boolean all) {}
    
    @Override
    public void close() throws IOException {}
    
    @Override
    public void tick(BooleanSupplier running, boolean chunks) {
        this.level.getProfiler().push("purge");
        ((LittleDistanceManager) this.chunkMap.getDistanceManager()).purgeStaleTickets();
        this.runDistanceManagerUpdates2();
        this.level.getProfiler().popPush("chunks");
        if (chunks)
            this.tickChunks();
        this.level.getProfiler().pop();
    }
    
    private void tickChunks() {
        if (this.level.isDebug()) {
            ((LittleChunkMap) chunkMap).tick();
            return;
        }
        
        ProfilerFiller profilerfiller = this.level.getProfiler();
        profilerfiller.push("pollingChunks");
        int k = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        
        profilerfiller.popPush("tick");
        for (Entry<Long, LittleChunkHolder> entry : chunks.entrySet())
            if (this.level.shouldTickBlocksAt(entry.getKey()))
                this.level.tickChunk(entry.getValue().chunk, k);
            
        for (LittleChunkHolder holder : chunks.values())
            holder.broadcastChanges();
        profilerfiller.pop();
        ((LittleChunkMap) chunkMap).tick();
    }
    
    @Override
    public int getPendingTasksCount() {
        return this.mainThreadProcessor.getPendingTasksCount();
    }
    
    @Override
    public ChunkGenerator getGenerator() {
        return null;
    }
    
    @Override
    public RandomState randomState() {
        return null;
    }
    
    @Override
    public int getLoadedChunksCount() {
        return chunks.size();
    }
    
    @Override
    public void blockChanged(BlockPos pos) {
        LittleChunkHolder chunkholder = chunks.get(ChunkPos.asLong(pos));
        if (chunkholder != null)
            chunkholder.blockChanged(pos);
    }
    
    @Override
    public void onLightUpdate(LightLayer layer, SectionPos pos) {
        this.mainThreadProcessor.execute(() -> {
            LittleChunkHolder chunkholder = chunks.get(ChunkPos.asLong(pos.x(), pos.z()));
            if (chunkholder != null)
                chunkholder.sectionLightChanged(layer, pos.y());
        });
    }
    
    @Override
    public <T> void addRegionTicket(TicketType<T> type, ChunkPos pos, int level, T key) {
        chunkMap.getDistanceManager().addRegionTicket(type, pos, level, key);
    }
    
    @Override
    public <T> void addRegionTicket(TicketType<T> type, ChunkPos pos, int level, T key, boolean forceTicks) {
        chunkMap.getDistanceManager().addRegionTicket(type, pos, level, key, forceTicks);
    }
    
    @Override
    public <T> void removeRegionTicket(TicketType<T> type, ChunkPos pos, int level, T key) {
        chunkMap.getDistanceManager().removeRegionTicket(type, pos, level, key);
    }
    
    @Override
    public <T> void removeRegionTicket(TicketType<T> type, ChunkPos pos, int level, T key, boolean forceTicks) {
        chunkMap.getDistanceManager().removeRegionTicket(type, pos, level, key, forceTicks);
    }
    
    @Override
    public void updateChunkForced(ChunkPos pos, boolean added) {
        ((LittleDistanceManager) chunkMap.getDistanceManager()).updateChunkForced(pos, added);
    }
    
    @Override
    public void move(ServerPlayer player) {
        ((LittleChunkMap) chunkMap).move(player);
    }
    
    @Override
    public void removeEntity(Entity entity) {
        ((LittleChunkMap) chunkMap).removeEntity(entity);
    }
    
    @Override
    public void addEntity(Entity entity) {
        ((LittleChunkMap) chunkMap).addEntity(entity);
    }
    
    @Override
    public void broadcastAndSend(Entity entity, Packet<?> packet) {
        ((LittleChunkMap) chunkMap).broadcastAndSend(entity, packet);
    }
    
    @Override
    public void broadcast(Entity entity, Packet<?> packet) {
        ((LittleChunkMap) chunkMap).broadcast(entity, packet);
    }
    
    @Override
    public void setViewDistance(int distance) {}
    
    @Override
    public void setSimulationDistance(int distance) {}
    
    @Override
    public void setSpawnSettings(boolean spawnEnemies, boolean spawnFriendlies) {}
    
    @Override
    public String getChunkDebugData(ChunkPos pos) {
        return "";
    }
    
    @Override
    public ChunkScanAccess chunkScanner() {
        return chunkMap.chunkScanner();
    }
    
    @Override
    @Nullable
    @VisibleForDebug
    public NaturalSpawner.SpawnState getLastSpawnState() {
        return null;
    }
    
    @Override
    public void removeTicketsOnClosing() {
        this.chunkMap.getDistanceManager().removeTicketsOnClosing();
    }
    
    public void loadLevelChunk(ChunkPos pos, CompoundTag tag) {
        chunks.put(pos.toLong(), ((LittleChunkMap) chunkMap).createHolder(pos, tag));
    }
    
    public Iterable<LevelChunk> all() {
        return this;
    }
    
    @Override
    public Iterator<LevelChunk> iterator() {
        return new FunctionIterator<>(chunks.values().iterator(), x -> x.chunk);
    }
    
    public boolean runDistanceManagerUpdates2() {
        this.chunkMap.getDistanceManager().runAllUpdates(this.chunkMap);
        return false;
    }
    
    @Override
    public ChunkGeneratorStructureState getGeneratorState() {
        return null;
    }
    
    public final class MainThreadExecutor extends BlockableEventLoop<Runnable> {
        
        public MainThreadExecutor(Level level) {
            super("Chunk source main thread executor for " + level.dimension().location());
        }
        
        @Override
        protected Runnable wrapRunnable(Runnable p_8506_) {
            return p_8506_;
        }
        
        @Override
        protected boolean shouldRun(Runnable p_8504_) {
            return true;
        }
        
        @Override
        protected boolean scheduleExecutables() {
            return true;
        }
        
        @Override
        protected Thread getRunningThread() {
            return ((ServerChunkCacheAccessor) LittleServerChunkCache.this).getMainThread();
        }
        
        @Override
        protected void doRunTask(Runnable run) {
            LittleServerChunkCache.this.level.getProfiler().incrementCounter("runTask");
            super.doRunTask(run);
        }
        
        @Override
        public boolean pollTask() {
            if (LittleServerChunkCache.this.runDistanceManagerUpdates2())
                return true;
            LittleServerChunkCache.this.getLightEngine().tryScheduleUpdate();
            return super.pollTask();
        }
    }
    
}
