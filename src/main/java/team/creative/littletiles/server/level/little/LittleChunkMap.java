package team.creative.littletiles.server.level.little;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;

public class LittleChunkMap extends ChunkMap {
    
    ChunkScanAccess fakeAccess = (pos, visitor) -> CompletableFuture.runAsync(() -> {});
    
    public LittleChunkMap(ServerLevel level, LevelStorageSource.LevelStorageAccess access, DataFixer fixer, StructureTemplateManager templateManager, Executor exe, BlockableEventLoop<Runnable> loop, LightChunkGetter lightGetter, ChunkGenerator generator, ChunkProgressListener progress, ChunkStatusUpdateListener status, Supplier<DimensionDataStorage> supplier, int viewDistance, boolean sync) {
        super(level, access, fixer, templateManager, exe, loop, lightGetter, generator, progress, status, supplier, viewDistance, sync);
    }
    
    @Override
    public void flushWorker() {}
    
    @Override
    public void close() throws IOException {}
    
    @Override
    public ChunkScanAccess chunkScanner() {
        return fakeAccess;
    }
    
    @Override
    public CompoundTag upgradeChunkTag(ResourceKey<Level> p_188289_, Supplier<DimensionDataStorage> p_188290_, CompoundTag p_188291_, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> p_188292_) {
        p_188291_.remove("__context");
        return p_188291_;
    }
    
}
