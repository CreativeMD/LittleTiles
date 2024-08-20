package team.creative.littletiles.common.structure.type.animation;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;

public class LittleAdvancedDoor extends LittleDoor {
    
    public boolean differentTransition;
    
    public LittleAdvancedDoor(LittleStateStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadExtra(nbt, provider);
        differentTransition = nbt.getBoolean("diffT");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        super.saveExtra(nbt, provider);
        if (differentTransition)
            nbt.putBoolean("diffT", differentTransition);
        else
            nbt.remove("diffT");
    }
    
}
