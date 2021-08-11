package team.creative.littletiles.common.math.box.volume;

import java.util.HashMap;
import java.util.Map.Entry;

import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.volume.LittleVolumes.LittleVolume;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.group.LittleGroup;

public class LittleVolumes implements IGridBased {
    
    public LittleGrid grid;
    private HashMap<LittleBlock, Double> volumes = new HashMap<>();
    
    public LittleVolumes(LittleGrid grid) {
        this.grid = grid;
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public void convertTo(LittleGrid grid) {
        double ratio = (double) grid.count / this.grid.count;
        for (Entry<LittleBlock, Double> entry : volumes.entrySet()) {
            entry.setValue(entry.getValue() * ratio);
        }
    }
    
    public void add(LittleGroup group) {
        minGrid(group);
        for (LittleTile tile : group)
            addDirectly(group.getGrid(), tile);
    }
    
    public void add(LittleGrid grid, LittleTile tile) {
        minGrid(grid);
        addDirectly(grid, tile);
    }
    
    private void addDirectly(LittleGrid context, LittleTile tile) {
        double volume = preview.getVolume();
        if (context.size < this.context.size)
            volume *= this.context.size / context.size;
        
        LittleVolume type = new LittleVolume(preview.getBlock(), preview.getMeta());
        Double exist = volumes.get(type);
        if (exist == null)
            exist = volume;
        else
            exist += volume;
        
        volumes.put(type, exist);
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
    
}
