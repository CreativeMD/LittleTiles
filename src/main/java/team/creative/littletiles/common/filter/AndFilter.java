package team.creative.littletiles.common.filter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.Constants;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public class AndFilter extends TileFilter {
    
    public TileFilter[] selectors;
    
    public AndFilter(TileFilter... selectors) {
        this.selectors = selectors;
    }
    
    public AndFilter() {
        
    }
    
    @Override
    protected void saveNBT(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (int i = 0; i < selectors.length; i++)
            list.add(selectors[i].writeNBT(new CompoundTag()));
        nbt.put("list", list);
    }
    
    @Override
    protected void loadNBT(CompoundTag nbt) {
        ListTag list = nbt.getList("list", Constants.NBT.TAG_COMPOUND);
        selectors = new TileFilter[list.size()];
        for (int i = 0; i < selectors.length; i++)
            selectors[i] = TileFilter.load(list.getCompound(i));
    }
    
    @Override
    public boolean is(IParentCollection parent, LittleTile tile) {
        for (int i = 0; i < selectors.length; i++) {
            if (!selectors[i].is(parent, tile))
                return false;
        }
        return true;
    }
    
}
