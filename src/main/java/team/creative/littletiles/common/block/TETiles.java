package team.creative.littletiles.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;

public class TETiles extends BlockEntity implements IGridBased {
    
    public TETiles(BlockPos pos, BlockState state) {
        super(LittleTiles.TILES_TE_TYPE, pos, state);
    }
    
    @Override
    public LittleGrid getGrid() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public int getSmallest() {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
