package team.creative.littletiles.mixin.server.level;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.datafixers.DataFixer;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import team.creative.creativecore.common.util.unsafe.CreativeHackery;
import team.creative.littletiles.server.level.little.LittleChunkMap;
import team.creative.littletiles.server.level.little.LittleServerChunkCache;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin extends ChunkSource {
    
    @Unique
    private ServerChunkCache as() {
        return (ServerChunkCache) (Object) this;
    }
    
    @Redirect(at = @At(value = "NEW", target = "net/minecraft/server/level/ChunkMap"), method = "<init>", require = 1)
    public ChunkMap newChunkMap(ServerLevel level, LevelStorageSource.LevelStorageAccess access, DataFixer fixer, StructureTemplateManager templateManager, Executor exe, BlockableEventLoop<Runnable> loop, LightChunkGetter lightGetter, ChunkGenerator generator, ChunkProgressListener progress, ChunkStatusUpdateListener status, Supplier<DimensionDataStorage> supplier, int viewDistance, boolean sync) {
        if (as() instanceof LittleServerChunkCache cache) {
            LittleChunkMap map = CreativeHackery.allocateInstance(LittleChunkMap.class);
            map.init(level, cache, lightGetter, exe);
            return map;
        }
        return new ChunkMap(level, access, fixer, templateManager, exe, loop, lightGetter, generator, progress, status, supplier, viewDistance, sync);
    }
    
}
