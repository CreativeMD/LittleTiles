package team.creative.littletiles.common.filter;

import net.minecraft.nbt.CompoundNBT;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public class AnyFilter extends TileFilter {
    
    @Override
    public boolean is(IParentCollection parent, LittleTile tile) {
        return true;
    }
    
    @Override
    protected void saveNBT(CompoundNBT nbt) {}
    
    @Override
    protected void loadNBT(CompoundNBT nbt) {}
    
}
