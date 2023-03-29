package team.creative.littletiles.common.structure.animation.curve;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.interpolation.HermiteInterpolation.Tension;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.math.vec.Vec2d;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.VecNd;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.creativecore.common.util.type.list.PairList;

public abstract class ValueCurveInterpolation<T extends VecNd> extends ValueCurve<T> {
    
    protected PairList<Integer, T> points = new PairList<>();
    
    protected ValueCurveInterpolation() {}
    
    public ValueCurveInterpolation(CompoundTag nbt) {
        int[] timestamps = nbt.getIntArray("time");
        long[] data = nbt.getLongArray("data");
        int dimension = timestamps.length == data.length ? 1 : timestamps.length * 2 == data.length ? 2 : 3;
        int j = 0;
        for (int i = 0; i < timestamps.length; i++) {
            if (dimension == 1)
                points.add(timestamps[i], (T) new Vec1d(Double.longBitsToDouble(data[j])));
            else if (dimension == 2)
                points.add(timestamps[i], (T) new Vec2d(Double.longBitsToDouble(data[j]), Double.longBitsToDouble(data[j + 1])));
            else
                points.add(timestamps[i], (T) new Vec3d(Double.longBitsToDouble(data[j]), Double.longBitsToDouble(data[j + 1]), Double.longBitsToDouble(data[j + 2])));
            
            j += dimension;
        }
    }
    
    public void add(int key, T vec) {
        points.add(key, vec);
    }
    
    @Override
    public void start(T start, T end, int duration) { // used to add start and end state
        points.add(0, new Pair<>(0, start));
        points.add(new Pair<>(duration, end));
    }
    
    @Override
    public void end() { // start and end state will be removed again
        points.remove(0);
        points.remove(points.size() - 1);
    }
    
    @Override
    public T value(int tick) {
        int higher = points.size();
        for (int i = 0; i < points.size(); i++) {
            int otherTick = points.get(i).key;
            if (otherTick == tick)
                return points.get(i).value;
            if (otherTick > tick) {
                higher = i;
                break;
            }
        }
        
        if (higher == 0 || higher == points.size())
            return points.get(higher == 0 ? 0 : points.size() - 1).value;
        
        Pair<Integer, T> before = points.get(higher - 1);
        Pair<Integer, T> after = points.get(higher);
        double percentage = (double) (tick - before.key) / (after.key - before.key);
        
        T vec = (T) before.value.copy();
        for (int dim = 0; dim < vec.dimensions(); dim++)
            vec.set(dim, valueAt(percentage, before.value.get(dim), higher - 1, after.value.get(dim), higher, dim));
        return vec;
    }
    
    public abstract double valueAt(double mu, double before, int pointIndex, double after, int pointIndexNext, int dim);
    
    @Override
    public void saveExtra(CompoundTag nbt) {
        if (points.isEmpty())
            return;
        int[] timestamps = new int[points.size()];
        int dimension = points.getFirst().value.dimensions();
        long[] data = new long[timestamps.length * dimension];
        int j = 0;
        for (int i = 0; i < timestamps.length; i++) {
            Pair<Integer, T> pair = points.get(i);
            timestamps[i] = pair.key;
            if (dimension == 1)
                data[j] = Double.doubleToRawLongBits(((Vec1d) pair.value).x);
            else if (dimension == 2) {
                data[j] = Double.doubleToRawLongBits(((Vec2d) pair.value).x);
                data[j + 1] = Double.doubleToRawLongBits(((Vec2d) pair.value).y);
            } else if (dimension == 3) {
                data[j] = Double.doubleToRawLongBits(((Vec3d) pair.value).x);
                data[j + 1] = Double.doubleToRawLongBits(((Vec3d) pair.value).y);
                data[j + 2] = Double.doubleToRawLongBits(((Vec3d) pair.value).z);
            }
            j += dimension;
        }
        nbt.putIntArray("time", timestamps);
        nbt.putLongArray("data", data);
    }
    
    @Override
    public void rotate(Rotation rotation) {
        for (T vec : points.values()) {
            if (!(vec instanceof Vec3d))
                break;
            rotation.transform((Vec3d) vec);
        }
    }
    
    @Override
    public void mirror(Axis axis) {
        if (points.getFirst().value instanceof Vec3d)
            for (T vec : points.values())
                axis.mirror((Vec3d) vec);
        else
            for (T vec : points.values())
                vec.invert();
    }
    
    public static class LinearCurve<T extends VecNd> extends ValueCurveInterpolation<T> {
        
        public LinearCurve(CompoundTag nbt) {
            super(nbt);
        }
        
