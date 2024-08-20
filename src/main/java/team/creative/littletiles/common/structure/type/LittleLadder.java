package team.creative.littletiles.common.structure.type;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

public class LittleLadder extends LittleStructure {
    
    public LittleLadder(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt, HolderLookup.Provider provider) {}
    
    @Override
    protected void saveExtra(CompoundTag nbt, HolderLookup.Provider provider) {}
    
}
