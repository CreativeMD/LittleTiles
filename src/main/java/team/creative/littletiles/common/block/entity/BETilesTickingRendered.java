package team.creative.littletiles.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BETilesTickingRendered extends BETilesTicking {
    
    public BETilesTickingRendered(BlockPos pos, BlockState state) {
        super(pos, state);
    }
    
    @Override
    public boolean isRendered() {
        return true;
    }
    
}
