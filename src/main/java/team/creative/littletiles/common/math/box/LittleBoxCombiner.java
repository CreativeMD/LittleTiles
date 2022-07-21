package team.creative.littletiles.common.math.box;

import java.util.List;

public class LittleBoxCombiner {
    
    public static boolean combine(List<LittleBox> boxes) {
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
    
    public static boolean combineLast(List<LittleBox> boxes) {
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
    
}