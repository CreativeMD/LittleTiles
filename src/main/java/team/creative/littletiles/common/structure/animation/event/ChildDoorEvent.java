package team.creative.littletiles.common.structure.animation.event;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.context.AnimationContext;
import team.creative.littletiles.common.structure.type.animation.LittleDoor;

public class ChildDoorEvent extends AnimationEvent<IntTag> {
    
    public int childId;
    
    public ChildDoorEvent(Tag tag) {
        if (tag instanceof IntTag i)
            childId = i.getAsInt();
        else
            throw new UnsupportedOperationException();
    }
    
    public ChildDoorEvent(int childId) {
        this.childId = childId;
    }
    
    @Override
    public IntTag save() {
        return IntTag.valueOf(childId);
    }
    
    @Override
    public void start(AnimationContext context) {
        if (context.isClient())
            return;
        
        LittleDoor door = getDoor(context);
        if (door == null)
            return;
        
        door.toggleState();
    }
    
    @Override
    public boolean isDone(int ticksActive, AnimationContext context) {
        LittleDoor door = getDoor(context);
        if (door == null)
            return true;
        return !door.isChanging();
    }
    
    public LittleDoor getDoor(AnimationContext context) {
        LittleStructure structure = context.getChildStructure(childId);
        if (structure instanceof LittleDoor door)
            return door;
        return null;
    }
    
    @Override
    public ChildDoorEvent copy() {
        return new ChildDoorEvent(childId);
    }
    
    @Override
    public int reverseTick(int start, int duration, AnimationContext context) {
        LittleDoor door = getDoor(context);
        if (door == null)
            return start;
        return duration - (start + door.duration);
    }
    
    @Override
    public AnimationEvent createGuiSpecific() {
        return new ChildDoorGuiEvent(childId);
    }
    
    public static class ChildDoorGuiEvent extends ChildDoorEvent implements AnimationEventGui {
        
        public int childId;
        
        public ChildDoorGuiEvent(int childId) {
            super(childId);
        }
        
        @Override
        public void prepare(AnimationContext context) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void set(int tick, AnimationContext context) {
            // TODO Auto-generated method stub
            
        }
        
    }
    
}
