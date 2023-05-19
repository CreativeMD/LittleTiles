package team.creative.littletiles.common.structure.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.registry.exception.RegistryException;
import team.creative.creativecore.common.util.type.list.MarkIterator;
import team.creative.creativecore.common.util.type.list.MarkList;
import team.creative.littletiles.common.structure.animation.context.AnimationContext;
import team.creative.littletiles.common.structure.animation.curve.ValueCurve;
import team.creative.littletiles.common.structure.animation.event.AnimationEvent;

public class AnimationTimeline {
    
    public final int duration;
    private int tick;
    private int eventIndex = 0;
    private MarkList<AnimationEventEntry> events;
    protected PhysicalState start;
    protected PhysicalState end;
    
    protected ValueCurve<Vec1d> offX = ValueCurve.ONE_EMPTY;
    protected ValueCurve<Vec1d> offY = ValueCurve.ONE_EMPTY;
    protected ValueCurve<Vec1d> offZ = ValueCurve.ONE_EMPTY;
    protected ValueCurve<Vec1d> rotX = ValueCurve.ONE_EMPTY;
    protected ValueCurve<Vec1d> rotY = ValueCurve.ONE_EMPTY;
    protected ValueCurve<Vec1d> rotZ = ValueCurve.ONE_EMPTY;
    
