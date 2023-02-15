package team.creative.littletiles.common.level.little;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.math.base.Facing;

public interface LevelBoundsListener {
    
    public void rescan(LittleLevel level, BlockUpdateLevelSystem system, Facing facing, Iterable<BlockPos> possible, int boundary);
    
}
