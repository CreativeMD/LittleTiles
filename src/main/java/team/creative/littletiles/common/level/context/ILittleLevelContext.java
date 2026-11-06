package team.creative.littletiles.common.level.context;

import java.util.UUID;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.mod.sable.SableManager;

public interface ILittleLevelContext {
    
    public static final ILittleLevelContext STANDARD = new ILittleLevelContext() {
        
        @Override
        public Matrix4f transform(double x, double y, double z, Vec3 camera, float partialTick) {
            Matrix4f matrix = new Matrix4f();
            matrix.translate((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z));
            return matrix;
        }
        
        @Override
        @Environment(EnvType.CLIENT)
        @OnlyIn(Dist.CLIENT)
        public void transformPose(PoseStack pose, double x, double y, double z, Vec3 camera, float partialTick) {
            pose.translate((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z));
        }
        
        @Override
        @Environment(EnvType.CLIENT)
        @OnlyIn(Dist.CLIENT)
        public void transformMatrix(Matrix4fStack matrix, double x, double y, double z, Vec3 camera, float partialTick) {
            matrix.translate((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z));
        }
        
        @Override
        public Matrix4f transformInverse(double x, double y, double z, Vec3 camera, float partialTick) {
            Matrix4f matrix = new Matrix4f();
            matrix.translate(-(float) (x - camera.x), -(float) (y - camera.y), -(float) (z - camera.z));
            return matrix;
        }
        
        @Override
        public Vec3 toRealWorld(Vec3 vec) {
            return vec;
        }
        
        @Override
        public Vec3 toRealWorld(Vec3 vec, float partialTick) {
            return vec;
        }
        
        @Override
        public AABB toRealWorld(AABB bb) {
            return bb;
        }
        
        @Override
        public AABB toRealWorld(AABB bb, float partialTick) {
            return bb;
        }
        
        @Override
        public Vec3 toFakeWorld(Vec3 vec) {
            return vec;
        }
        
        @Override
        public Vec3 toFakeWorld(Vec3 vec, float partialTick) {
            return vec;
        }
        
        @Override
        public AABB toFakeWorld(AABB bb) {
            return bb;
        }
        
        @Override
        public AABB toFakeWorld(AABB bb, float partialTick) {
            return bb;
        }
        
        @Override
        public boolean isSubLevel() {
            return false;
        }
        
        @Override
        public boolean is(ILittleLevelContext context) {
            return context == this;
        }
        
        @Override
        public boolean isChildOf(LevelAccessor level) {
            if (level instanceof LittleLevel l && l.getHolder() instanceof LittleEntity e) {
                e = e.getTopLevelEntity();
                if (e instanceof LittleAnimationEntity a)
                    return is(ILittleLevelContext.of(e.level(), a.getCenter().baseOffset));
            }
            return false;
        }
    };
    
    public static ILittleLevelContext of(Level level, String id) throws LittleActionException {
        LittleEntity animation = LittleTiles.ANIMATION_HANDLERS.find(level.isClientSide, UUID.fromString(id));
        if (animation == null)
            throw new LittleActionException.EntityNotFoundException();
        return animation.getSubLevel();
    }
    
    public static ILittleLevelContext of(Level level, BlockPos pos) {
        if (level instanceof ILittleLevelContext c)
            return c;
        return SableManager.context(level, pos);
    }
    
    public static ILittleLevelContext of(Level level, Vec3 vec) {
        if (level instanceof ILittleLevelContext c)
            return c;
        return SableManager.context(level, vec);
    }
    
    public default Matrix4f transform(Vec3i pos, Vec3 camera, float partialTick) {
        return transform(pos.getX(), pos.getY(), pos.getZ(), camera, partialTick);
    }
    
    public default Matrix4f transform(Vec3 pos, Vec3 camera, float partialTick) {
        return transform(pos.x, pos.y, pos.z, camera, partialTick);
    }
    
    public Matrix4f transform(double x, double y, double z, Vec3 camera, float partialTick);
    
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    public void transformPose(PoseStack pose, double x, double y, double z, Vec3 camera, float partialTick);
    
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    public void transformMatrix(Matrix4fStack matrix, double x, double y, double z, Vec3 camera, float partialTick);
    
    public Matrix4f transformInverse(double x, double y, double z, Vec3 camera, float partialTick);
    
    public Vec3 toRealWorld(Vec3 vec);
    
    public Vec3 toRealWorld(Vec3 vec, float partialTick);
    
    public AABB toRealWorld(AABB bb);
    
    public AABB toRealWorld(AABB bb, float partialTick);
    
    public Vec3 toFakeWorld(Vec3 vec);
    
    public Vec3 toFakeWorld(Vec3 vec, float partialTick);
    
    public AABB toFakeWorld(AABB bb);
    
    public AABB toFakeWorld(AABB bb, float partialTick);
    
    public default boolean isSubLevel() {
        return true;
    }
    
    public default boolean hasId() {
        return false;
    }
    
    public default String getId() {
        throw new UnsupportedOperationException();
    }
    
    public default Level asLevel(Level level) {
        return level;
    }
    
    public boolean is(@Nullable ILittleLevelContext context);
    
    public boolean isChildOf(LevelAccessor level);
    
}
