package team.creative.littletiles.mixin;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
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
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.server.level.little.LittleChunkMap;
import team.creative.littletiles.server.level.little.LittleServerChunkCache;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin extends ChunkSource {
    
    private static final String INIT_DESC = "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/chunk/ChunkGenerator;IIZLnet/minecraft/server/level/progress/ChunkProgressListener;Lnet/minecraft/world/level/entity/ChunkStatusUpdateListener;Ljava/util/function/Supplier;)Lnet/minecraft/server/level/ServerChunkCache;";
    
    public ServerChunkCache as() {
        return (ServerChunkCache) (Object) this;
    }
    
    @Redirect(at = @At(value = "NEW",
            target = "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;Ljava/util/concurrent/Executor;Lnet/minecraft/util/thread/BlockableEventLoop;Lnet/minecraft/world/level/chunk/LightChunkGetter;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/server/level/progress/ChunkProgressListener;Lnet/minecraft/world/level/entity/ChunkStatusUpdateListener;Ljava/util/function/Supplier;IZ)Lnet/minecraft/server/level/ChunkMap;"),
            method = INIT_DESC, require = 1)
    public ChunkMap newChunkMap(ServerLevel level, LevelStorageSource.LevelStorageAccess access, DataFixer fixer, StructureTemplateManager templateManager, Executor exe, BlockableEventLoop<Runnable> loop, LightChunkGetter lightGetter, ChunkGenerator generator, ChunkProgressListener progress, ChunkStatusUpdateListener status, Supplier<DimensionDataStorage> supplier, int viewDistance, boolean sync) {
        if (as() instanceof LittleServerChunkCache cache)
            try {
                LittleChunkMap map = (LittleChunkMap) LittleTiles.getUnsafe().allocateInstance(LittleChunkMap.class);
                map.init(level, cache, lightGetter, exe);
                return map;
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        return new ChunkMap(level, access, fixer, templateManager, exe, loop, lightGetter, generator, progress, status, supplier, viewDistance, sync);
    }
    
}
