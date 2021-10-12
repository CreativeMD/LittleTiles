package team.creative.littletiles.common.animation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.creativecore.common.util.type.PairList;

public class AnimationTimeline {
    
    public int duration;
    public PairList<AnimationKey, ValueTimeline> values;
    
    public AnimationTimeline(CompoundTag nbt) {
        duration = nbt.getInt("duration");
        values = new PairList<>();
        
        ListTag list = nbt.getList("values", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag valueNBT = list.getCompound(i);
            values.add(AnimationKey.getKey(valueNBT.getString("key")), ValueTimeline.read(valueNBT.getIntArray("data")));
            
        }
    }
    
    public AnimationTimeline(int duration, PairList<AnimationKey, ValueTimeline> values) {
        this.duration = duration;
        this.values = values;
    }
    
    public boolean tick(int tick, AnimationState state) {
        if (tick > duration)
            return false;
        
        for (Pair<AnimationKey, ValueTimeline> pair : values) {
            state.set(pair.key, pair.value.value(tick));
        }
        return true;
    }
    
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putInt("duration", duration);
        ListTag list = new ListTag();
        for (Pair<AnimationKey, ValueTimeline> pair : values) {
            CompoundTag valueNBT = new CompoundTag();
            valueNBT.putString("key", pair.key.name);
            valueNBT.putIntArray("data", pair.value.write());
            list.add(valueNBT);
        }
        nbt.put("values", list);
        return nbt;
    }
    
    public void offset(int offset) {
        duration += offset;
        for (Pair<AnimationKey, ValueTimeline> pair : values)
            pair.value.offset(offset);
    }
    
    public void transform(Rotation rotation) {
        PairList<AnimationKey, ValueTimeline> newPairs = new PairList<>();
        for (Pair<AnimationKey, ValueTimeline> pair : values) {
            Pair<AnimationKey, Double> result = pair.key.transform(rotation, 1);
            if (result != null) {
                if (result.value < 0)
                    pair.value.flip();
                newPairs.add(result.key, pair.value);
            } else
                newPairs.add(pair);
        }
        this.values = newPairs;
    }
    
    public boolean isFirstAligned() {
        for (Pair<AnimationKey, ValueTimeline> pair : values) {
            if (!pair.key.isAligned(pair.value.first(pair.key)))
                return false;
        }
        return true;
    }
}
