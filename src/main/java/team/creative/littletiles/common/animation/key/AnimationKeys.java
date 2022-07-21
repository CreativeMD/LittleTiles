package team.creative.littletiles.common.animation.key;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.littletiles.common.animation.key.AnimationKey.OffsetKey;
import team.creative.littletiles.common.animation.key.AnimationKey.OffsetPathKey;
import team.creative.littletiles.common.animation.key.AnimationKey.RotationKey;

public class AnimationKeys {
    
    public static final NamedHandlerRegistry<AnimationKey> REGISTRY = new NamedHandlerRegistry<>(null);
    
    public static final RotationKey ROTX = new RotationKey(Axis.X);
    public static final RotationKey ROTY = new RotationKey(Axis.Y);
    public static final RotationKey ROTZ = new RotationKey(Axis.Z);
    
    public static final OffsetKey OFFX = new OffsetKey(Axis.X);
    public static final OffsetKey OFFY = new OffsetKey(Axis.Y);
    public static final OffsetKey OFFZ = new OffsetKey(Axis.Z);
    
    public static final OffsetPathKey OFF_PATH = new OffsetPathKey();
    
    static {
        REGISTRY.register("rotX", ROTX);
        REGISTRY.register("rotY", ROTY);
        REGISTRY.register("rotZ", ROTZ);
        REGISTRY.register("offX", OFFX);
        REGISTRY.register("offY", OFFY);
        REGISTRY.register("offZ", OFFZ);
        REGISTRY.register("path", OFF_PATH);
    }
    
    public static AnimationKey getRotation(Axis axis) {
        switch (axis) {
        case X:
            return ROTX;
        case Y:
            return ROTY;
        case Z:
            return ROTZ;
        }
        return null;
    }
    
    public static AnimationKey getOffset(Axis axis) {
        switch (axis) {
        case X:
            return OFFX;
        case Y:
            return OFFY;
        case Z:
            return OFFZ;
        }
        return null;
    }
    
}
