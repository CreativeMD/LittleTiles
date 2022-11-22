package team.creative.littletiles.server.level.little;

import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import com.mojang.datafixers.DataFixer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.creativecore.common.util.type.itr.FunctionIterator;
import team.creative.littletiles.mixin.ChunkMapAccessor;
import team.creative.littletiles.mixin.ChunkStorageAccessor;

public class LittleServerChunkCache extends ServerChunkCache {
    
    private final Iterable<LevelChunk> chunkIterable = new Iterable<LevelChunk>() {
        
        @Override
        public Iterator<LevelChunk> iterator() {
            return new FilterIterator<LevelChunk>(new FunctionIterator<LevelChunk>(((ChunkMapAccessor) chunkMap).callGetChunks()
                    .iterator(), x -> x.getFullChunk()), x -> x != null);
        }
    };
    
    public LittleServerChunkCache(LittleServerLevel level, LevelStorageAccess storageAccess, DataFixer dataFixer, StructureTemplateManager structureTemplate, Executor exe, ChunkGenerator generator, int viewDistance, int simulationDistance, boolean sync, ChunkProgressListener progress, ChunkStatusUpdateListener status, Supplier<DimensionDataStorage> supplier) {
        super(level, storageAccess, dataFixer, structureTemplate, exe, generator, viewDistance, simulationDistance, sync, progress, status, supplier);
    }
    
    public void addLevelChunkTag(ChunkPos pos, CompoundTag tag) {
        ((LittleIOWorker) ((ChunkStorageAccessor) chunkMap).getWorker()).add(pos, tag);
        ((ChunkMapAccessor) chunkMap).callUpdateChunkScheduling(pos.toLong(), 0, null, 0);
    }
    
    public Iterable<LevelChunk> all() {
        return chunkIterable;
    }
    
}
