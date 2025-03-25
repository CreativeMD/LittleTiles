package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.MeshData;

import team.creative.littletiles.client.render.mc.MeshDataExtender;

@Mixin(MeshData.class)
public class MeshDataMixin implements MeshDataExtender {
    
    @Unique
    private boolean keepAlive = false;
    
    @Inject(method = "close()V", at = @At("HEAD"), cancellable = true, require = 1)
    public void preventClose(CallbackInfo info) {
        if (keepAlive)
            info.cancel();
    }
    
    @Override
    public void keepAlive(boolean alive) {
        this.keepAlive = alive;
    }
    
}
