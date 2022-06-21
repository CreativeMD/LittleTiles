package team.creative.littletiles.common.animation.timeline;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.registry.exception.RegistryException;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.animation.AnimationState;
import team.creative.littletiles.common.animation.curve.ValueCurve;
import team.creative.littletiles.common.animation.event.AnimationEvent;
import team.creative.littletiles.common.animation.property.AnimationProperties;
import team.creative.littletiles.common.animation.property.AnimationProperty;

public class AnimationTimeline {
    
    public final int duration;
    private int tick;
    private HashMap<AnimationProperty, ValueCurve> values = new HashMap<>();
    private int eventIndex = 0;
    private List<AnimationEventEntry> events = new ArrayList<>();
    private BitSet activeEvents = new BitSet();
    
    public AnimationTimeline(CompoundTag nbt) {
        duration = nbt.getInt("duration");
        tick = nbt.getInt("tick");
        eventIndex = nbt.getInt("eindex");
        
        CompoundTag valueNBT = nbt.getCompound("values");
        for (String key : valueNBT.getAllKeys())
            values.put(AnimationProperties.REGISTRY.get(key), ValueCurve.load(valueNBT.getCompound(key)));
        
        ListTag list = nbt.getList("events", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
            events.add(new AnimationEventEntry(list.getCompound(i)));
        
        for (int i = 0; i < events.size(); i++)
            if (events.get(i).active)
                activeEvents.set(i);
    }
    
    public boolean tick(AnimationState state) {
        if (tick > duration)
            return false;
        
        tick++;
        for (Entry<AnimationProperty, ValueCurve> pair : values.entrySet())
            state.set(pair.getKey(), pair.getValue().value(tick));
        
        if (eventIndex < events.size()) {
            while (events.get(eventIndex).tick <= tick) {
                AnimationEventEntry entry = events.get(eventIndex);
                entry.start();
                activeEvents.set(eventIndex);
                eventIndex++;
            }
            
            int next = 0;
            while ((next = activeEvents.nextSetBit(next)) != -1) {
                AnimationEventEntry entry = events.get(next);
                entry.tick();
                if (entry.endTick >= tick) {
                    entry.end();
                    activeEvents.clear(next);
                }
                next++;
            }
        }
        
        return true;
    }
    
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putInt("duration", duration);
        nbt.putInt("tick", tick);
        nbt.putInt("eindex", eventIndex);
        
        CompoundTag valuesNBT = new CompoundTag();
        for (Entry<AnimationProperty, ValueCurve> entry : values.entrySet())
            valuesNBT.put(entry.getKey().name(), entry.getValue().save());
        nbt.put("values", valuesNBT);
        
        ListTag list = new ListTag();
        for (AnimationEventEntry entry : events)
            list.add(entry.save());
        nbt.put("events", list);
        return nbt;
    }
    
    public void rotate(Rotation rotation) {
        HashMap<AnimationProperty, ValueCurve> newValues = new HashMap<>();
        for (Entry<AnimationProperty, ValueCurve> pair : values.entrySet()) {
            Pair<AnimationProperty, ValueCurve> result = pair.getKey().rotate(rotation, pair.getValue());
            newValues.put(result.key, result.value);
        }
        this.values = newValues;
    }
    
    public boolean firstAligned() {
        for (Entry<AnimationProperty, ValueCurve> pair : values.entrySet()) {
            if (!pair.getKey().aligned(pair.getValue().first(pair.getKey())))
                return false;
        }
        return true;
    }
    
    public boolean lastAligned() {
        for (Entry<AnimationProperty, ValueCurve> pair : values.entrySet()) {
            if (!pair.getKey().aligned(pair.getValue().last(pair.getKey())))
                return false;
        }
        return true;
    }
    
    public class AnimationEventEntry implements Comparable<AnimationEventEntry> {
        
        public final AnimationEvent event;
        public final int tick;
        public final int endTick;
        private boolean active = false;
        
        AnimationEventEntry(CompoundTag nbt) {
            this.tick = nbt.getInt("tick");
            this.endTick = nbt.getInt("end");
            try {
                this.event = AnimationEvent.REGISTRY.create(nbt.getString("id"), nbt.get("data"));
            } catch (RegistryException e) {
                throw new RuntimeException(e);
            }
            this.active = nbt.getBoolean("active");
        }
        
        public AnimationEventEntry(int tick, int endTick, AnimationEvent event) {
            this.tick = tick;
            this.endTick = endTick;
            this.event = event;
        }
        
        public void start() {
            active = true;
        }
        
        public void tick() {
            
        }
        
        public void end() {
            active = false;
        }
        
        @Override
        public int compareTo(AnimationEventEntry o) {
            return Integer.compare(tick, o.tick);
        }
        
        public CompoundTag save() {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("tick", tick);
            nbt.putInt("end", endTick);
            nbt.putBoolean("active", active);
            nbt.putString("id", AnimationEvent.REGISTRY.getId(event));
            nbt.put("data", event.save());
            return nbt;
        }
        
    }
}
