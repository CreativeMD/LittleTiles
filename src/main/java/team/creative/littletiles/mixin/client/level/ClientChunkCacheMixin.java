package team.creative.littletiles.mixin.client.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.lighting.LevelLightEngine;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.LittleEntity;

@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin extends ChunkSource {
    
    @Accessor
    @Mutable
    public abstract void setLevel(ClientLevel level);
    
    @Accessor
    @Mutable
    public abstract void setLightEngine(LevelLightEngine engine);
    
    @Inject(method = "onLightUpdate(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/SectionPos;)V", at = @At("HEAD"), require = 1)
    public void onLightUpdate(LightLayer layer, SectionPos pos, CallbackInfo info) {
        var chunk = getChunkNow(pos.getX(), pos.getZ());
        if (chunk != null) {
            for (BlockEntity be : chunk.getBlockEntities().values())
                if (be instanceof BETiles t)
                    t.render.hasLightChanged = true;
            for (LittleEntity entity : LittleTiles.ANIMATION_HANDLERS.get((Level) getLevel()).find(pos))
                entity.getRenderManager().onLightUpdate(layer, pos);
        }
    }
    
}
