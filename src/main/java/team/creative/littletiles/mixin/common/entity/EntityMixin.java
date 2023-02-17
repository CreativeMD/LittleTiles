package team.creative.littletiles.mixin.common.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.math.vec.LittleHitResult;

@Mixin(Entity.class)
public class EntityMixin {
    
    @Unique
    private Entity asEntity() {
        return (Entity) (Object) this;
    }
    
    @Inject(method = "pick", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true, require = 1)
    public void pick(double reach, float partialTicks, boolean fluid, CallbackInfoReturnable<HitResult> info, Vec3 pos, Vec3 view, Vec3 look) {
        Entity entity = asEntity();
        HitResult result = info.getReturnValue();
        double reachDistance = result != null ? pos.distanceTo(result.getLocation()) : (entity instanceof Player p ? PlayerUtils.getReach(p) : 4);
        LittleHitResult hit = LittleAnimationHandlers.get(entity.level).getHit(pos, look, reachDistance);
        if (hit != null)
            info.setReturnValue(hit);
    }
    
}
