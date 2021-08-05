package team.creative.littletiles.common.filter;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public class ColorFilter extends TileFilter {
    
    public int color;
    
    public ColorFilter(int color) {
        this.color = color;
    }
    
    public ColorFilter() {
        
    }
    
    @Override
    protected void saveNBT(CompoundTag nbt) {
        nbt.putInt("color", color);
    }
    
    @Override
    protected void loadNBT(CompoundTag nbt) {
        color = nbt.getInt("color");
    }
    
    @Override
    public boolean is(IParentCollection parent, LittleTile tile) {
        return tile.color == color;
    }
    
}
