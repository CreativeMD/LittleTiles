package team.creative.littletiles.common.block.little.tile.group;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;

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
    
}
