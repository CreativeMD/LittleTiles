package team.creative.littletiles.common.level.little;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.entity.level.BlockUpdateLevelSystem;

public interface LevelBoundsListener {
    
    public void rescan(LittleLevel level, BlockUpdateLevelSystem system, Facing facing, Iterable<BlockPos> possible, int boundary);
    
    public void afterChangesApplied(BlockUpdateLevelSystem system);
    
}
