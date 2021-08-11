package team.creative.littletiles.common.math.box.collection;

import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public abstract class LittleBoxes implements IGridBased {
    
    public BlockPos pos;
    public LittleGrid grid;
    
    public LittleBoxes(BlockPos pos, LittleGrid grid) {
        this.pos = pos;
        this.grid = grid;
    }
    
    public abstract void add(LittleBox box);
    
    public LittleBox addBox(IParentCollection parent, LittleTile tile) {
        return addBox(parent.getGrid(), parent.getPos(), tile.getBox().copy());
    }
    
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
    public abstract void convertTo(LittleGrid to);
    
    @Override
    public abstract int getSmallest();
    
    public abstract void clear();
    
    public abstract boolean isEmpty();
    
    public abstract int size();
    
    public abstract LittleBox getSurroundingBox();
    
    public abstract HashMapList<BlockPos, LittleBox> generateBlockWise();
    
    public abstract Iterable<LittleBox> all();
    
    public abstract void flip(Axis axis, LittleAbsoluteBox absoluteBox);
    
    public abstract LittleBoxes copy();
    
    public abstract void combineBoxesBlocks();
    
}