        public LinearCurve() {}
        
        @Override
        public double valueAt(double mu, double before, int pointIndex, double after, int pointIndexNext, int dim) {
            return (after - before) * mu + before;
        }
        
        @Override
        public LinearCurve<T> copy() {
            LinearCurve<T> copy = new LinearCurve<T>();
            for (Pair<Integer, T> pair : copy.points)
                points.add(pair.key, (T) pair.value.copy());
            return copy;
        }
        
    }
    
    public static class CosineCurve<T extends VecNd> extends ValueCurveInterpolation<T> {
        
        public CosineCurve(CompoundTag nbt) {
            super(nbt);
        }
        
        public CosineCurve() {}
        
        @Override
        public double valueAt(double mu, double before, int pointIndex, double after, int pointIndexNext, int dim) {
            double mu2 = (1 - Math.cos(mu * Math.PI)) / 2;
            return (before * (1 - mu2) + after * mu2);
        }
        
        @Override
        public CosineCurve<T> copy() {
            CosineCurve<T> copy = new CosineCurve<T>();
            for (Pair<Integer, T> pair : copy.points)
                points.add(pair.key, (T) pair.value.copy());
            return copy;
        }
        
    }
    
    public static abstract class AdvancedValue<T extends VecNd> extends ValueCurveInterpolation<T> {
        
        public AdvancedValue(CompoundTag nbt) {
            super(nbt);
        }
        
        public AdvancedValue() {}
        
        protected double get(int index, int dim) {
            if (index < 0)
                return points.getFirst().value.get(dim);
            if (index >= points.size())
                return points.getLast().value.get(dim);
            return points.get(index).value.get(dim);
        }
    }
    
    public static class CubicCurve<T extends VecNd> extends AdvancedValue<T> {
        
        public CubicCurve(CompoundTag nbt) {
            super(nbt);
        }
        
        public CubicCurve() {}
        
        @Override
        public double valueAt(double mu, double before, int pointIndex, double after, int pointIndexNext, int dim) {
            double v0 = get(pointIndex - 1, dim);
            double v1 = get(pointIndex, dim);
            double v2 = get(pointIndexNext, dim);
            double v3 = get(pointIndexNext + 1, dim);
            
            double a0, a1, a2, a3, mu2;
            
            mu2 = mu * mu;
            a0 = v3 - v2 - v0 + v1;
            a1 = v0 - v1 - a0;
            a2 = v2 - v0;
            a3 = v1;
            
            return (a0 * mu * mu2 + a1 * mu2 + a2 * mu + a3);
        }
        
        @Override
        public CubicCurve<T> copy() {
            CubicCurve<T> copy = new CubicCurve<T>();
            for (Pair<Integer, T> pair : copy.points)
                points.add(pair.key, (T) pair.value.copy());
            return copy;
        }
    }
    
    public static class HermiteCurve<T extends VecNd> extends AdvancedValue<T> {
        
        public static final Tension TENSION = Tension.Normal;
        public static final double BIAS = 0;
        
        public HermiteCurve(CompoundTag nbt) {
            super(nbt);
        }
        
        public HermiteCurve() {}
        
        @Override
        public double valueAt(double mu, double before, int pointIndex, double after, int pointIndexNext, int dim) {
            double m0, m1, mu2, mu3;
            double a0, a1, a2, a3;
            
            double v0 = get(pointIndex - 1, dim);
            double v1 = get(pointIndex, dim);
            double v2 = get(pointIndexNext, dim);
            double v3 = get(pointIndexNext + 1, dim);
            
            mu2 = mu * mu;
            mu3 = mu2 * mu;
            m0 = (v1 - v0) * (1 + BIAS) * (1 - TENSION.value) / 2;
            m0 += (v2 - v1) * (1 - BIAS) * (1 - TENSION.value) / 2;
            m1 = (v2 - v1) * (1 + BIAS) * (1 - TENSION.value) / 2;
            m1 += (v3 - v2) * (1 - BIAS) * (1 - TENSION.value) / 2;
            a0 = 2 * mu3 - 3 * mu2 + 1;
            a1 = mu3 - 2 * mu2 + mu;
            a2 = mu3 - mu2;
            a3 = -2 * mu3 + 3 * mu2;
            
            return (a0 * v1 + a1 * m0 + a2 * m1 + a3 * v2);
        }
        
        @Override
        public HermiteCurve<T> copy() {
            HermiteCurve<T> copy = new HermiteCurve<T>();
            for (Pair<Integer, T> pair : copy.points)
                points.add(pair.key, (T) pair.value.copy());
            return copy;
        }
    }
    
}
