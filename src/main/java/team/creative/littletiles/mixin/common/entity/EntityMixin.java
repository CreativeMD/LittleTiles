package team.creative.littletiles.mixin.common.entity;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatSet;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.box.BoxesVoxelShape;
import team.creative.creativecore.common.util.math.box.OBB;
import team.creative.creativecore.common.util.math.vec.VectorUtils;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.math.vec.LittleHitResult;
import team.creative.littletiles.mixin.common.collision.ShapesAccessor;

@Mixin(Entity.class)
public class EntityMixin {
    
    @Unique
    private Entity asEntity() {
        return (Entity) (Object) this;
    }
    
    @WrapOperation(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", require = 1, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;updateEntityAfterFallOn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V"))
    public void deltaMovementVertical(Block block, BlockGetter level, Entity entity, Operation<Void> original) {
        var vec = decreaseDelta(Axis.Y, entity);
        if (vec != null)
            entity.setDeltaMovement(vec);
        else
            original.call(block, level, entity);
    }
    
    @Unique
    private Vec3 decreaseDelta(Axis axis, Entity entity) {
        var shape = axis == Axis.X ? ShapesAccessor.getCollidedX() : (axis == Axis.Y ? ShapesAccessor.getCollidedY() : ShapesAccessor.getCollidedZ());
        if (shape instanceof BoxesVoxelShape b && b.boxes.getFirst() instanceof OBB o) {
            var d = o.origin.deltaMovement();
            if (d == null)
                return null;
            double value = d.get(axis);
            if (value == 0)
                return null;
            
            Vec3 delta = entity.getDeltaMovement();
            double detlaValue = VectorUtils.get(axis, delta);
            if (detlaValue != 0 && detlaValue < 0 ? (detlaValue < value) : (detlaValue > value))
                return VectorUtils.set(delta, value, axis);
        }
        return null;
    }
    
    @WrapOperation(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", require = 1, at = @At(value = "FIELD", target = "horizontalCollision:Z",
            opcode = Opcodes.GETFIELD, ordinal = 1))
    public boolean deltaMovementHorizontal(Entity entity, Operation<Boolean> original) {
        if (!original.call(entity))
            return false;
        
        Vec3 deltaX = decreaseDelta(Axis.X, entity);
        Vec3 deltaZ = decreaseDelta(Axis.Z, entity);
        if (deltaX == null && deltaZ == null)
            return true;
        
        var delta = entity.getDeltaMovement();
        
        double x = delta.x;
        if (deltaX != null)
            x = deltaX.x;
        else if (ShapesAccessor.getCollidedX() != null)
            x = 0;
        
        double z = delta.z;
        if (deltaZ != null)
            z = deltaZ.z;
        else if (ShapesAccessor.getCollidedZ() != null)
            z = 0;
        
        entity.setDeltaMovement(new Vec3(x, delta.y, z));
        return false;
    }
    
    @Inject(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", require = 1, at = @At("TAIL"))
    public void moveEnd(MoverType type, Vec3 motion, CallbackInfo info) {
        ShapesAccessor.setCollidedX(null);
        ShapesAccessor.setCollidedY(null);
        ShapesAccessor.setCollidedZ(null);
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
        return afloat;
    }
    
    @Shadow
    protected void onInsideBlock(BlockState state) {
        throw new UnsupportedOperationException();
    }
    
    @Inject(method = "checkInsideBlocks()V", require = 1, at = @At("HEAD"))
    protected void checkInsideBlocks(CallbackInfo info) {
        if (!LittleTiles.CONFIG.general.checkCollisionListenerForAnimations)
            return;
        
        MutableBlockPos min = new MutableBlockPos();
        MutableBlockPos max = new MutableBlockPos();
        MutableBlockPos pos = new MutableBlockPos();
        var handler = LittleTiles.ANIMATION_HANDLERS.getWithoutCreate(asEntity().level());
        for (LittleEntity entity : handler.find(asEntity().getBoundingBox())) {
            if (!entity.checkEntityInside(entity))
                continue;
            
            var bb = entity.getOrigin().getOBB(asEntity().getBoundingBox());
            var level = entity.getSubLevel();
            
            min.set(Mth.floor(bb.minX + 1.0E-7), Mth.floor(bb.minY + 1.0E-7), Mth.floor(bb.minZ + 1.0E-7));
            max.set(Mth.floor(bb.maxX - 1.0E-7), Mth.floor(bb.maxY - 1.0E-7), Mth.floor(bb.maxZ - 1.0E-7));
            if (level.hasChunksAt(min, max)) {
                
                for (int i = min.getX(); i <= max.getX(); i++) {
                    for (int j = min.getY(); j <= max.getY(); j++) {
                        for (int k = min.getZ(); k <= max.getZ(); k++) {
                            if (!asEntity().isAlive()) {
                                return;
                            }
                            
                            pos.set(i, j, k);
                            BlockState blockstate = level.getBlockState(pos);
                            
                            try {
                                blockstate.entityInside((Level) level, pos, asEntity());
                                this.onInsideBlock(blockstate);
                            } catch (Throwable throwable) {
                                CrashReport crashreport = CrashReport.forThrowable(throwable, "Colliding entity with block");
                                CrashReportCategory crashreportcategory = crashreport.addCategory("Block being collided with");
                                CrashReportCategory.populateBlockDetails(crashreportcategory, level, pos, blockstate);
                                throw new ReportedException(crashreport);
                            }
                        }
                    }
                }
            }
        }
        
    }
    
}
