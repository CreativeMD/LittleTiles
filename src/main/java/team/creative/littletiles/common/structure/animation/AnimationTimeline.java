package team.creative.littletiles.common.structure.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.registry.NamedTypeRegistry;
import team.creative.creativecore.common.util.registry.exception.RegistryException;
import team.creative.littletiles.common.structure.animation.context.AnimationContext;
import team.creative.littletiles.common.structure.animation.curve.ValueCurve;
import team.creative.littletiles.common.structure.animation.event.AnimationEvent;

public abstract class AnimationTimeline {
    
    public static final NamedTypeRegistry<AnimationTimeline> REGISTRY = new NamedTypeRegistry<AnimationTimeline>().addConstructorPattern(CompoundTag.class);
    
    static {
        REGISTRY.register("i", AnimationTimelineIndividual.class);
        REGISTRY.register("o", AnimationTimelineGroupOff.class);
        REGISTRY.register("r", AnimationTimelineGroupRot.class);
        REGISTRY.register("b", AnimationTimelineGroupBoth.class);
    }
    
    public static AnimationTimeline generate(AnimationState start, AnimationState end, Supplier<ValueCurve<Vec1d>> curve1d, Supplier<ValueCurve<Vec3d>> curve3d, int duration, boolean groupOff, boolean groupRot) {
        if (groupOff)
            if (groupRot) {
                AnimationTimelineGroupBoth time = new AnimationTimelineGroupBoth(duration);
                if (start.offX() != end.offX() || start.offY() != end.offY() || start.offZ() != end.offZ())
                    time.off = curve3d.get();
                if (start.rotX() != end.rotX() || start.rotY() != end.rotY() || start.rotZ() != end.rotZ())
                    time.rot = curve3d.get();
                return time;
            } else {
                AnimationTimelineGroupOff time = new AnimationTimelineGroupOff(duration);
                if (start.offX() != end.offX() || start.offY() != end.offY() || start.offZ() != end.offZ())
                    time.off = curve3d.get();
                if (start.rotX() != end.rotX())
                    time.rotX = curve1d.get();
                if (start.rotY() != end.rotY())
                    time.rotY = curve1d.get();
                if (start.rotZ() != end.rotZ())
                    time.rotZ = curve1d.get();
                return time;
            }
        else if (groupRot) {
            AnimationTimelineGroupRot time = new AnimationTimelineGroupRot(duration);
            if (start.offX() != end.offX())
                time.offX = curve1d.get();
            if (start.offY() != end.offY())
                time.offY = curve1d.get();
            if (start.offZ() != end.offZ())
                time.offZ = curve1d.get();
            if (start.rotX() != end.rotX() || start.rotY() != end.rotY() || start.rotZ() != end.rotZ())
                time.rot = curve3d.get();
            return time;
        }
        AnimationTimelineIndividual time = new AnimationTimelineIndividual(duration);
        if (start.offX() != end.offX())
            time.offX = curve1d.get();
        if (start.offY() != end.offY())
            time.offY = curve1d.get();
        if (start.offZ() != end.offZ())
            time.offZ = curve1d.get();
        if (start.rotX() != end.rotX())
            time.rotX = curve1d.get();
        if (start.rotY() != end.rotY())
            time.rotY = curve1d.get();
        if (start.rotZ() != end.rotZ())
            time.rotZ = curve1d.get();
        return time;
    }
    
    public static AnimationTimeline load(CompoundTag nbt) {
        return REGISTRY.createSafe(AnimationTimelineIndividual.class, nbt.getString("id"), nbt);
    }
    
    public final int duration;
    private int tick;
    private int eventIndex = 0;
    private List<AnimationEventEntry> events = new ArrayList<>();
    protected PhysicalState start;
    protected PhysicalState end;
    
