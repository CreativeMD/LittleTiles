package team.creative.littletiles.common.animation.event;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import team.creative.littletiles.common.animation.context.AnimationContext;

public class ChildActivateEvent extends AnimationEvent<IntTag> {
    
    static {
        AnimationEvent.REGISTRY.register("child", ChildActivateEvent.class);
    }
    
    public int childId;
    
    public ChildActivateEvent(Tag tag) {
        super();
        this.childId = ((IntTag) tag).getAsInt();
    }
    
    public ChildActivateEvent(int childId) {
        super();
        this.childId = childId;
    }
    
    @Override
    public IntTag save() {
        return IntTag.valueOf(childId);
    }
    
    @Override
    public void start(AnimationContext context) {
        // TODO REWORK THIS ENTIRE CLASS
    }
    
    @Override
    public void tick(int tick, int duration, AnimationContext context) {}
    
    @Override
    public void end(AnimationContext context) {}
    
}
