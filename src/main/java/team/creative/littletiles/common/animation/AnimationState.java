package team.creative.littletiles.common.animation;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.VecNd;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.animation.key.AnimationKey;
import team.creative.littletiles.common.animation.key.AnimationKeys;

public class AnimationState {
    
    private HashMap<AnimationKey, VecNd> values = new HashMap<>();
    
    public <T extends VecNd> T get(AnimationKey<T> key) {
        VecNd value = values.get(key);
        if (value == null)
            return key.defaultValue();
        return (T) value;
    }
    
    public <T extends VecNd> AnimationState set(AnimationKey<T> key, T value) {
        T savedValue = (T) values.get(key);
        
        if (key.defaultValue().equals(value)) {
            if (savedValue != null)
                values.remove(key);
            return this;
        }
        
        if (savedValue != null)
            savedValue.set(value);
        else
            values.put(key, savedValue);
        return this;
    }
    
    public AnimationState(CompoundTag nbt) {
        for (String key : nbt.getAllKeys()) {
            AnimationKey an = AnimationKeys.REGISTRY.get(key);
            if (an == null)
                continue;
            
            Tag tag = nbt.get(key);
            if (tag instanceof DoubleTag doubleTag)
                values.put(an, new Vec1d(doubleTag.getAsDouble()));
            else if (tag instanceof LongArrayTag longTag)
                values.put(an, VecNd.load(longTag.getAsLongArray()));
        }
    }
    
    public AnimationState() {}
    
    public Vec3d rotation() {
        return new Vec3d(get(AnimationKeys.ROTX).x, get(AnimationKeys.ROTY).x, get(AnimationKeys.ROTZ).x);
    }
    
    public Vec3d offset() {
        Vec3d path = (Vec3d) values.get(AnimationKeys.OFF_PATH);
        if (path != null)
            return path;
        return new Vec3d(get(AnimationKeys.OFFX).x, get(AnimationKeys.OFFY).x, get(AnimationKeys.OFFZ).x);
    }
    
    public void clear() {
        values.clear();
    }
    
    public Set<AnimationKey> keys() {
        return values.keySet();
    }
    
    public boolean aligned() {
        for (Entry<AnimationKey, VecNd> pair : values.entrySet())
            if (!pair.getKey().aligned(pair.getValue()))
                return false;
        return true;
    }
    
    public CompoundTag save(CompoundTag nbt) {
        for (Entry<AnimationKey, VecNd> pair : values.entrySet())
            if (pair.getValue() instanceof Vec1d)
                nbt.putDouble(pair.getKey().name(), ((Vec1d) pair.getValue()).x);
            else
                nbt.putLongArray(pair.getKey().name(), pair.getValue().toLong());
        return nbt;
    }
    
    public void rotate(Rotation rotation) {
        HashMap<AnimationKey, VecNd> newValues = new HashMap<>();
        for (Entry<AnimationKey, VecNd> pair : values.entrySet()) {
            Pair<AnimationKey, VecNd> result = pair.getKey().rotate(rotation, pair.getValue());
            newValues.put(result.key, result.value);
        }
        this.values = newValues;
    }
}
