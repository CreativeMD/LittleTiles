package team.creative.littletiles.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.KeyboardHandler;
import team.creative.littletiles.client.LittleTilesClient;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    
    @Inject(method = "keyPress(JIIII)V", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/InputConstants;getKey(II)Lcom/mojang/blaze3d/platform/InputConstants$Key;"), cancellable = true)
    private void onKeyPressed(long window, int keyCode, int scanCode, int action, int modifiers, CallbackInfo info) {
        if (LittleTilesClient.PREVIEW_RENDERER != null && LittleTilesClient.PREVIEW_RENDERER.keyPressed(keyCode, scanCode, action, modifiers))
            info.cancel();
    }
    
}
