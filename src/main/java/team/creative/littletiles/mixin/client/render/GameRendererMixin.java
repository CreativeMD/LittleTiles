package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.common.math.vec.LittleHitResult;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    
    @Redirect(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getLocation()Lnet/minecraft/world/phys/Vec3;"), require = 1)
    private Vec3 hitLocation(HitResult hit) {
        if (hit instanceof LittleHitResult result)
            return result.getRealLocation();
        return hit.getLocation();
    }
    
}
