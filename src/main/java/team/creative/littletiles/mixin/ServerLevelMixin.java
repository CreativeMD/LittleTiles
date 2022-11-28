package team.creative.littletiles.mixin;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.datafixers.DataFixer;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import team.creative.littletiles.server.level.little.LittleServerChunkCache;
import team.creative.littletiles.server.level.little.LittleServerLevel;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    
    @Redirect(at = @At(value = "NEW", target = "net/minecraft/server/level/ServerChunkCache"), method = "<init>", require = 1)
    public ServerChunkCache newServerChunkCache(ServerLevel level, LevelStorageAccess storageAccess, DataFixer dataFixer, StructureTemplateManager structureTemplate, Executor exe, ChunkGenerator generator, int viewDistance, int simulationDistance, boolean sync, ChunkProgressListener progress, ChunkStatusUpdateListener status, Supplier<DimensionDataStorage> supplier) {
        if (level instanceof LittleServerLevel)
            return new LittleServerChunkCache((LittleServerLevel) level, storageAccess, dataFixer, structureTemplate, exe, generator, viewDistance, simulationDistance, sync, progress, status, supplier);
        return new ServerChunkCache(level, storageAccess, dataFixer, structureTemplate, exe, generator, viewDistance, simulationDistance, sync, progress, status, supplier);
    }
    
}
