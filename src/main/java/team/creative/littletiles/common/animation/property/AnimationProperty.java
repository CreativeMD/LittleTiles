package team.creative.littletiles.common.animation.property;

import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.math.vec.Vec2d;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.VecNd;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.animation.curve.ValueCurve;

public abstract class AnimationProperty<T extends VecNd> {
    
    public static final Vec1d ONE_DEFAULT = new Vec1d();
    public static final Vec2d TWO_DEFAULT = new Vec2d();
    public static final Vec3d THREE_DEFAULT = new Vec3d();
    
    public String name() {
        return AnimationProperties.REGISTRY.getId(this);
    }
    
    public abstract T defaultValue();
    
    public abstract boolean aligned(T value);
    
    public abstract Pair<AnimationProperty, T> rotate(Rotation rotation, T value);
    
    public abstract Pair<AnimationProperty, ValueCurve<T>> rotate(Rotation rotation, ValueCurve<T> value);
    
    @Override
    public String toString() {
        return name();
    }
    
}
