package team.creative.littletiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.LevelRenderer;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    
    @Inject(at = @At("HEAD"), method = "allChanged()V")
    public void allChanged(CallbackInfo info) {
        LittleChunkDispatcher.onReloadRenderers((LevelRenderer) (Object) this);
    }
    
}
