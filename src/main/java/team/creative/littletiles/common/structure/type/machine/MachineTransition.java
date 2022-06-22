package team.creative.littletiles.common.structure.type.machine;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.animation.timeline.AnimationTimeline;

public class MachineTransition {
    
    public final String start;
    public final String end;
    public final AnimationTimeline timeline;
    
    public MachineTransition(String start, String end, AnimationTimeline timeline) {
        this.start = start;
        this.end = end;
        this.timeline = timeline;
    }
    
    public MachineTransition(CompoundTag nbt) {
        this.start = nbt.getString("from");
        this.end = nbt.getString("to");
        this.timeline = new AnimationTimeline(nbt.getCompound("ani"));
    }
    
    public CompoundTag save(CompoundTag nbt) {
        nbt.putString("from", start);
        nbt.putString("to", end);
        nbt.put("ani", timeline.save(new CompoundTag()));
        return nbt;
    }
    
}