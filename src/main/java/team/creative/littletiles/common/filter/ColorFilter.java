package team.creative.littletiles.common.filter;

import net.minecraft.nbt.CompoundNBT;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentTileList;

public class ColorFilter extends TileFilter {
    
    public int color;
    
    public ColorFilter(int color) {
        this.color = color;
    }
    
    public ColorFilter() {
        
    }
    
    @Override
    protected void saveNBT(CompoundNBT nbt) {
        nbt.putInt("color", color);
    }
    
    @Override
    protected void loadNBT(CompoundNBT nbt) {
        color = nbt.getInt("color");
    }
    
    @Override
    public boolean is(IParentTileList parent, LittleTile tile) {
        return tile.color == color;
    }
    
}
