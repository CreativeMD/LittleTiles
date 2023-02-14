package team.creative.littletiles.mixin.server.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    
    @Accessor
    public LevelStorageSource.LevelStorageAccess getStorageSource();
    
    @Invoker
    public boolean callHaveTime();
}
