package team.creative.littletiles.common.animation.property;

import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.animation.curve.ValueCurve;

public class OffsetPathProperty extends AnimationProperty<Vec3d> {
    
    OffsetPathProperty() {}
    
    @Override
    public Vec3d defaultValue() {
        return THREE_DEFAULT;
    }
    
    @Override
    public boolean aligned(Vec3d value) {
        return true;
    }
    
    @Override
    public Pair<AnimationProperty, Vec3d> rotate(Rotation rotation, Vec3d value) {
        Vec3d newValue = new Vec3d(value);
        rotation.transform(newValue);
        return new Pair<>(this, newValue);
    }
    
    @Override
    public Pair<AnimationProperty, ValueCurve<Vec3d>> rotate(Rotation rotation, ValueCurve<Vec3d> value) {
        ValueCurve<Vec3d> newValue = value.copy();
        newValue.rotate(rotation);
        return new Pair<>(this, newValue);
    }
    
}