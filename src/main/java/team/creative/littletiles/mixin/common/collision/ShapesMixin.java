package team.creative.littletiles.mixin.common.collision;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.math.box.BoxesVoxelShape;

@Mixin(Shapes.class)
public class ShapesMixin {
    
    @Inject(method = "joinIsNotEmpty(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/BooleanOp;)Z",
            at = @At("HEAD"), cancellable = true, require = 1)
    private static void joinIsNotEmpty(VoxelShape shape1, VoxelShape shape2, BooleanOp operation, CallbackInfoReturnable<Boolean> info) {
        if (operation == BooleanOp.AND && (shape1 instanceof BoxesVoxelShape || shape2 instanceof BoxesVoxelShape)) {
            if (shape1 instanceof BoxesVoxelShape bb1 && shape2 instanceof BoxesVoxelShape bb2) {
                for (ABB first : bb1.boxes) {
                    for (ABB second : bb2.boxes) {
                        if (first.intersects(second)) {
                            info.setReturnValue(true);
                            return;
                        }
                    }
                }
                info.setReturnValue(false);
                return;
            }
            
            if (shape1 instanceof BoxesVoxelShape bb1) {
                List<AABB> others = shape2.toAabbs();
                for (ABB first : bb1.boxes) {
                    for (AABB second : others) {
                        if (first.intersects(second)) {
                            info.setReturnValue(true);
                            return;
                        }
                    }
                }
                info.setReturnValue(false);
                return;
            }
            
            if (shape2 instanceof BoxesVoxelShape bb1) {
                List<AABB> others = shape1.toAabbs();
                for (ABB first : bb1.boxes) {
                    for (AABB second : others) {
                        if (first.intersects(second)) {
                            info.setReturnValue(true);
                            return;
                        }
                    }
                }
                info.setReturnValue(false);
                return;
            }
        }
    }
    
}
