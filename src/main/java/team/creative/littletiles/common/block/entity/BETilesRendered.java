package team.creative.littletiles.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.LittleTilesRegistry;

public class BETilesRendered extends BETiles {
    
    public BETilesRendered(BlockPos pos, BlockState state) {
        super(LittleTilesRegistry.BE_TILES_TYPE_RENDERED.get(), pos, state);
    }
    
    @Override
    public boolean isRendered() {
        return true;
    }
}
