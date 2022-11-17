package team.creative.littletiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ThreadedLevelLightEngine;

@Mixin(ServerChunkCache.class)
public interface ServerChunkCacheAccessor {
    
    @Accessor
    public ThreadedLevelLightEngine getLightEngine();
    
    @Accessor
    public void setLightEngine(ThreadedLevelLightEngine engine);
    
}
