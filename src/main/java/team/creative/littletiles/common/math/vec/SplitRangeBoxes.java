package team.creative.littletiles.common.math.vec;

import java.util.Iterator;
import java.util.List;

import team.creative.creativecore.common.util.math.vec.RangedBitSet;
import team.creative.creativecore.common.util.math.vec.RangedBitSet.BitRange;
import team.creative.littletiles.common.math.box.LittleBox;

public class SplitRangeBoxes implements Iterable<SplitRangeBoxes.SplitRangeBox> {
    
    protected List<BitRange> xList;
    protected List<BitRange> yList;
    protected List<BitRange> zList;
    
    public SplitRangeBoxes(RangedBitSet x, RangedBitSet y, RangedBitSet z) {
        this.xList = x.getRanges();
        this.yList = y.getRanges();
        this.zList = z.getRanges();
    }
    
    public SplitRangeBoxes(List<BitRange> xList, List<BitRange> yList, List<BitRange> zList) {
        this.xList = xList;
        this.yList = yList;
        this.zList = zList;
    }
    
    @Override
    public Iterator<SplitRangeBox> iterator() {
        
        SplitRangeBox box = new SplitRangeBox();
        
        return new Iterator<SplitRangeBoxes.SplitRangeBox>() {
            
            public int x = 0;
            public int y = 0;
            public int z = 0;
            
            public int total = xList.size() * yList.size() * zList.size();
            public int current = 0;
            
            @Override
            public boolean hasNext() {
                return current < total;
            }
            
            @Override
            public SplitRangeBox next() {
                box.set(xList.get(x), yList.get(y), zList.get(z));
                z++;
                if (z >= zList.size()) {
                    y++;
                    z = 0;
                    if (y >= yList.size()) {
                        x++;
                        y = 0;
                    }
                }
                current++;
                return box;
            }
        };
    }
    
    public static class SplitRangeBox {
        
        public BitRange x;
        public BitRange y;
        public BitRange z;
        
        public void set(BitRange x, BitRange y, BitRange z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public boolean intersectsWith(LittleBox box) {
            return box.maxX > x.min() && box.minX < x.max() && box.maxY > y.min() && box.minY < y.max() && box.maxZ > z.min() && box.minZ < z.max();
        }
        
        public boolean isSame(LittleBox box) {
            return box.minX == x.min() && box.maxX == x.max() && box.minY == y.min() && box.maxY == y.max() && box.minZ == z.min() && box.maxZ == z.max();
        }
    }
    
}
