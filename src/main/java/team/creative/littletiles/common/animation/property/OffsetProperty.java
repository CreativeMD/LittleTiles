package team.creative.littletiles.common.animation.property;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.animation.curve.ValueCurve;

public class OffsetProperty extends AnimationProperty<Vec1d> {
    
    public final Axis axis;
    
    OffsetProperty(Axis axis) {
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
    public Pair<AnimationProperty, Vec1d> rotate(Rotation rotation, Vec1d value) {
        return new Pair<>(AnimationProperties.getOffset(rotation.getRotatedComponent(axis)), new Vec1d(rotation.getRotatedComponentPositive(axis) ? value.x : -value.x));
    }
    
    @Override
    public Pair<AnimationProperty, ValueCurve<Vec1d>> rotate(Rotation rotation, ValueCurve<Vec1d> value) {
        ValueCurve<Vec1d> curve = value.copy();
        if (!rotation.getRotatedComponentPositive(axis))
            curve.mirror(axis);
        return new Pair<>(AnimationProperties.getOffset(rotation.getRotatedComponent(axis)), curve);
    }
    
}