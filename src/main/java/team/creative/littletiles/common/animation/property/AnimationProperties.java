package team.creative.littletiles.common.animation.property;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;

public class AnimationProperties {
    
    public static final NamedHandlerRegistry<AnimationProperty> REGISTRY = new NamedHandlerRegistry<>(null);
    
    public static final RotationProperty ROTX = new RotationProperty(Axis.X);
    public static final RotationProperty ROTY = new RotationProperty(Axis.Y);
    public static final RotationProperty ROTZ = new RotationProperty(Axis.Z);
    
    public static final OffsetProperty OFFX = new OffsetProperty(Axis.X);
    public static final OffsetProperty OFFY = new OffsetProperty(Axis.Y);
    public static final OffsetProperty OFFZ = new OffsetProperty(Axis.Z);
    
    public static final OffsetPathProperty OFF_PATH = new OffsetPathProperty();
    
    static {
        REGISTRY.register("rotX", ROTX);
        REGISTRY.register("rotY", ROTY);
        REGISTRY.register("rotZ", ROTZ);
        REGISTRY.register("offX", OFFX);
        REGISTRY.register("offY", OFFY);
        REGISTRY.register("offZ", OFFZ);
        REGISTRY.register("path", OFF_PATH);
    }
    
    public static AnimationProperty getRotation(Axis axis) {
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
    
    public static AnimationProperty getOffset(Axis axis) {
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
