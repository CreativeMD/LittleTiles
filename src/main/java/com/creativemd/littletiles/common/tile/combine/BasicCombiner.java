package com.creativemd.littletiles.common.tile.combine;

import java.util.List;

import com.creativemd.littletiles.common.tile.math.box.LittleBox;

public class BasicCombiner {
    
    public static boolean combineBoxes(List<LittleBox> boxes) {
        int sizeBefore = boxes.size();
        boolean modified = true;
        while (modified) {
            modified = false;
            int i = 0;
            while (i < boxes.size()) {
                int j = 0;
                while (j < boxes.size()) {
                    if (i != j) {
                        LittleBox box = boxes.get(i).combineBoxes(boxes.get(j));
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
        return sizeBefore != boxes.size();
    }
    
    public static boolean combineBoxesOnlyLast(List<LittleBox> boxes) {
        int sizeBefore = boxes.size();
        boolean modified = true;
        while (modified) {
            modified = false;
            int i = boxes.size() - 1;
            while (i < boxes.size()) {
                int j = 0;
                while (j < boxes.size()) {
                    if (i != j) {
                        LittleBox box = boxes.get(i).combineBoxes(boxes.get(j));
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
        return sizeBefore != boxes.size();
    }
    
    public static <T extends ICombinable> boolean combine(List<T> tiles) {
        int sizeBefore = tiles.size();
        boolean modified = true;
        while (modified) {
            modified = false;
            
            int i = 0;
            while (i < tiles.size()) {
                int j = 0;
                while (j < tiles.size()) {
                    if (i != j && tiles.get(i).canCombine(tiles.get(j))) {
                        LittleBox box = tiles.get(i).getBox().combineBoxes(tiles.get(j).getBox());
                        if (box != null) {
                            tiles.get(i).setBox(box);
                            tiles.remove(j);
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
        return sizeBefore != tiles.size();
    }
    
}