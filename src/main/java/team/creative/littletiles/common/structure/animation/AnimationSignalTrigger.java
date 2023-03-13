package team.creative.littletiles.common.structure.animation;

import java.text.ParseException;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;

public class AnimationSignalTrigger {
    
    public final SignalInputCondition condition;
    public final int transition;
    
    public AnimationSignalTrigger(CompoundTag nbt) {
        try {
            condition = SignalInputCondition.parseInput(nbt.getString("i"));
        } catch (ParseException e) {
            throw new RuntimeException("Invalid trigger condition found '" + nbt.getString("i") + "'");
        }
        transition = nbt.getInt("t");
    }
    
    public int signalChanged(LittleStructure structure) {
        if (condition.test(structure, false).any())
            return transition;
        return -1;
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("i", condition.toString());
        nbt.putInt("t", transition);
        return nbt;
    }
    
}
