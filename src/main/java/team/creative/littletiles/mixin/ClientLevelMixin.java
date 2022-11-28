package team.creative.littletiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.level.little.LittleClientChunkCache;
import team.creative.littletiles.client.level.little.LittleClientLevel;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    
    @Redirect(at = @At(value = "NEW", target = "net/minecraft/client/multiplayer/ClientChunkCache"), method = "<init>", require = 1)
    public ClientChunkCache newClientChunkCache(ClientLevel level, int distance) {
        if (level instanceof LittleClientLevel little) {
            try {
                LittleClientChunkCache cache = (LittleClientChunkCache) LittleTiles.getUnsafe().allocateInstance(LittleClientChunkCache.class);
                cache.init(little);
                return cache;
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        return new ClientChunkCache(level, distance);
    }
    
}
