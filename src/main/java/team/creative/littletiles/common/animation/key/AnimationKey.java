package team.creative.littletiles.common.animation.key;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.math.vec.Vec2d;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.VecNd;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.animation.curve.ValueCurve;

public abstract class AnimationKey<T extends VecNd> {
    
    public static final Vec1d ONE_DEFAULT = new Vec1d();
    public static final Vec2d TWO_DEFAULT = new Vec2d();
    public static final Vec3d THREE_DEFAULT = new Vec3d();
    
    public String name() {
        return AnimationKeys.REGISTRY.getId(this);
    }
    
    public abstract T defaultValue();
    
    public abstract boolean aligned(T value);
    
    public abstract Pair<AnimationKey, T> rotate(Rotation rotation, T value);
    
    public abstract Pair<AnimationKey, ValueCurve<T>> rotate(Rotation rotation, ValueCurve<T> value);
    
    @Override
    public String toString() {
        return name();
    }
    
    public static class RotationKey extends AnimationKey<Vec1d> {
        
        public final Axis axis;
        
        RotationKey(Axis axis) {
            this.axis = axis;
        }
        
        @Override
        public boolean aligned(Vec1d value) {
            return value.x % 360 == 0;
        }
        
        @Override
        public Pair<AnimationKey, Vec1d> rotate(Rotation rotation, Vec1d value) {
            return new Pair<>(AnimationKeys.getRotation(rotation.getRotatedComponent(axis)), new Vec1d(rotation.getRotatedComponentPositive(axis) ? value.x : -value.x));
        }
        
        @Override
        public Vec1d defaultValue() {
            return ONE_DEFAULT;
        }
        
        @Override
        public Pair<AnimationKey, ValueCurve<Vec1d>> rotate(Rotation rotation, ValueCurve<Vec1d> value) {
            ValueCurve<Vec1d> curve = value.copy();
            if (!rotation.getRotatedComponentPositive(axis))
                curve.mirror(axis);
            return new Pair<>(AnimationKeys.getRotation(rotation.getRotatedComponent(axis)), curve);
        }
        
    }
    
    public static class OffsetKey extends AnimationKey<Vec1d> {
        
        public final Axis axis;
        
        OffsetKey(Axis axis) {
            this.axis = axis;
        }
        
        @Override
        public Vec1d defaultValue() {
            return ONE_DEFAULT;
        }
        
        @Override
        public boolean aligned(Vec1d value) {
            return true;
        }
        
        @Override
        public Pair<AnimationKey, Vec1d> rotate(Rotation rotation, Vec1d value) {
            return new Pair<>(AnimationKeys.getOffset(rotation.getRotatedComponent(axis)), new Vec1d(rotation.getRotatedComponentPositive(axis) ? value.x : -value.x));
        }
        
        @Override
        public Pair<AnimationKey, ValueCurve<Vec1d>> rotate(Rotation rotation, ValueCurve<Vec1d> value) {
            ValueCurve<Vec1d> curve = value.copy();
            if (!rotation.getRotatedComponentPositive(axis))
                curve.mirror(axis);
            return new Pair<>(AnimationKeys.getOffset(rotation.getRotatedComponent(axis)), curve);
        }
        
    }
    
    public static class OffsetPathKey extends AnimationKey<Vec3d> {
        
        OffsetPathKey() {}
        
        @Override
        public Vec3d defaultValue() {
            return THREE_DEFAULT;
        }
        
        @Override
        public boolean aligned(Vec3d value) {
            return true;
        }
        
        @Override
        public Pair<AnimationKey, Vec3d> rotate(Rotation rotation, Vec3d value) {
            Vec3d newValue = new Vec3d(value);
            rotation.transform(newValue);
            return new Pair<>(this, newValue);
        }
        
        @Override
        public Pair<AnimationKey, ValueCurve<Vec3d>> rotate(Rotation rotation, ValueCurve<Vec3d> value) {
            ValueCurve<Vec3d> newValue = value.copy();
            newValue.rotate(rotation);
            return new Pair<>(this, newValue);
        }
        
    }
    
}
