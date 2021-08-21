package team.creative.littletiles.common.math.box.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.LittleBoxCombiner;
import team.creative.littletiles.common.math.vec.LittleVec;

public final class LittleBoxesSimple extends LittleBoxes implements IGridBased, Iterable<LittleBox> {
    
    protected List<LittleBox> boxes = new ArrayList<>();
    
    public LittleBoxesSimple(BlockPos pos, LittleGrid context) {
        super(pos, context);
    }
    
    @Override
    public void add(LittleBox box) {
        boxes.add(box);
    }
    
    @Override
    public boolean isEmpty() {
        return boxes.isEmpty();
    }
    
    @Override
    public void clear() {
        boxes.clear();
    }
    
    @Override
    public int size() {
        return boxes.size();
    }
    
    @Override
    public LittleBox addBox(LittleGrid grid, BlockPos pos, LittleBox box) {
        if (this.grid != grid) {
            if (this.grid.count > grid.count) {
                box.convertTo(grid, this.grid);
                grid = this.grid;
            } else
                convertTo(grid);
        }
        
        box.add(new LittleVec(grid, pos.subtract(this.pos)));
        add(box);
        return box;
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        for (LittleBox box : boxes)
            box.convertTo(this.grid, to);
        this.grid = to;
    }
    
    @Override
    public int getSmallest() {
        int size = LittleGrid.min().count;
        for (LittleBox box : boxes)
            size = Math.max(size, box.getSmallest(grid));
        return size;
    }
    
    @Override
    public LittleBox getSurroundingBox() {
        if (isEmpty())
            return null;
        
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        
        for (LittleBox box : boxes) {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        
        return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    @Override
    public Iterator<LittleBox> iterator() {
        return boxes.iterator();
    }
    
    @Override
    public Iterable<LittleBox> all() {
        return this;
    }
    
    @Override
    public HashMapList<BlockPos, LittleBox> generateBlockWise() {
        HashMapList<BlockPos, LittleBox> map = new HashMapList<>();
        for (LittleBox box : this)
            box.split(grid, pos, map, null);
        return map;
    }
    
    @Override
    public void flip(Axis axis, LittleBoxAbsolute absoluteBox) {
        sameGrid(absoluteBox, () -> {
            LittleVec center = absoluteBox.getDoubledCenter(pos);
            for (LittleBox box : boxes)
                box.mirror(axis, center);
        });
    }
    
    @Override
    public LittleBoxes copy() {
        LittleBoxesSimple boxes = new LittleBoxesSimple(pos, grid);
        boxes.boxes.addAll(this.boxes);
        return boxes;
    }
    
    @Override
    public void combineBoxesBlocks() {
        HashMapList<BlockPos, LittleBox> chunked = new HashMapList<>();
        for (int i = 0; i < boxes.size(); i++)
            chunked.add(boxes.get(i).getMinVec().getBlockPos(grid), boxes.get(i));
        boxes.clear();
        for (Iterator<ArrayList<LittleBox>> iterator = chunked.values().iterator(); iterator.hasNext();) {
            ArrayList<LittleBox> list = iterator.next();
            LittleBoxCombiner.combine(list);
            boxes.addAll(list);
        }
    }
    
}
