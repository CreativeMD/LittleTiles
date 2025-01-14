package team.creative.littletiles.mixin.common.entity;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatSet;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.util.math.box.BoxesVoxelShape;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.littletiles.LittleTiles;
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
        LittleHitResult hit = LittleTiles.ANIMATION_HANDLERS.get(entity.level()).getHit(pos, look, reachDistance);
        if (hit != null)
            info.setReturnValue(hit);
    }
    
    @WrapOperation(method = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;collectCandidateStepUpHeights(Lnet/minecraft/world/phys/AABB;Ljava/util/List;FF)[F"), require = 1)
    private float[] collectCandidateStepUpHeights(AABB bb, List<VoxelShape> list, float maxStepHeight, float yMotion, Operation<float[]> original, @Local(
            ordinal = 0) Vec3 motion) {
        List<VoxelShape> remaining = null;
        boolean found = false;
        int i = 0;
        FloatSet floatset = new FloatArraySet(4);
        SingletonList<VoxelShape> temp = null;
        double maxStepUp = -1;
        
        while (i < list.size()) {
            if (list.get(i) instanceof BoxesVoxelShape b && b.requiresAdvancedEntityStep()) {
                if (temp == null)
                    temp = new SingletonList<>(null);
                temp.setElement(b);
                
                // Implement the old way to check how high a step should be. This is done by moving the player box up the maximum step height, then moving it horizontal.
                // At the end the box is moved down again, the remaining height is the optimal step height. If it is zero it will be ignored (because in that case a step up has no effect).
                AABB stepBB = bb.move(0, maxStepHeight, 0);
                double movedX = motion.x;
                double movedZ = motion.z;
                
                boolean zFirst = Math.abs(movedX) < Math.abs(movedZ);
                if (zFirst && movedZ != 0) {
                    movedZ = Shapes.collide(Direction.Axis.Z, stepBB, temp, movedZ);
                    if (movedZ != 0)
                        stepBB = stepBB.move(0, 0, movedZ);
                }
                
                if (movedX != 0) {
                    movedX = Shapes.collide(Direction.Axis.X, stepBB, temp, movedX);
                    if (movedX != 0)
                        stepBB = stepBB.move(movedX, 0, 0);
                }
                
                if (!zFirst && movedZ != 0) {
                    movedZ = Shapes.collide(Direction.Axis.Z, stepBB, temp, movedZ);
                    if (movedZ != 0)
                        stepBB = stepBB.move(0, 0, movedZ);
                }
                
                if (movedX != 0 || movedZ != 0) {
                    double movedY = Shapes.collide(Direction.Axis.Y, stepBB, temp, -maxStepHeight) + maxStepHeight;
                    if (movedY > 0)
                        maxStepUp = Math.max(maxStepUp, movedY);
                }
                
                if (!found && remaining == null && i > 0) {
                    remaining = new ArrayList<>();
                    for (int j = 0; j < i; j++)
                        remaining.add(list.get(j));
                }
                
                found = true;
            } else if (found) {
                if (remaining == null)
                    remaining = new ArrayList<>();
                remaining.add(list.get(i));
            }
            i++;
        }
        
        if (!found || remaining != null) {
            float[] result = original.call(bb, remaining == null ? list : remaining, maxStepHeight, yMotion);
            for (int j = 0; j < result.length; j++)
                floatset.add(result[j]);
        }
        
        if (found && maxStepUp != -1)
            floatset.add((float) maxStepUp);
        float[] afloat = floatset.toFloatArray();
        FloatArrays.unstableSort(afloat);
        FloatArrays.reverse(afloat);
        return afloat;
    }
    
}
