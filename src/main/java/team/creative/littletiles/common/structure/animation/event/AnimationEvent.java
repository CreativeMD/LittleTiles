package team.creative.littletiles.common.structure.animation.event;

import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.registry.NamedTypeRegistry;
import team.creative.littletiles.common.structure.animation.context.AnimationContext;

public abstract class AnimationEvent<T extends Tag> {
    
    public static final NamedTypeRegistry<AnimationEvent> REGISTRY = new NamedTypeRegistry<AnimationEvent>().addConstructorPattern(Tag.class);
    
    static {
        REGISTRY.register("s", PlaySoundEvent.class);
        REGISTRY.register("c", ChildDoorEvent.class);
    }
    
    public AnimationEvent() {}
    
    public abstract T save();
    
    public abstract void start(AnimationContext context);
    
    public abstract boolean isDone(int ticksActive, AnimationContext context);
    
    public abstract AnimationEvent<T> copy();
    
    public abstract int reverseTick(int start, int duration, AnimationContext context);
    
    public AnimationEvent createGuiSpecific() {
        return this;
    }
    
    public static interface AnimationEventGui {
        
        public void prepare(AnimationContext context);
        
        public void set(int tick, AnimationContext context);
        
    }
    
}
