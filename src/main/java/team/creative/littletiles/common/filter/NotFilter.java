package team.creative.littletiles.common.filter;

import net.minecraft.nbt.CompoundNBT;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentTileList;

public class NotFilter extends TileFilter {
    
    public TileFilter selector;
    
    public NotFilter(TileFilter selector) {
        this.selector = selector;
    }
    
    public NotFilter() {
        
    }
    
    @Override
    protected void saveNBT(CompoundNBT nbt) {
        selector.saveNBT(nbt);
        nbt.putString("type2", REGISTRY.getId(selector));
    }
    
    @Override
    protected void loadNBT(CompoundNBT nbt) {
        selector = TileFilter.load(nbt.getString("type2"), nbt);
    }
    
    @Override
    public boolean is(IParentTileList parent, LittleTile tile) {
        return !selector.is(parent, tile);
    }
    
}
