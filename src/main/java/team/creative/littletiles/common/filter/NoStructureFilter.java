package team.creative.littletiles.common.filter;

import net.minecraft.nbt.CompoundNBT;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentTileList;

public class NoStructureFilter extends TileFilter {
    
    public NoStructureFilter() {
        
    }
    
    @Override
    protected void saveNBT(CompoundNBT nbt) {}
    
    @Override
    protected void loadNBT(CompoundNBT nbt) {}
    
    @Override
    public boolean is(IParentTileList parent, LittleTile tile) {
        return !parent.isStructure();
    }
    
}
