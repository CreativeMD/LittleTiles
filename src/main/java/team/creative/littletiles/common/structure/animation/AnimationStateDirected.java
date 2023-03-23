package team.creative.littletiles.common.structure.animation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import team.creative.littletiles.common.structure.LittleStructure;

public class AnimationStateDirected extends AnimationState {
    
    private int rightClickTransition = -1;
    private List<AnimationSignalTrigger> triggers;
    
    public AnimationStateDirected(CompoundTag nbt) {
        super(nbt);
        this.rightClickTransition = nbt.contains("right") ? nbt.getInt("right") : -1;
        ListTag list = nbt.getList("trigger", Tag.TAG_COMPOUND);
        this.triggers = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++)
            triggers.add(new AnimationSignalTrigger(list.getCompound(i)));
    }
    
    public AnimationStateDirected(String name) {
        super(name);
    }
    
    @Override
    public CompoundTag save() {
        CompoundTag nbt = super.save();
        if (hasRightClickTransition())
            nbt.putInt("right", rightClickTransition);
        ListTag list = new ListTag();
        for (int i = 0; i < triggers.size(); i++)
            list.add(triggers.get(i).save());
        nbt.put("trigger", list);
        return nbt;
    }
    
    public boolean hasRightClickTransition() {
        return rightClickTransition != -1;
    }
    
    public int signalChanged(LittleStructure structure) {
        for (AnimationSignalTrigger trigger : triggers) {
            int transition = trigger.signalChanged(structure);
            if (transition >= 0)
                return transition;
        }
        return -1;
    }
    
}
