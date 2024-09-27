package team.creative.littletiles.common.math.box;

import team.creative.creativecore.common.util.math.base.Axis;

public enum LittleBoxSorting {
    
    XYZ(Axis.X, Axis.Y, Axis.Z),
    XZY(Axis.X, Axis.Z, Axis.Y),
    YXZ(Axis.Y, Axis.X, Axis.Z),
    YZX(Axis.Y, Axis.Z, Axis.X),
    ZXY(Axis.Z, Axis.X, Axis.Y),
    ZYX(Axis.Z, Axis.Y, Axis.X);
    
    public final Axis first;
    public final Axis second;
    public final Axis third;
    
    private LittleBoxSorting(Axis first, Axis second, Axis third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
    
}
