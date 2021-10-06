package team.creative.littletiles.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.LittleTiles;

public class BETilesTicking extends BETiles {
    
    public BETilesTicking(BlockPos pos, BlockState state) {
        super(LittleTiles.BE_TILES_TYPE_TICKING, pos, state);
    }
    
    @Override
    public boolean isTicking() {
        return true;
    }
}
