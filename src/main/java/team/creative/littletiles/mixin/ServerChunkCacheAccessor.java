package team.creative.littletiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.level.ServerChunkCache;

@Mixin(ServerChunkCache.class)
public interface ServerChunkCacheAccessor {
    
    @Accessor
    public Thread getMainThread();
    
}
