package team.creative.littletiles.common.math.box;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.math.BlockPos;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;

public abstract class LittleBoxes implements IGridBased {
    
    public BlockPos pos;
    public LittleGrid context;
    
    public LittleBoxes(BlockPos pos, LittleGrid context) {
        this.pos = pos;
        this.context = context;
    }
    
    public abstract void add(LittleBox box);
    
    public LittleBox addBox(IParentTileList parent, LittleTile tile) {
        return addBox(parent.getContext(), parent.getPos(), tile.getBox().copy());
    }
    
    public LittleBox addBox(LittleGridContext context, BlockPos pos, LittleBox box) {
        if (this.context != context) {
            if (this.context.size > context.size) {
                box.convertTo(context, this.context);
                context = this.context;
            } else
                convertTo(context);
        }
        
        box.add(new LittleVec(context, pos.subtract(this.pos)));
        add(box);
        return box;
    }
    
    @Override
    public LittleGridContext getContext() {
        return context;
    }
    
    @Override
    public abstract void convertTo(LittleGridContext to);
    
    @Override
    public abstract void convertToSmallest();
    
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
