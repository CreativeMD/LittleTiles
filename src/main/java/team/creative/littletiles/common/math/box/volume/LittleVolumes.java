package team.creative.littletiles.common.math.box.volume;

import java.util.Map.Entry;
import java.util.Set;

import team.creative.creativecore.common.util.type.HashMapInteger;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;

public class LittleVolumes implements IGridBased {
    
    public LittleGrid grid;
    private HashMapInteger<LittleElement> volumes = new HashMapInteger<>();
    
    public LittleVolumes(LittleGrid grid) {
        this.grid = grid;
    }
    
    public LittleVolumes() {
        this.grid = LittleGrid.min();
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
    
    public void clear() {
        this.grid = LittleGrid.min();
        volumes.clear();
    }
    
    public Set<Entry<LittleElement, Integer>> entrySet() {
        return volumes.entrySet();
    }
    
    public void add(LittleVolumes volumes) {
        this.volumes.putAll(volumes.volumes);
    }
    
    public void add(LittleGroup group) {
        minGrid(group);
        for (LittleTile tile : group.allTiles())
            addDirectly(group.getGrid(), tile);
    }
    
    public void add(LittleGrid grid, LittleElement element, int volume) {
        minGrid(grid);
        addDirectly(grid, element, volume);
    }
    
    public void add(LittleGrid grid, LittleTile tile) {
        minGrid(grid);
        addDirectly(grid, tile);
    }
    
    private void addDirectly(LittleGrid grid, LittleTile tile) {
        add(grid, tile, tile.getVolume());
    }
    
    private void addDirectly(LittleGrid grid, LittleElement tile, int volume) {
        if (grid.count < this.grid.count)
            volume *= this.grid.count / grid.count;
        
        Integer exist = volumes.get(tile);
        if (exist == null) {
            exist = volume;
            tile = new LittleElement(tile);
        } else
            exist += volume;
        
        volumes.put(tile, exist);
    }
    
    @Override
    public int getSmallest() {
        int smallest = LittleGrid.min().count;
        for (Integer value : volumes.values()) {
            double root = Math.cbrt(value);
            if ((root == Math.floor(root)))
                smallest = Math.max(smallest, grid.getMinGrid((int) root));
            else
                smallest = grid.count;
        }
        return smallest;
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
    
    public boolean isEmpty() {
        return volumes.isEmpty();
    }
    
}
