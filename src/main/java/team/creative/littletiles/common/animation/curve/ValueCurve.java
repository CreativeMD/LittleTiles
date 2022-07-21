package team.creative.littletiles.common.animation.curve;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.VecNd;
import team.creative.creativecore.common.util.registry.NamedTypeRegistry;
import team.creative.creativecore.common.util.registry.exception.RegistryException;
import team.creative.littletiles.common.animation.property.AnimationProperty;

public abstract class ValueCurve<T extends VecNd> {
    
    public static final NamedTypeRegistry<ValueCurve> REGISTRY = new NamedTypeRegistry<ValueCurve>().addConstructorPattern(CompoundTag.class);
    
    public static ValueCurve load(CompoundTag nbt) {
        try {
            return REGISTRY.create(nbt.getString("id"), nbt);
        } catch (RegistryException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ValueCurve() {}
    
    public abstract T value(int tick);
    
    public abstract T first(AnimationProperty<T> key);
    
    public abstract T last(AnimationProperty<T> key);
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", REGISTRY.getId(this));
        saveExtra(nbt);
        return nbt;
    }
    
    protected abstract void saveExtra(CompoundTag nbt);
    
    public abstract void rotate(Rotation rotation);
    
    public abstract void mirror(Axis axis);
    
    public abstract ValueCurve<T> copy();
    
}
