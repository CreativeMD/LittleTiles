package team.creative.littletiles.mixin;

import java.nio.file.Path;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import team.creative.littletiles.server.level.little.LittleChunkMap;
import team.creative.littletiles.server.level.little.LittleIOWorker;

@Mixin(ChunkStorage.class)
public class ChunkStorageMixin {
    
    public ChunkMap as() {
        return (ChunkMap) (Object) this;
    }
    
    @Redirect(at = @At(value = "NEW", target = "(Ljava/nio/file/Path;ZLjava/lang/String;)Lnet/minecraft/world/level/chunk/storage/IOWorker;"),
            method = "(Ljava/nio/file/Path;Lcom/mojang/datafixers/DataFixer;Z)Lnet/minecraft/world/level/chunk/storage/ChunkStorage;", require = 1)
    public IOWorker newIOWorker(Path path, boolean sync, String name) {
        if (as() instanceof LittleChunkMap)
            return new LittleIOWorker(path, sync, name);
        return IOWorkerAccessor.create(path, sync, name);
    }
    
}
