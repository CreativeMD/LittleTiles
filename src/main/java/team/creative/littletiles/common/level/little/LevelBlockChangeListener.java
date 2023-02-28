package team.creative.littletiles.common.level.little;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface LevelBlockChangeListener {
    
    public void blockChanged(BlockPos pos, BlockState state);
    
}
