package team.creative.littletiles.server.level.little;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.mixin.ServerChunkCacheAccessor;

public class LittleServerChunkCache extends ServerChunkCache {
    
    private final ChunkTaskPriorityQueueSorter queueSorter;
    private final HashMap<Long, LevelChunk> chunks = new HashMap<>();
    
    public LittleServerChunkCache(LittleServerLevel level, LevelStorageAccess storageAccess, DataFixer dataFixer, StructureTemplateManager structureTemplate, Executor exe, ChunkGenerator generator, int viewDistance, int simulationDistance, boolean sync, ChunkProgressListener progress, ChunkStatusUpdateListener status, Supplier<DimensionDataStorage> supplier) {
        super(level, storageAccess, dataFixer, structureTemplate, exe, generator, viewDistance, simulationDistance, sync, progress, status, supplier);
        ProcessorMailbox<Runnable> mailbox = ProcessorMailbox.create(exe, "light");
        this.queueSorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(mailbox), exe, Integer.MAX_VALUE);
        ((ServerChunkCacheAccessor) this)
                .setLightEngine(new ThreadedLevelLightEngine(this, null, level.dimensionType().hasSkyLight(), mailbox, queueSorter.getProcessor(mailbox, false)));
    }
    
    @Override
    @Nullable
    public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean create) {
        LevelChunk chunk = chunks.get(ChunkPos.asLong(x, z));
        if (chunk == null && create)
            chunks.put(ChunkPos.asLong(x, z), chunk = new LevelChunk(level, new ChunkPos(x, z)));
        return chunk;
    }
    
    @Override
    @Nullable
    public LevelChunk getChunkNow(int x, int z) {
        return getChunk(x, z, false);
    }
    
    @Override
    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture(int x, int z, ChunkStatus status, boolean create) {
        return ChunkHolder.UNLOADED_CHUNK_FUTURE;
    }
    
    @Override
    public boolean hasChunk(int x, int z) {
        return chunks.containsKey(ChunkPos.asLong(x, z));
    }
    
    @Override
    public int getTickingGenerated() {
        return 
     }
    
    @Override
    public BlockGetter getChunkForLighting(int x, int z) {
        return getChunk(x, z, false);
    }
    
    @Override
    public boolean isPositionTicking(long index) {
        LevelChunk chunk = chunks.get(index);
        if (chunkholder == null)
            return false;
        else if (!this.level.shouldTickBlocksAt(index)) {
            return false;
        } else {
            Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = chunkholder.getTickingChunkFuture().getNow((Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>) null);
            return either != null && either.left().isPresent();
        }
    }
    
    @Override
    public void save(boolean p_8420_) {
        
    }
    
    @Override
    public void close() throws IOException {
        
    }
    
    @Override
    public void tick(BooleanSupplier p_201913_, boolean p_201914_) {
        long i = this.level.getGameTime();
        long j = i - this.lastInhabitedUpdate;
        this.lastInhabitedUpdate = i;
        boolean flag = this.level.isDebug();
        if (flag) {
            this.chunkMap.tick();
        } else {
            LevelData leveldata = this.level.getLevelData();
            ProfilerFiller profilerfiller = this.level.getProfiler();
            profilerfiller.push("pollingChunks");
            int k = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
            boolean flag1 = leveldata.getGameTime() % 400L == 0L;
            profilerfiller.push("naturalSpawnCount");
            int l = this.distanceManager.getNaturalSpawnChunkCount();
            NaturalSpawner.SpawnState naturalspawner$spawnstate = NaturalSpawner
                    .createState(l, this.level.getAllEntities(), this::getFullChunk, new LocalMobCapCalculator(this.chunkMap));
            this.lastSpawnState = naturalspawner$spawnstate;
            profilerfiller.popPush("filteringLoadedChunks");
            List<ServerChunkCache.ChunkAndHolder> list = Lists.newArrayListWithCapacity(l);
            
            for (ChunkHolder chunkholder : this.chunkMap.getChunks()) {
                LevelChunk levelchunk = chunkholder.getTickingChunk();
                if (levelchunk != null) {
                    list.add(new ServerChunkCache.ChunkAndHolder(levelchunk, chunkholder));
                }
            }
            
            profilerfiller.popPush("spawnAndTick");
            boolean flag2 = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
            Collections.shuffle(list);
            
            for (ServerChunkCache.ChunkAndHolder serverchunkcache$chunkandholder : list) {
                LevelChunk levelchunk1 = serverchunkcache$chunkandholder.chunk;
                ChunkPos chunkpos = levelchunk1.getPos();
                if ((this.level.isNaturalSpawningAllowed(chunkpos) && this.chunkMap.anyPlayerCloseEnoughForSpawning(chunkpos)) || this.distanceManager
                        .shouldForceTicks(chunkpos.toLong())) {
                    levelchunk1.incrementInhabitedTime(j);
                    if (flag2 && (this.spawnEnemies || this.spawnFriendlies) && this.level.getWorldBorder().isWithinBounds(chunkpos)) {
                        NaturalSpawner.spawnForChunk(this.level, levelchunk1, naturalspawner$spawnstate, this.spawnFriendlies, this.spawnEnemies, flag1);
                    }
                    
                    if (this.level.shouldTickBlocksAt(chunkpos.toLong())) {
                        this.level.tickChunk(levelchunk1, k);
                    }
                }
            }
            
            profilerfiller.popPush("customSpawners");
            if (flag2) {
                this.level.tickCustomSpawners(this.spawnEnemies, this.spawnFriendlies);
            }
            
            profilerfiller.popPush("broadcast");
            list.forEach((p_184022_) -> {
                p_184022_.holder.broadcastChanges(p_184022_.chunk);
            });
            profilerfiller.pop();
            profilerfiller.pop();
            this.chunkMap.tick();
        }
    }
    
    @Override
    public ChunkGenerator getGenerator() {
        return this.chunkMap.generator();
    }
    
    @Override
    public RandomState randomState() {
        return this.chunkMap.randomState();
    }
    
    @Override
    public int getLoadedChunksCount() {
        return this.chunkMap.size();
    }
    
    @Override
    public void blockChanged(BlockPos p_8451_) {
        int i = SectionPos.blockToSectionCoord(p_8451_.getX());
        int j = SectionPos.blockToSectionCoord(p_8451_.getZ());
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(ChunkPos.asLong(i, j));
        if (chunkholder != null) {
            chunkholder.blockChanged(p_8451_);
        }
        
    }
    
    @Override
    public void onLightUpdate(LightLayer p_8403_, SectionPos p_8404_) {
        this.mainThreadProcessor.execute(() -> {
            ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_8404_.chunk().toLong());
            if (chunkholder != null) {
                chunkholder.sectionLightChanged(p_8403_, p_8404_.y());
            }
            
        });
    }
    
    @Override
    public <T> void addRegionTicket(TicketType<T> p_8388_, ChunkPos p_8389_, int p_8390_, T p_8391_) {
        addRegionTicket(p_8388_, p_8389_, p_8390_, p_8391_, false);
    }
    
    @Override
    public <T> void addRegionTicket(TicketType<T> p_8388_, ChunkPos p_8389_, int p_8390_, T p_8391_, boolean forceTicks) {
        this.distanceManager.addRegionTicket(p_8388_, p_8389_, p_8390_, p_8391_, forceTicks);
    }
    
    @Override
    public <T> void removeRegionTicket(TicketType<T> p_8439_, ChunkPos p_8440_, int p_8441_, T p_8442_) {
        removeRegionTicket(p_8439_, p_8440_, p_8441_, p_8442_, false);
    }
    
    @Override
    public <T> void removeRegionTicket(TicketType<T> p_8439_, ChunkPos p_8440_, int p_8441_, T p_8442_, boolean forceTicks) {
        this.distanceManager.removeRegionTicket(p_8439_, p_8440_, p_8441_, p_8442_, forceTicks);
    }
    
    @Override
    public void updateChunkForced(ChunkPos p_8400_, boolean p_8401_) {
        this.distanceManager.updateChunkForced(p_8400_, p_8401_);
    }
    
    @Override
    public void move(ServerPlayer p_8386_) {
        if (!p_8386_.isRemoved()) {
            this.chunkMap.move(p_8386_);
        }
        
    }
    
    @Override
    public void removeEntity(Entity p_8444_) {
        this.chunkMap.removeEntity(p_8444_);
    }
    
    @Override
    public void addEntity(Entity p_8464_) {
        this.chunkMap.addEntity(p_8464_);
    }
    
    @Override
    public void broadcastAndSend(Entity p_8395_, Packet<?> p_8396_) {
        this.chunkMap.broadcastAndSend(p_8395_, p_8396_);
    }
    
    @Override
    public void broadcast(Entity p_8446_, Packet<?> p_8447_) {
        this.chunkMap.broadcast(p_8446_, p_8447_);
    }
    
    @Override
    public void setViewDistance(int p_8355_) {
        this.chunkMap.setViewDistance(p_8355_);
    }
    
    @Override
    public void setSimulationDistance(int p_184027_) {
        this.distanceManager.updateSimulationDistance(p_184027_);
    }
    
    @Override
    public void setSpawnSettings(boolean p_8425_, boolean p_8426_) {
        this.spawnEnemies = p_8425_;
        this.spawnFriendlies = p_8426_;
    }
    
    @Override
    public String getChunkDebugData(ChunkPos p_8449_) {
        return this.chunkMap.getChunkDebugData(p_8449_);
    }
    
    @Override
    public DimensionDataStorage getDataStorage() {
        return this.dataStorage;
    }
    
    @Override
    public PoiManager getPoiManager() {
        return this.chunkMap.getPoiManager();
    }
    
    @Override
    public ChunkScanAccess chunkScanner() {
        return this.chunkMap.chunkScanner();
    }
    
    @Override
    @Nullable
    @VisibleForDebug
    public NaturalSpawner.SpawnState getLastSpawnState() {
        return this.lastSpawnState;
    }
    
    @Override
    public void removeTicketsOnClosing() {
        this.distanceManager.removeTicketsOnClosing();
    }
    
    public void addLoadedChunk(LevelChunk chunk) {
        chunks.put(chunk.getPos().toLong(), chunk);
        ((LittleLevel) level).onChunkLoaded(chunk);
    }
    
    public Iterable<LevelChunk> all() {
        return chunks.values();
    }
    
}
