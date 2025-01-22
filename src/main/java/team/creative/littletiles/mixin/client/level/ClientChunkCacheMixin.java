package team.creative.littletiles.mixin.client.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkSource;
import team.creative.littletiles.common.block.entity.BETiles;

@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin extends ChunkSource {
    
    @Inject(method = "onLightUpdate(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/SectionPos;)V", at = @At("HEAD"), require = 1)
    public void onLightUpdate(LightLayer layer, SectionPos pos, CallbackInfo info) {
        var chunk = getChunkNow(pos.getX(), pos.getZ());
        if (chunk != null)
            for (BlockEntity be : chunk.getBlockEntities().values())
                if (be instanceof BETiles t)
                    t.render.hasLightChanged = true;
    }
    
}
