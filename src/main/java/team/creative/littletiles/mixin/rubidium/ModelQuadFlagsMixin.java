package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFlags;
import net.minecraft.core.Direction;
import team.creative.creativecore.client.render.model.CreativeBakedQuad;

@Mixin(ModelQuadFlags.class)
public class ModelQuadFlagsMixin {
    
    @Inject(method = "getQuadFlags", at = @At("RETURN"), cancellable = true, remap = false)
    private static void getQuadFlags(ModelQuadView quad, Direction face, CallbackInfoReturnable<Integer> info) {
        if (quad instanceof CreativeBakedQuad c && quad.getX(2) == quad.getX(3) && quad.getY(2) == quad.getY(3) && quad.getZ(2) == quad.getZ(3))
            info.setReturnValue(info.getReturnValueI() | ModelQuadFlags.IS_PARTIAL);
    }
    
}
