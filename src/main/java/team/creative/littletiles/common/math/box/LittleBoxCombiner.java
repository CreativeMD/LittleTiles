package team.creative.littletiles.common.math.box;

import java.util.ArrayList;
import java.util.List;

import team.creative.creativecore.common.util.math.vec.RangedBitSet.BitRange;
import team.creative.creativecore.common.util.type.set.LineBitSet;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.SplitRangeBoxes;
import team.creative.littletiles.common.math.vec.SplitRangeBoxes.SplitRangeBox;

public class LittleBoxCombiner {
    
    private static List<BitRange> ranges(LineBitSet set) {
        List<BitRange> ranges = new ArrayList<>();
        int start = Integer.MIN_VALUE;
        for (Integer index : set) {
            if (start == Integer.MIN_VALUE)
                start = index;
            else {
                ranges.add(new BitRange(start, index));
                start = index;
            }
        }
        return ranges;
    }
    
    /** cuts the boxes into shapes that can be combined again. Resolves edge cases where this is not possible, but also costs some more performance.
     * Returns whether the resulted list has changed in size */
    public static boolean separate(LittleGrid grid, List<LittleBox> boxes) {
        LineBitSet xAxis = new LineBitSet();
        LineBitSet yAxis = new LineBitSet();
        LineBitSet zAxis = new LineBitSet();
        
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (LittleBox box : boxes) {
            xAxis.set(box.minX, true);
            xAxis.set(box.maxX, true);
            yAxis.set(box.minY, true);
            yAxis.set(box.maxY, true);
            zAxis.set(box.minZ, true);
            zAxis.set(box.maxZ, true);
            
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        
        SplitRangeBoxes ranges = new SplitRangeBoxes(ranges(xAxis), ranges(yAxis), ranges(zAxis));
        
        List<LittleBox> original = new ArrayList<>(boxes);
        boxes.clear();
        for (LittleBox box : original) {
            for (SplitRangeBox range : ranges) {
                if (range.intersectsWith(box)) {
                    if (range.isSame(box)) {
                        boxes.add(box);
                        break;
                    } else {
                        LittleBox splitted = box.extractBox(grid, range.x.min(), range.y.min(), range.z.min(), range.x.max(), range.y.max(), range.z.max(), null);
                        if (splitted != null)
                            boxes.add(splitted);
                    }
                }
                
            }
        }
        LittleBox.sortListByPosition(boxes, minX, minY, minZ, maxX, maxY, maxZ);
        return original.size() != boxes.size();
    }
    
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