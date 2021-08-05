package team.creative.littletiles.common.filter;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public class NotFilter extends TileFilter {
    
    public TileFilter selector;
    
    public NotFilter(TileFilter selector) {
        this.selector = selector;
    }
    
    public NotFilter() {
        
    }
    
    @Override
    protected void saveNBT(CompoundTag nbt) {
        selector.saveNBT(nbt);
        nbt.putString("type2", REGISTRY.getId(selector));
    }
    
    @Override
    protected void loadNBT(CompoundTag nbt) {
        selector = TileFilter.load(nbt.getString("type2"), nbt);
    }
    
    @Override
    public boolean is(IParentCollection parent, LittleTile tile) {
        return !selector.is(parent, tile);
    }
    
}
