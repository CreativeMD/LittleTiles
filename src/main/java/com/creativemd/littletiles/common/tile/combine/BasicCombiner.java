package com.creativemd.littletiles.common.tile.combine;

import java.util.List;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;

public class BasicCombiner {
    
    public static boolean combineBoxes(List<LittleBox> boxes) {
        return new BasicCombiner(boxes).combine();
    }
    
    public static boolean combineTiles(List<LittleTile> tiles) {
        return new AdvancedCombiner<>(tiles).combine();
    }
    
    public static boolean combinePreviews(List<LittlePreview> previews) {
        return new AdvancedCombiner<>(previews).combine();
    }
    
    protected List<LittleBox> boxes;
    protected int i;
    protected int j;
    protected boolean modified;
    
    public BasicCombiner(List<LittleBox> boxes) {
        this.boxes = boxes;
    }
    
    public void set(List<LittleBox> boxes) {
        if (getClass() != BasicCombiner.class)
            throw new RuntimeException("Illegal action! Boxes cannot be set for advanced combiners!");
        
        this.boxes = boxes;
    }
    
    public List<LittleBox> getBoxes() {
        return boxes;
    }
    
    public boolean combine() {
        int sizeBefore = boxes.size();
        modified = true;
        while (modified) {
            modified = false;
            i = 0;
            while (i < boxes.size()) {
                j = 0;
                while (j < boxes.size()) {
                    if (i != j) {
                        LittleBox box = boxes.get(i).combineBoxes(boxes.get(j), this);
                        if (box != null) {
                            boxes.set(i, box);
                            boxes.remove(j);
                            modified = true;
                            if (i > j)
                                i--;
                            continue;
                        }
                    }
                    j++;
                }
                i++;
            }
        }
        boolean changed = sizeBefore != boxes.size();
        this.boxes = null;
        return changed;
    }
    
    public void removeBox(LittleBox box) {
        int index = boxes.indexOf(box);
        if (index != -1) {
            if (i > index)
                i--;
            if (j > index)
                j--;
            modified = true;
            removeBox(index);
        }
    }
    
    protected void removeBox(int index) {
        boxes.remove(index);
    }
}