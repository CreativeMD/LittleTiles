package team.creative.littletiles.mixin.client.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.lighting.LevelLightEngine;

@Mixin(ClientChunkCache.class)
public interface ClientChunkCacheAccessor {
    
    @Accessor
    @Mutable
    public void setLevel(ClientLevel level);
    
    @Accessor
    @Mutable
    public void setLightEngine(LevelLightEngine engine);
    
}
