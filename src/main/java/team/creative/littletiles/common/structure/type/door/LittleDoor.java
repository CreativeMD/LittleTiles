package team.creative.littletiles.common.structure.type.door;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.type.machine.LittleStateMachine;

public class LittleDoor extends LittleStateMachine {
    
    public boolean activateParent = false;
    public boolean waitingForApproval = false;
    public boolean disableRightClick = false;
    public boolean noClip = false;
    public boolean playPlaceSounds = true;
    
    public LittleDoor(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        super.loadExtra(nbt);
        activateParent = nbt.getBoolean("activateParent");
        disableRightClick = nbt.getBoolean("disableRightClick");
        if (nbt.contains("sounds"))
            playPlaceSounds = nbt.getBoolean("sounds");
        else
            playPlaceSounds = true;
        noClip = nbt.getBoolean("noClip");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        super.saveExtra(nbt);
        nbt.putBoolean("activateParent", activateParent);
        nbt.putBoolean("disableRightClick", disableRightClick);
        if (noClip)
            nbt.putBoolean("noClip", noClip);
        else
            nbt.remove("noClip");
        nbt.putBoolean("sounds", playPlaceSounds);
    }
    
}
