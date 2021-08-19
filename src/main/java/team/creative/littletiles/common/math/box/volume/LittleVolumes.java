package team.creative.littletiles.common.math.box.volume;

import java.util.HashMap;
import java.util.Map.Entry;

import team.creative.creativecore.common.util.type.HashMapInteger;
import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.group.LittleGroup;

public class LittleVolumes implements IGridBased {
    
    public LittleGrid grid;
    private HashMapInteger<LittleVolume> volumes = new HashMapInteger<>();
    
    public LittleVolumes(LittleGrid grid) {
        this.grid = grid;
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public void convertTo(LittleGrid grid) {
        int ratio = grid.count / this.grid.count;
        volumes.scale(ratio);
    }
    
    public void add(LittleVolumes volumes) {
        adasdkalsk
    }
    
    public void add(LittleGroup group) {
        minGrid(group);
        for (LittleTile tile : group.allTiles())
            addDirectly(group.getGrid(), tile);
    }
    
    public void add(LittleGrid grid, LittleTile tile) {
        minGrid(grid);
        addDirectly(grid, tile);
    }
    
    private void addDirectly(LittleGrid grid, LittleTile tile) {
        int volume = tile.getVolume();
        if (grid.count < this.grid.count)
            volume *= this.grid.count / grid.count;
        
        LittleVolume type = new LittleVolume(tile.block, tile.color);
        Integer exist = volumes.get(type);
        if (exist == null)
            exist = volume;
        else
            exist += volume;
        
        volumes.put(type, exist);
    }
    
    @Override
    public int getSmallest() {
        adasd
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public int hashCode() {
        return volumes.size();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LittleVolumes) {
            LittleGrid beforeThis = grid;
            minGrid((IGridBased) obj);
            
            LittleGrid beforeTheirs = ((LittleVolumes) obj).grid;
            ((LittleVolumes) obj).minGrid(this);
            
            boolean result = ((LittleVolumes) obj).volumes.equals(this.volumes);
            
            if (beforeThis != grid)
                convertTo(beforeThis);
            
            if (beforeTheirs != ((LittleVolumes) obj).grid)
                ((LittleVolumes) obj).convertTo(beforeTheirs);
            
            return result;
        }
        return false;
    }
    
    public static class LittleVolume {
        
        public final LittleBlock block;
        public final int color;
        
        public LittleVolume(LittleBlock block, int color) {
            this.block = block;
            this.color = color;
        }
        
    }
    
}
