package team.creative.littletiles.mixin.common.collision;

import java.util.Iterator;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.littletiles.LittleTiles;

@Mixin(BlockCollisions.class)
public class BlockCollisionsMixin<T> {
    
    @Shadow
    @Final
    private AABB box;
    
    @Shadow
    @Final
    private CollisionContext context;
    
    @Shadow
    @Final
    private CollisionGetter collisionGetter;
    
    @Shadow
    @Final
    private BlockPos.MutableBlockPos pos;
    
    @Unique
    private Iterator<VoxelShape> extraShapes;
    
    @Shadow
    @Final
    private BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> resultProvider;
    
    @Inject(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Cursor3D;advance()Z"), cancellable = true, require = 1)
    private void computeStart(CallbackInfoReturnable<T> info) {
        if (extraShapes != null)
            if (extraShapes.hasNext())
                info.setReturnValue(resultProvider.apply(this.pos, extraShapes.next()));
            else
                extraShapes = null;
    }
    
    @Inject(method = "<init>(Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;ZLjava/util/function/BiFunction;)V",
            at = @At("RETURN"), require = 1)
    private void constructorEnd(CollisionGetter level, @Nullable Entity entity, AABB bb, boolean onlySuffocatingBlocks, BiFunction<BlockPos.MutableBlockPos, VoxelShape, ?> resultProvider, CallbackInfo info) {
        if (!(collisionGetter instanceof Level))
            return;
        Iterable<VoxelShape> shapes = LittleTiles.ANIMATION_HANDLERS.get((Level) collisionGetter).collisionExcept(entity, box, (Level) collisionGetter);
        if (shapes != null)
            extraShapes = shapes.iterator();
    }
    
}