    public AnimationTimeline(CompoundTag nbt) {
        duration = nbt.getInt("d");
        tick = nbt.getInt("t");
        eventIndex = nbt.getInt("eI");
        
        ListTag list = nbt.getList("e", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
            events.add(new AnimationEventEntry(list.getCompound(i)));
    }
    
    public AnimationTimeline(int duration) {
        this.duration = duration;
        this.tick = 0;
    }
    
    public AnimationTimeline(int duration, List<AnimationEventEntry> events) {
        this.duration = duration;
        this.tick = 0;
        this.events.addAll(events);
    }
    
    public void start(PhysicalState start, PhysicalState end) {
        this.start = start;
        this.end = end;
    }
    
    protected abstract void tickState(int tick, PhysicalState state);
    
    public boolean tick(PhysicalState state, AnimationContext context) {
        if (tick > duration)
            return false;
        
        tick++;
        tickState(tick, state);
        
        if (eventIndex < events.size()) {
            while (events.get(eventIndex).start <= tick) {
                AnimationEventEntry entry = events.get(eventIndex);
                entry.start(context);
                eventIndex++;
            }
            
            for (AnimationEventEntry entry : events) {
                entry.tick(tick, context);
                if (entry.start + entry.duration >= tick)
                    entry.end(context);
            }
        }
        
        return tick > duration;
    }
    
    public void end() {
        this.start = this.end = null;
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("d", duration);
        nbt.putInt("t", tick);
        nbt.putInt("eI", eventIndex);
        
        ListTag list = new ListTag();
        for (AnimationEventEntry entry : events)
            list.add(entry.save());
        nbt.put("e", list);
        
        if (this.getClass() != AnimationTimelineIndividual.class)
            nbt.putString("id", REGISTRY.getId(this));
        return nbt;
    }
    
    public abstract void rotate(Rotation rotation);
    
    public abstract void mirror(Axis axis);
    
    public static class AnimationTimelineIndividual extends AnimationTimeline {
        
        protected ValueCurve<Vec1d> offX = ValueCurve.ONE_EMPTY;
        protected ValueCurve<Vec1d> offY = ValueCurve.ONE_EMPTY;
        protected ValueCurve<Vec1d> offZ = ValueCurve.ONE_EMPTY;
        protected ValueCurve<Vec1d> rotX = ValueCurve.ONE_EMPTY;
        protected ValueCurve<Vec1d> rotY = ValueCurve.ONE_EMPTY;
        protected ValueCurve<Vec1d> rotZ = ValueCurve.ONE_EMPTY;
        
        public AnimationTimelineIndividual(CompoundTag nbt) {
            super(nbt);
            if (nbt.contains("oX"))
                this.offX = ValueCurve.load(nbt.getCompound("oX"));
            if (nbt.contains("oY"))
                this.offY = ValueCurve.load(nbt.getCompound("oY"));
            if (nbt.contains("oZ"))
                this.offZ = ValueCurve.load(nbt.getCompound("oZ"));
            if (nbt.contains("rX"))
                this.rotX = ValueCurve.load(nbt.getCompound("rX"));
            if (nbt.contains("rY"))
                this.rotY = ValueCurve.load(nbt.getCompound("rY"));
            if (nbt.contains("rZ"))
                this.rotZ = ValueCurve.load(nbt.getCompound("rZ"));
        }
        
        public AnimationTimelineIndividual(int duration) {
            super(duration);
        }
        
        public AnimationTimelineIndividual(int duration, List<AnimationEventEntry> events) {
            super(duration, events);
        }
        
        @Override
        public CompoundTag save() {
            CompoundTag nbt = super.save();
            if (!offX.isEmpty())
                nbt.put("oX", offX.save());
            if (!offY.isEmpty())
                nbt.put("oY", offY.save());
            if (!offZ.isEmpty())
                nbt.put("oZ", offZ.save());
            if (!rotX.isEmpty())
                nbt.put("rX", rotX.save());
            if (!rotY.isEmpty())
                nbt.put("rY", rotY.save());
            if (!rotZ.isEmpty())
                nbt.put("rZ", rotZ.save());
            return nbt;
        }
        
        @Override
        public void start(PhysicalState start, PhysicalState end) {
            super.start(start, end);
            if (!offX.isEmpty())
                offX.start(new Vec1d(start.offX()), new Vec1d(end.offX()), duration);
            if (!offY.isEmpty())
                offY.start(new Vec1d(start.offY()), new Vec1d(end.offY()), duration);
            if (!offZ.isEmpty())
                offZ.start(new Vec1d(start.offZ()), new Vec1d(end.offZ()), duration);
            if (!rotX.isEmpty())
                rotX.start(new Vec1d(start.rotX()), new Vec1d(end.rotX()), duration);
            if (!rotY.isEmpty())
                rotY.start(new Vec1d(start.rotY()), new Vec1d(end.rotY()), duration);
            if (!rotZ.isEmpty())
                rotZ.start(new Vec1d(start.rotZ()), new Vec1d(end.rotZ()), duration);
        }
        
        @Override
        protected void tickState(int tick, PhysicalState state) {
            state.offX(offX.value(tick).x);
            state.offY(offY.value(tick).x);
            state.offZ(offZ.value(tick).x);
            state.rotX(rotX.value(tick).x);
            state.rotY(rotY.value(tick).x);
            state.rotZ(rotZ.value(tick).x);
        }
        
        @Override
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
        
        @Override
        public void mirror(Axis axis) {
            offX.mirror(axis);
            offY.mirror(axis);
            offZ.mirror(axis);
            rotX.mirror(axis);
            rotY.mirror(axis);
            rotZ.mirror(axis);
        }
        
    }
    
    public static class AnimationTimelineGroupOff extends AnimationTimeline {
        
        protected ValueCurve<Vec3d> off = ValueCurve.THREE_EMPTY;
        protected ValueCurve<Vec1d> rotX = ValueCurve.ONE_EMPTY;
        protected ValueCurve<Vec1d> rotY = ValueCurve.ONE_EMPTY;
        protected ValueCurve<Vec1d> rotZ = ValueCurve.ONE_EMPTY;
        
        public AnimationTimelineGroupOff(CompoundTag nbt) {
            super(nbt);
            if (nbt.contains("o"))
                this.off = ValueCurve.load(nbt.getCompound("o"));
            if (nbt.contains("rX"))
                this.rotX = ValueCurve.load(nbt.getCompound("rX"));
            if (nbt.contains("rY"))
                this.rotY = ValueCurve.load(nbt.getCompound("rY"));
            if (nbt.contains("rZ"))
                this.rotZ = ValueCurve.load(nbt.getCompound("rZ"));
        }
        
        public AnimationTimelineGroupOff(int duration) {
            super(duration);
        }
        
        @Override
        public CompoundTag save() {
            CompoundTag nbt = super.save();
            if (!off.isEmpty())
                nbt.put("o", off.save());
            if (!rotX.isEmpty())
                nbt.put("rX", rotX.save());
            if (!rotY.isEmpty())
                nbt.put("rY", rotY.save());
            if (!rotZ.isEmpty())
                nbt.put("rZ", rotZ.save());
            return nbt;
        }
        
        @Override
        public void start(PhysicalState start, PhysicalState end) {
            super.start(start, end);
            if (!off.isEmpty())
                off.start(start.offset(), end.offset(), duration);
            if (!rotX.isEmpty())
                rotX.start(new Vec1d(start.rotX()), new Vec1d(end.rotX()), duration);
            if (!rotY.isEmpty())
                rotY.start(new Vec1d(start.rotY()), new Vec1d(end.rotY()), duration);
            if (!rotZ.isEmpty())
                rotZ.start(new Vec1d(start.rotZ()), new Vec1d(end.rotZ()), duration);
        }
        
        @Override
        protected void tickState(int tick, PhysicalState state) {
            Vec3d vec = off.value(tick);
            state.offX(vec.x);
            state.offY(vec.y);
            state.offZ(vec.z);
            state.rotX(rotX.value(tick).x);
            state.rotY(rotY.value(tick).x);
            state.rotZ(rotZ.value(tick).x);
        }
        
        @Override
        public void rotate(Rotation rotation) {
            off.rotate(rotation);
            
            rotX.rotate(rotation);
            rotY.rotate(rotation);
            rotZ.rotate(rotation);
            
            ValueCurve<Vec1d> tempX = rotX;
            ValueCurve<Vec1d> tempY = rotY;
            ValueCurve<Vec1d> tempZ = rotZ;
            
            rotX = rotation.getX(tempX, tempY, tempZ);
            rotY = rotation.getY(tempX, tempY, tempZ);
            rotZ = rotation.getZ(tempX, tempY, tempZ);
        }
        
        @Override
        public void mirror(Axis axis) {
            off.mirror(axis);
            rotX.mirror(axis);
            rotY.mirror(axis);
            rotZ.mirror(axis);
        }
        
    }
    
    public static class AnimationTimelineGroupRot extends AnimationTimeline {
        
        protected ValueCurve<Vec1d> offX = ValueCurve.ONE_EMPTY;
        protected ValueCurve<Vec1d> offY = ValueCurve.ONE_EMPTY;
        protected ValueCurve<Vec1d> offZ = ValueCurve.ONE_EMPTY;
        protected ValueCurve<Vec3d> rot = ValueCurve.THREE_EMPTY;
        
        public AnimationTimelineGroupRot(CompoundTag nbt) {
            super(nbt);
            if (nbt.contains("oX"))
                this.offX = ValueCurve.load(nbt.getCompound("oX"));
            if (nbt.contains("oY"))
                this.offY = ValueCurve.load(nbt.getCompound("oY"));
            if (nbt.contains("oZ"))
                this.offZ = ValueCurve.load(nbt.getCompound("oZ"));
            if (nbt.contains("r"))
                this.rot = ValueCurve.load(nbt.getCompound("r"));
        }
        
        public AnimationTimelineGroupRot(int duration) {
            super(duration);
        }
        
        @Override
        public CompoundTag save() {
            CompoundTag nbt = super.save();
            if (!offX.isEmpty())
                nbt.put("oX", offX.save());
            if (!offY.isEmpty())
                nbt.put("oY", offY.save());
            if (!offZ.isEmpty())
                nbt.put("oZ", offZ.save());
            if (!rot.isEmpty())
                nbt.put("r", rot.save());
            return nbt;
        }
        
        @Override
        public void start(PhysicalState start, PhysicalState end) {
            super.start(start, end);
            if (!offX.isEmpty())
                offX.start(new Vec1d(start.offX()), new Vec1d(end.offX()), duration);
            if (!offY.isEmpty())
                offY.start(new Vec1d(start.offY()), new Vec1d(end.offY()), duration);
            if (!offZ.isEmpty())
                offZ.start(new Vec1d(start.offZ()), new Vec1d(end.offZ()), duration);
            if (!rot.isEmpty())
                rot.start(start.rotation(), end.rotation(), duration);
        }
        
        @Override
        protected void tickState(int tick, PhysicalState state) {
            state.offX(offX.value(tick).x);
            state.offY(offY.value(tick).x);
            state.offZ(offZ.value(tick).x);
            
            Vec3d vec = rot.value(tick);
            state.rotX(vec.x);
            state.rotY(vec.y);
            state.rotZ(vec.z);
        }
        
        @Override
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
            
            rot.rotate(rotation);
        }
        
        @Override
        public void mirror(Axis axis) {
            offX.mirror(axis);
            offY.mirror(axis);
            offZ.mirror(axis);
            rot.mirror(axis);
        }
        
    }
    
