package team.creative.littletiles.common.filter;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public class NoStructureFilter extends TileFilter {
    
    public NoStructureFilter() {
        
    }
    
    @Override
    protected void saveNBT(CompoundTag nbt) {}
    
    @Override
    protected void loadNBT(CompoundTag nbt) {}
    
    @Override
    public boolean is(IParentCollection parent, LittleTile tile) {
        return !parent.isStructure();
    }
    
}
