package team.creative.littletiles.common.math.box;

import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVecGrid;

public class LittleBoxGrid implements IGridBased {
    
    protected LittleBox box;
    protected LittleGrid grid;
    
    public LittleBoxGrid(LittleBox box, LittleGrid grid) {
        this.box = box;
        this.grid = grid;
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        box.convertTo(grid, to);
        this.grid = to;
    }
    
    @Override
    public int getSmallest() {
        return box.getSmallest(grid);
    }
    
    public void add(LittleVecGrid vec) {
        sameGrid(vec, () -> this.box.add(vec.getVec()));
    }
    
    public void sub(LittleVecGrid vec) {
        sameGrid(vec, () -> this.box.sub(vec.getVec()));
    }
    
    public LittleBoxGrid copy() {
        return new LittleBoxGrid(box.copy(), grid);
    }
    
    public LittleBox getBox() {
        return box;
    }
    
    public void setBox(LittleBox box) {
        this.box = box;
    }
    
}
