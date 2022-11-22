package team.creative.littletiles.mixin;

import java.util.function.IntSupplier;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;

@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {
    
    @Invoker
    public ThreadedLevelLightEngine callGetLightEngine();
    
    @Invoker
    public Iterable<ChunkHolder> callGetChunks();
    
    @Invoker
    public IntSupplier callGetChunkQueueLevel(long pos);
    
    @Invoker
    public void callReleaseLightTicket(ChunkPos pos);
    
    @Invoker
    public ChunkHolder callUpdateChunkScheduling(long pos, int newTicketLevel, @Nullable ChunkHolder holder, int oldTicketLevel);
    
    @Accessor
    public LongSet getToDrop();
    
}
