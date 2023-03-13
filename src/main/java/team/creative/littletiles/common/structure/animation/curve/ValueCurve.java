package team.creative.littletiles.common.structure.animation.curve;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.VecNd;
import team.creative.creativecore.common.util.registry.NamedTypeRegistry;
import team.creative.creativecore.common.util.registry.exception.RegistryException;

public abstract class ValueCurve<T extends VecNd> {
    
    public static final NamedTypeRegistry<ValueCurve> REGISTRY = new NamedTypeRegistry<ValueCurve>().addConstructorPattern(CompoundTag.class);
    private static final Vec1d ONE_ZERO = new Vec1d();
    private static final Vec3d THREE_ZERO = new Vec3d();
    
    public static final ValueCurve<Vec1d> ONE_EMPTY = new ValueCurve<Vec1d>() {
        
        @Override
        public boolean isEmpty() {
            return true;
        }
        
        @Override
        public Vec1d value(int tick) {
            return ONE_ZERO;
        }
        
        @Override
        public void start(Vec1d start, Vec1d end, int duration) {}
        
        @Override
        public void end() {}
        
        @Override
        protected void saveExtra(CompoundTag nbt) {}
        
        @Override
        public void rotate(Rotation rotation) {}
        
        @Override
        public void mirror(Axis axis) {}
        
        @Override
        public ValueCurve<Vec1d> copy() {
            return this;
        }
        
    };
    
    public static final ValueCurve<Vec3d> THREE_EMPTY = new ValueCurve<Vec3d>() {
        
        @Override
        public boolean isEmpty() {
            return true;
        }
        
        @Override
        public Vec3d value(int tick) {
            return THREE_ZERO;
        }
        
        @Override
        public void start(Vec3d start, Vec3d end, int duration) {}
        
        @Override
        public void end() {}
        
        @Override
        protected void saveExtra(CompoundTag nbt) {}
        
        @Override
        public void rotate(Rotation rotation) {}
        
        @Override
        public void mirror(Axis axis) {}
        
        @Override
        public ValueCurve<Vec3d> copy() {
            return this;
        }
        
    };
    
    public static ValueCurve load(CompoundTag nbt) {
        try {
            return REGISTRY.create(nbt.getString("id"), nbt);
        } catch (RegistryException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ValueCurve() {}
    
    public abstract T value(int tick);
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", REGISTRY.getId(this));
        saveExtra(nbt);
        return nbt;
    }
    
    public abstract void start(T start, T end, int duration);
    
    public abstract void end();
    
    protected abstract void saveExtra(CompoundTag nbt);
    
    public abstract void rotate(Rotation rotation);
    
    public abstract void mirror(Axis axis);
    
    public abstract ValueCurve<T> copy();
    
    public boolean isEmpty() {
        return false;
    }
    
}
