package com.creativemd.littletiles.common.tile.combine;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.tile.math.box.LittleBox;

public class AdvancedCombiner<T extends ICombinable> extends BasicCombiner {
    
    protected List<T> tiles;
    protected T currentTile;
    
    public AdvancedCombiner(List<T> tiles) {
        super(new ArrayList<>());
        setCombinables(tiles);
    }
    
    public void setCombinables(List<T> tiles) {
        boxes.clear();
        for (T tile : tiles) {
            boxes.add(tile.getBox());
        }
        this.tiles = tiles;
    }
    
    public List<T> getCombinables() {
        return tiles;
    }
    
    public void onCombined(T first, T second) {
        
    }
    
    protected boolean shouldScan(T tile) {
        return true;
    }
    
    @Override
    public boolean combine() {
        int sizeBefore = tiles.size();
        modified = true;
        while (modified) {
            modified = false;
            
            i = 0;
            while (i < tiles.size()) {
                if (!shouldScan(tiles.get(i))) {
                    i++;
                    continue;
                }
                
                j = 0;
                while (j < tiles.size()) {
                    if (!shouldScan(tiles.get(j))) {
                        j++;
                        continue;
                    }
                    
                    if (i != j && tiles.get(i).canCombine(tiles.get(j))) {
                        this.currentTile = tiles.get(i);
                        LittleBox box = tiles.get(i).getBox().combineBoxes(tiles.get(j).getBox(), this);
                        if (box != null) {
                            onCombined(tiles.get(i), tiles.get(j));
                            tiles.get(i).setBox(box);
                            tiles.remove(j);
                            boxes.set(i, box);
                            boxes.remove(j);
                            if (i > j)
                                i--;
                            modified = true;
                            continue;
                        }
                    }
                    j++;
                }
                i++;
            }
        }
        boolean changed = sizeBefore != tiles.size();
        this.tiles = null;
        this.currentTile = null;
        return changed;
    }
    
    @Override
    protected void removeBox(int index) {
        super.removeBox(index);
        tiles.remove(index);
    }
}