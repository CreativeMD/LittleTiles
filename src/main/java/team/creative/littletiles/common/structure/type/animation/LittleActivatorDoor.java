package team.creative.littletiles.common.structure.type.animation;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;

public class LittleActivatorDoor extends LittleDoor {
    
    public int[] toActivate;
    
    public LittleActivatorDoor(LittleStateStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        super.saveExtra(nbt, provider);
        nbt.putIntArray("act", toActivate);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadExtra(nbt, provider);
        toActivate = nbt.getIntArray("act");
    }
    
}
