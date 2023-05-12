package team.creative.littletiles.common.structure.animation.event;

import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.registry.NamedTypeRegistry;
import team.creative.littletiles.common.structure.animation.context.AnimationContext;

public abstract class AnimationEvent<T extends Tag> {
    
    public static final NamedTypeRegistry<AnimationEvent> REGISTRY = new NamedTypeRegistry<AnimationEvent>().addConstructorPattern(Tag.class);
    
    public AnimationEvent() {}
    
    public abstract T save();
    
    public abstract void start(AnimationContext context);
    
    public abstract void tick(int tick, int duration, AnimationContext context);
    
    public abstract void end(AnimationContext context);
    
    public abstract AnimationEvent<T> copy();
    
}
