package team.creative.littletiles.mixin.common.collision;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(Shapes.class)
public class ShapesMixin {
    
    private static final ThreadLocal<VoxelShape> collidedX = new ThreadLocal<>();
    private static final ThreadLocal<VoxelShape> collidedY = new ThreadLocal<>();
    private static final ThreadLocal<VoxelShape> collidedZ = new ThreadLocal<>();
    
    @WrapOperation(method = "collide(Lnet/minecraft/core/Direction$Axis;Lnet/minecraft/world/phys/AABB;Ljava/lang/Iterable;D)D", require = 1, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/shapes/VoxelShape;collide(Lnet/minecraft/core/Direction$Axis;Lnet/minecraft/world/phys/AABB;D)D"))
    private static double collide(VoxelShape shape, Direction.Axis axis, AABB bb, double value, Operation<Double> original) {
        double result = original.call(shape, axis, bb, value);
        if (Math.abs(value) > Math.abs(result))
            switch (axis) {
                case X -> collidedX.set(shape);
                case Y -> collidedY.set(shape);
                case Z -> collidedZ.set(shape);
            }
        return result;
    }
    
    @SuppressWarnings("unused")
    private static VoxelShape collidedX() {
        return collidedX.get();
    }
    
    @SuppressWarnings("unused")
    private static VoxelShape collidedY() {
        return collidedY.get();
    }
    
    @SuppressWarnings("unused")
    private static VoxelShape collidedZ() {
        return collidedZ.get();
    }
    
    @SuppressWarnings("unused")
    private static void collidedX(VoxelShape shape) {
        collidedX.set(shape);
    }
    
    @SuppressWarnings("unused")
    private static void collidedY(VoxelShape shape) {
        collidedY.set(shape);
    }
    
    @SuppressWarnings("unused")
    private static void collidedZ(VoxelShape shape) {
        collidedZ.set(shape);
    }
    
}
