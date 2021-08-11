package com.creativemd.littletiles.common.tile.math.box;

import java.util.List;

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
    
    public final BlockPos pos;
    protected LittleGrid grid;
    
    public LittleBoxes(BlockPos pos, LittleGrid grid) {
        this.pos = pos;
        this.grid = grid;
    }
    
    public abstract void add(LittleBox box);
    
    public void addBox(IParentCollection parent, LittleTile tile) {
        addBoxes(parent.getGrid(), parent.getPos(), tile.boxes);
    }
    
    public void addBoxes(LittleGrid grid, BlockPos pos, List<LittleBox> boxes) {
        for (LittleBox box : boxes)
            addBox(grid, pos, box.copy());
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
