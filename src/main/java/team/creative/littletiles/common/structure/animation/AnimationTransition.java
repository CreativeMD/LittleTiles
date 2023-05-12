package team.creative.littletiles.common.structure.animation;

import net.minecraft.nbt.CompoundTag;

public class AnimationTransition {
    
    public final String name;
    public final int start;
    public final int end;
    public final AnimationTimeline timeline;
    
    public AnimationTransition(String name, int start, int end, AnimationTimeline timeline) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.timeline = timeline;
    }
    
    public AnimationTransition(CompoundTag nbt) {
        this.name = nbt.getString("n");
        this.start = nbt.getInt("f");
        this.end = nbt.getInt("t");
        this.timeline = new AnimationTimeline(nbt.getCompound("a"));
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("n", name);
        nbt.putInt("f", start);
        nbt.putInt("t", end);
        nbt.put("a", timeline.save());
        return nbt;
    }
    
}