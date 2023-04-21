package team.creative.littletiles.mixin.client.level;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import team.creative.creativecore.common.util.unsafe.CreativeHackery;
import team.creative.littletiles.client.level.ClientLevelExtender;
import team.creative.littletiles.client.level.little.LittleClientChunkCache;
import team.creative.littletiles.client.level.little.LittleClientLevel;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements ClientLevelExtender {
    
    @Shadow
    @Final
    private TransientEntitySectionManager<Entity> entityStorage;
    
    @Shadow
    @Final
    private BlockStatePredictionHandler blockStatePredictionHandler;
    
    @Redirect(at = @At(value = "NEW", target = "net/minecraft/client/multiplayer/ClientChunkCache"), method = "<init>", require = 1)
    public ClientChunkCache newClientChunkCache(ClientLevel level, int distance) {
        if (level instanceof LittleClientLevel little) {
            LittleClientChunkCache cache = CreativeHackery.allocateInstance(LittleClientChunkCache.class);
            cache.init(little);
            return cache;
        }
        return new ClientChunkCache(level, distance);
    }
    
    @Override
    public TransientEntitySectionManager getEntityStorage() {
        return entityStorage;
    }
    
    @Override
    public BlockStatePredictionHandler blockStatePredictionHandler() {
        return blockStatePredictionHandler;
    }
    
}
