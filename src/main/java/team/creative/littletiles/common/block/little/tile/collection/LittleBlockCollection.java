package team.creative.littletiles.common.block.little.tile.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;

public class LittleBlockCollection {
    
    protected HashMap<BlockPos, LittleCollection> content = new HashMap<>();
    public final BlockPos pos;
    public final LittleGrid grid;
    
    public LittleBlockCollection(BlockPos pos, LittleGrid grid) {
        this.pos = pos;
        this.grid = grid;
    }
    
    public void add(LittleGroup group, LittleVec offset) {
        
        HashMapList<BlockPos, LittleBox> map = new HashMapList<>();
        for (LittleTile tile : group) {
            tile.split(map, pos, grid, offset, null);
            
            for (Entry<BlockPos, ArrayList<LittleBox>> entry : map.entrySet()) {
                LittleCollection collection = content.get(entry.getKey());
                if (collection == null) {
                    collection = new LittleCollection();
                    content.put(entry.getKey(), collection);
                }
                collection.add(tile.copy(entry.getValue()));
            }
            
            map.clear();
        }
    }
    
    public Set<Entry<BlockPos, LittleCollection>> entrySet() {
        return content.entrySet();
    }
}
