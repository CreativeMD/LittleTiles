package team.creative.littletiles.common.structure.type.machine;

import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

public abstract class LittleStateMachine extends LittleStructure {
    
    public String parser;
    
    private HashMap<String, StructureState> states = new HashMap<>();
    private HashMap<String, MachineTransition> transitions = new HashMap<>();
    private String current;
    private StructureState tempState;
    
    public LittleStateMachine(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    public StructureState get(String in) {
        return states.get(in);
    }
    
    public StructureState state() {
        if (tempState == null)
            tempState = get(current);
        return tempState;
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        current = nbt.getString("cu");
        
        states.clear();
        CompoundTag stateNBT = nbt.getCompound("st");
        for (String key : stateNBT.getAllKeys())
            states.put(key, new StructureState(stateNBT.getCompound(key)));
        
        transitions.clear();
        CompoundTag transNBT = nbt.getCompound("tr");
        for (String key : transNBT.getAllKeys())
            transitions.put(key, new MachineTransition(transNBT.getCompound(key)));
        
        parser = nbt.getString("parser");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.putString("cu", current);
        
        CompoundTag stateNBT = new CompoundTag();
        for (Entry<String, StructureState> entry : states.entrySet())
            stateNBT.put(entry.getKey(), entry.getValue().save(new CompoundTag()));
        nbt.put("st", stateNBT);
        
        CompoundTag transNBT = new CompoundTag();
        for (Entry<String, MachineTransition> entry : transitions.entrySet())
            stateNBT.put(entry.getKey(), entry.getValue().save(new CompoundTag()));
        nbt.put("tr", transNBT);
        
        nbt.putString("parser", parser);
    }
    
}
