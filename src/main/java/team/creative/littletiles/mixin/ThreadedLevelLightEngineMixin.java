package team.creative.littletiles.mixin;

import java.util.function.IntSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;

@Mixin(ThreadedLevelLightEngine.class)
public class ThreadedLevelLightEngineMixin {
    
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;getChunkQueueLevel(J)Ljava/util/function/IntSupplier;"),
            method = "addTask(IILnet/minecraft/server/level/ThreadedLevelLightEngine$TaskType;Ljava/lang/Runnable;)V", require = 1)
    public IntSupplier getChunkQueueLevel(ChunkMap map, long pos) {
        if (map == null)
            return () -> ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1;
        return ((ChunkMapAccessor) map).callGetChunkQueueLevel(pos);
    }
    
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;releaseLightTicket(Lnet/minecraft/world/level/ChunkPos;)V"),
            method = "lightChunk(Lnet/minecraft/world/level/chunk/ChunkAccess;Z)Ljava/util/concurrent/CompletableFuture;", require = 1)
    public void releaseLightTicket(ChunkMap map, ChunkPos pos) {
        if (map != null)
            ((ChunkMapAccessor) map).callReleaseLightTicket(pos);
    }
    
}
