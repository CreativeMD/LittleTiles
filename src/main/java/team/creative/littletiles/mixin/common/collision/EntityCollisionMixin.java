package team.creative.littletiles.mixin.common.collision;

import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;

@Mixin(Entity.class)
public class EntityCollisionMixin {
    
    @Inject(method = "collideBoundingBox",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getBlockCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/lang/Iterable;",
                    shift = Shift.AFTER),
            require = 1, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void collideBoundingBox(@Nullable Entity entity, Vec3 vec, AABB bb, Level level, List<VoxelShape> shapes, CallbackInfoReturnable<Vec3> info, ImmutableList.Builder<VoxelShape> builder) {
        LittleAnimationHandlers.get(level).collision(entity, bb.expandTowards(vec), builder);
    }
    
}
