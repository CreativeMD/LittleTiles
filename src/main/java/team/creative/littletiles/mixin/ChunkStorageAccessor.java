package team.creative.littletiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;

@Mixin(ChunkStorage.class)
public interface ChunkStorageAccessor {
    
    @Accessor
    public IOWorker getWorker();
    
}
