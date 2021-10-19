package team.creative.littletiles.common.block.little.tile.group;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;

public class LittleGroupAbsolute implements IGridBased {
    
    public final BlockPos pos;
    public final LittleGroup group;
    
    public LittleGroupAbsolute(BlockPos pos, LittleGroup group) {
        this.pos = pos;
        this.group = group;
    }
    
    public LittleGroupAbsolute(BlockPos pos) {
        this(pos, new LittleGroup(null, LittleGrid.min(), null));
    }
    
    @Override
    public LittleGrid getGrid() {
        return group.getGrid();
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        group.convertTo(to);
    }
    
    @Override
    public int getSmallest() {
        return group.getSmallest();
    }
    
    public void add(IParentCollection parent, LittleTile tile) {
        tile = tile.copy();
        
        if (this.getGrid() != parent.getGrid())
            if (this.getGrid().count > parent.getGrid().count)
                tile.convertTo(parent.getGrid(), this.getGrid());
            else
                convertTo(parent.getGrid());
            
        tile.move(new LittleVec(getGrid(), parent.getPos().subtract(pos)));
        addDirectly(tile);
    }
    
    @SuppressWarnings("deprecation")
    protected void addDirectly(LittleTile tile) {
        group.addDirectly(tile);
    }
    
}
