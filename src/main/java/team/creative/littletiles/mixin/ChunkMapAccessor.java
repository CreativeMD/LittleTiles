package team.creative.littletiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;

@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {
    
    @Invoker
    public Iterable<ChunkHolder> callGetChunks();
    
}