    public AnimationTimeline(CompoundTag nbt) {
        duration = nbt.getInt("d");
        tick = nbt.getInt("t");
        eventIndex = nbt.getInt("eI");
        
        for (PhysicalPart part : PhysicalPart.values())
            if (nbt.contains(part.name()))
                set(part, ValueCurve.load(nbt.getCompound(part.name())));
            
        List<AnimationEventEntry> entries = new ArrayList<>();
        ListTag list = nbt.getList("e", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
            try {
                entries.add(new AnimationEventEntry(list.getCompound(i)));
            } catch (RegistryException e) {}
        events = new MarkList<>(entries);
    }
    
    public AnimationTimeline(int duration) {
        this.duration = duration;
        this.tick = 0;
        this.events = MarkList.EMPTY;
    }
    
    public AnimationTimeline(int duration, List<AnimationEventEntry> events) {
        this.duration = duration;
        this.tick = 0;
        List<AnimationEventEntry> tempList = new ArrayList<>(events);
        Collections.sort(tempList);
        this.events = new MarkList<>(tempList);
    }
    
    public ValueCurve<Vec1d> get(PhysicalPart part) {
        return switch (part) {
            case OFFX -> offX;
            case OFFY -> offY;
            case OFFZ -> offZ;
            case ROTX -> rotX;
            case ROTY -> rotY;
            case ROTZ -> rotZ;
        };
    }
    
    public void set(PhysicalPart part, ValueCurve<Vec1d> value) {
        switch (part) {
            case OFFX -> offX = value;
            case OFFY -> offY = value;
            case OFFZ -> offZ = value;
            case ROTX -> rotX = value;
            case ROTY -> rotY = value;
            case ROTZ -> rotZ = value;
        }
    }
    
    public void start(PhysicalState start, PhysicalState end, Supplier<ValueCurve<Vec1d>> curve1d) {
        this.start = start;
        this.end = end;
        this.tick = 0;
        this.eventIndex = 0;
        this.events.clear();
        for (PhysicalPart part : PhysicalPart.values()) {
            ValueCurve<Vec1d> curve = get(part);
            double s = start.get(part);
            double e = end.get(part);
            if (curve.isEmpty() && s == 0 && e == 0)
                continue;
            
            if (!curve.modifiable()) {
                curve = curve1d.get();
                set(part, curve);
            }
            
            curve.start(new Vec1d(s), new Vec1d(e), duration);
        }
    }
    
    protected void tickState(int tick, PhysicalState state) {
        for (PhysicalPart part : PhysicalPart.values()) {
            ValueCurve<Vec1d> curve = get(part);
            state.set(part, curve.isEmpty() ? 0 : get(part).value(tick).x);
        }
    }
    
    public void setStateAtTick(int tick, PhysicalState state, AnimationContext context) {
        tickState(tick, state);
        for (AnimationEventEntry entry : events.allIgnoreMark())
            entry.setAtTick(tick, context);
    }
    
    public boolean tick(PhysicalState state, AnimationContext context) {
        if (tick <= duration)
            tick++;
        tickState(tick, state);
        
        if (tick > duration && events.isEmpty())
            return true;
        
        while (eventIndex < events.sizeIgnoreMark() && events.getIgnoreMark(eventIndex).start <= tick) {
            AnimationEventEntry entry = events.getIgnoreMark(eventIndex);
            entry.start(context);
            eventIndex++;
        }
        
        for (MarkIterator<AnimationEventEntry> iterator = events.iterator(); iterator.hasNext();) {
            AnimationEventEntry entry = iterator.next();
            if (entry.active() && entry.isDone(tick, context))
                entry.end();
            if (!entry.active())
                iterator.mark();
        }
        
        return tick > duration && events.isEmpty();
    }
    
    public void end() {
        for (PhysicalPart part : PhysicalPart.values())
            get(part).end();
        this.start = this.end = null;
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("d", duration);
        nbt.putInt("t", tick);
        nbt.putInt("eI", eventIndex);
        
        ListTag list = new ListTag();
        for (AnimationEventEntry entry : events.allIgnoreMark())
            list.add(entry.save());
        nbt.put("e", list);
        
        for (PhysicalPart part : PhysicalPart.values()) {
            ValueCurve<Vec1d> curve = get(part);
            if (!curve.isEmpty())
                nbt.put(part.name(), curve.save());
        }
        return nbt;
    }
    
    public void rotate(Rotation rotation) {
        offX.rotate(rotation);
        offY.rotate(rotation);
        offZ.rotate(rotation);
        
        ValueCurve<Vec1d> tempX = offX;
        ValueCurve<Vec1d> tempY = offY;
        ValueCurve<Vec1d> tempZ = offZ;
        
        offX = rotation.getX(tempX, tempY, tempZ);
        offY = rotation.getY(tempX, tempY, tempZ);
        offZ = rotation.getZ(tempX, tempY, tempZ);
        
        rotX.rotate(rotation);
        rotY.rotate(rotation);
        rotZ.rotate(rotation);
        
        tempX = rotX;
        tempY = rotY;
        tempZ = rotZ;
        
        rotX = rotation.getX(tempX, tempY, tempZ);
        rotY = rotation.getY(tempX, tempY, tempZ);
        rotZ = rotation.getZ(tempX, tempY, tempZ);
    }
    
    public void mirror(Axis axis) {
        offX.mirror(axis);
        offY.mirror(axis);
        offZ.mirror(axis);
        rotX.mirror(axis);
        rotY.mirror(axis);
        rotZ.mirror(axis);
    }
    
    public AnimationTimeline copy() {
        List<AnimationEventEntry> events = new ArrayList<>();
        for (AnimationEventEntry entry : this.events.allIgnoreMark())
            events.add(entry.copy());
        AnimationTimeline timeline = new AnimationTimeline(duration, events);
        timeline.start = start != null ? start.copy() : null;
        timeline.end = end != null ? end.copy() : null;
        for (PhysicalPart part : PhysicalPart.values())
            timeline.set(part, get(part).copy());
        return timeline;
    }
    
    public void reverse(AnimationContext context) {
        for (PhysicalPart part : PhysicalPart.values())
            get(part).reverse(duration);
        PhysicalState beginning = start;
        start = end;
        end = beginning;
        List<AnimationEventEntry> newEvents = new ArrayList<>();
        for (AnimationEventEntry entry : events.allIgnoreMark())
            newEvents.add(new AnimationEventEntry(entry.reverseTick(duration, context), entry.event));
        Collections.sort(newEvents);
        events = new MarkList<>(newEvents);
    }
    
    public Iterable<AnimationEventEntry> allEvents() {
        return events.allIgnoreMark();
    }
    
    public static class AnimationEventEntry implements Comparable<AnimationEventEntry> {
        
        private AnimationEvent event;
        public final int start;
        protected boolean active = false;
        
        AnimationEventEntry(CompoundTag nbt) throws RegistryException {
            this.start = nbt.getInt("t");
            this.event = AnimationEvent.REGISTRY.create(nbt.getString("id"), nbt.get("e"));
            this.active = nbt.getBoolean("a");
        }
        
        public AnimationEventEntry(int tick, AnimationEvent event) {
            this.start = tick;
            this.event = event;
        }
        
        public boolean active() {
            return active;
        }
        
        public void start(AnimationContext context) {
            active = true;
            event.start(context);
        }
        
        public void setAtTick(int current, AnimationContext context) {
            if (current == start)
                event.start(context); // Only call start, do not set to active
        }
        
        public boolean isDone(int current, AnimationContext context) {
            return event.isDone(current - start, context);
        }
        
        public int reverseTick(int duration, AnimationContext context) {
            return event.reverseTick(start, duration, context);
        }
        
        public void end() {
            active = false;
        }
        
        @Override
        public int compareTo(AnimationEventEntry o) {
            return Integer.compare(start, o.start);
        }
        
        public CompoundTag save() {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("t", start);
            nbt.putBoolean("a", active);
            nbt.putString("id", AnimationEvent.REGISTRY.getId(event));
            nbt.put("e", event.save());
            return nbt;
        }
        
        public AnimationEventEntry copy() {
            return new AnimationEventEntry(start, event.copy());
        }
        
        public AnimationEvent getEvent() {
            return event;
        }
        
    }
    
}