    public static class AnimationTimelineGroupBoth extends AnimationTimeline {
        
        protected ValueCurve<Vec3d> off = ValueCurve.THREE_EMPTY;
        protected ValueCurve<Vec3d> rot = ValueCurve.THREE_EMPTY;
        
        public AnimationTimelineGroupBoth(CompoundTag nbt) {
            super(nbt);
            if (nbt.contains("o"))
                this.off = ValueCurve.load(nbt.getCompound("o"));
            if (nbt.contains("r"))
                this.rot = ValueCurve.load(nbt.getCompound("r"));
        }
        
        public AnimationTimelineGroupBoth(int duration) {
            super(duration);
        }
        
        @Override
        public CompoundTag save() {
            CompoundTag nbt = super.save();
            if (!off.isEmpty())
                nbt.put("o", off.save());
            if (!rot.isEmpty())
                nbt.put("r", rot.save());
            return nbt;
        }
        
        @Override
        public void start(PhysicalState start, PhysicalState end) {
            super.start(start, end);
            if (!off.isEmpty())
                off.start(start.offset(), end.offset(), duration);
            if (!rot.isEmpty())
                rot.start(start.rotation(), end.rotation(), duration);
        }
        
        @Override
        protected void tickState(int tick, PhysicalState state) {
            Vec3d vec = off.value(tick);
            state.offX(vec.x);
            state.offY(vec.y);
            state.offZ(vec.z);
            
            vec = rot.value(tick);
            state.rotX(vec.x);
            state.rotY(vec.y);
            state.rotZ(vec.z);
        }
        
