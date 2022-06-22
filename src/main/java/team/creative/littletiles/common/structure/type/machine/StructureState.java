package team.creative.littletiles.common.structure.type.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.structure.signal.SignalState;

public class StructureState {
    
    private Vec3d offset;
    private Vec3d rotation;
    private HashMap<String, SignalState> signals = new HashMap<>();
    
    private List<String> transitions = new ArrayList<>();
    
    private boolean animated;
    
    public StructureState(CompoundTag nbt) {
        offset = new Vec3d(nbt.getDouble("offX"), nbt.getDouble("offY"), nbt.getDouble("offZ"));
        rotation = new Vec3d(nbt.getDouble("rotX"), nbt.getDouble("rotY"), nbt.getDouble("rotZ"));
        if (nbt.contains("sig")) {
            CompoundTag signalNBT = nbt.getCompound("signal");
            for (String key : signalNBT.getAllKeys())
                signals.put(key, SignalState.loadFromTag(signalNBT.get(key)));
        }
        if (nbt.contains("tra")) {
            ListTag list = nbt.getList("transitions", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++)
                transitions.add(list.getString(i));
        }
        
        animated = nbt.getBoolean("ani");
    }
    
    public CompoundTag save(CompoundTag nbt) {
        if (offset.x != 0)
            nbt.putDouble("offX", offset.x);
        if (offset.y != 0)
            nbt.putDouble("offY", offset.y);
        if (offset.z != 0)
            nbt.putDouble("offZ", offset.z);
        
        if (rotation.x != 0)
            nbt.putDouble("rotX", rotation.x);
        if (rotation.y != 0)
            nbt.putDouble("rotY", rotation.y);
        if (rotation.z != 0)
            nbt.putDouble("rotZ", rotation.z);
        
        if (!signals.isEmpty()) {
            CompoundTag signalNBT = new CompoundTag();
            for (Entry<String, SignalState> entry : signals.entrySet())
                signalNBT.put(entry.getKey(), entry.getValue().save());
            nbt.put("sig", signalNBT);
        }
        
        if (!transitions.isEmpty()) {
            ListTag list = new ListTag();
            for (int i = 0; i < transitions.size(); i++)
                list.add(StringTag.valueOf(transitions.get(i)));
            nbt.put("tra", list);
        }
        
        if (animated)
            nbt.putBoolean("ani", animated);
        
        return nbt;
    }
    
}
