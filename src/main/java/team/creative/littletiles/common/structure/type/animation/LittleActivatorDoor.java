package team.creative.littletiles.common.structure.type.animation;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;

public class LittleActivatorDoor extends LittleDoor {
    
    public int[] toActivate;
    
    public LittleActivatorDoor(LittleStateStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        super.saveExtra(nbt);
        nbt.putIntArray("act", toActivate);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        super.loadExtra(nbt);
        toActivate = nbt.getIntArray("act");
    }
    
}