        @Override
        public void rotate(Rotation rotation) {
            off.rotate(rotation);
            rot.rotate(rotation);
        }
        
        @Override
        public void mirror(Axis axis) {
            off.mirror(axis);
            rot.mirror(axis);
        }
        
    }
    
    public static class AnimationEventEntry implements Comparable<AnimationEventEntry> {
        
        public final AnimationEvent event;
        public final int start;
        public final int duration;
        private boolean active = false;
        
        AnimationEventEntry(CompoundTag nbt) {
            this.start = nbt.getInt("t");
            this.duration = nbt.getInt("d");
            try {
                this.event = AnimationEvent.REGISTRY.create(nbt.getString("id"), nbt.get("e"));
            } catch (RegistryException e) {
                throw new RuntimeException(e);
            }
            this.active = nbt.getBoolean("a");
        }
        
        public AnimationEventEntry(int tick, int duration, AnimationEvent event) {
            this.start = tick;
            this.duration = duration;
            this.event = event;
        }
        
        public void start(AnimationContext context) {
            active = true;
            event.start(context);
        }
        
        public void tick(int current, AnimationContext context) {
            event.tick(current - start, duration, context);
        }
        
        public void end(AnimationContext context) {
            active = false;
            event.end(context);
        }
        
        @Override
        public int compareTo(AnimationEventEntry o) {
            return Integer.compare(start, o.start);
        }
        
        public CompoundTag save() {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("t", start);
            nbt.putInt("d", duration);
            nbt.putBoolean("a", active);
            nbt.putString("id", AnimationEvent.REGISTRY.getId(event));
            nbt.put("e", event.save());
            return nbt;
        }
        
    }
}
