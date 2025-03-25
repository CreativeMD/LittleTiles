package team.creative.littletiles.common.math.vec;

import team.creative.creativecore.common.util.math.base.Axis;

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
        return switch (axis) {
            case X -> x;
            case Y -> y;
            case Z -> z;
        };
    }
    
    public void set(Axis axis, T value) {
        switch (axis) {
            case X -> this.x = value;
            case Y -> this.y = value;
            case Z -> this.z = value;
        }
    }
    
    @Override
    public String toString() {
        return "[" + x + "," + y + "," + z + "]";
    }
    
}
