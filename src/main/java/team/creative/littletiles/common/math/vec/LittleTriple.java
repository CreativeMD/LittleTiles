package team.creative.littletiles.common.math.vec;

import net.minecraft.util.EnumFacing.Axis;

public class LittleTriple<T> {
    
    private T x;
    private T y;
    private T z;
    
    public LittleTriple(T x, T y, T z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public T get(Axis axis) {
        switch (axis) {
        case X:
            return x;
        case Y:
            return y;
        case Z:
            return z;
        default:
            return null;
        }
    }
    
    public void set(Axis axis, T value) {
        switch (axis) {
        case X:
            this.x = value;
            break;
        case Y:
            this.y = value;
            break;
        case Z:
            this.z = value;
            break;
        }
    }
    
    @Override
    public String toString() {
        return "[" + x + "," + y + "," + z + "]";
    }
    
}
