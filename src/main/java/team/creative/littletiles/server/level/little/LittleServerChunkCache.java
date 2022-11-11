package team.creative.littletiles.server.level.little;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import com.mojang.datafixers.DataFixer;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import team.creative.littletiles.common.level.little.LittleLevel;

public class LittleServerChunkCache extends ServerChunkCache {
    
    public LittleServerChunkCache(LittleServerLevel level, LevelStorageAccess storageAccess, DataFixer dataFixer, StructureTemplateManager structureTemplate, Executor exe, ChunkGenerator generator, int p_214988_, int p_214989_, boolean p_214990_, ChunkProgressListener p_214991_, ChunkStatusUpdateListener p_214992_, Supplier<DimensionDataStorage> p_214993_) {
        super(level, storageAccess, dataFixer, structureTemplate, exe, generator, p_214988_, p_214989_, p_214990_, p_214991_, p_214992_, p_214993_);
    }
    
    public void addLoadedChunk(LevelChunk chunk) {
        ((LittleLevel) level).onChunkLoaded(chunk);
    }
    
    public Iterable<? extends ChunkAccess> all() {
        return null;
    }
    
}
