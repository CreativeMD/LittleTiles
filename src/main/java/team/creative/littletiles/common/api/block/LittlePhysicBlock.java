package team.creative.littletiles.common.api.block;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.level.little.LittleLevel;

public interface LittlePhysicBlock {
    
    public default float weight(LittleLevel level, BlockPos pos) {
        return 1;
    }
    
    public double bound(LittleLevel level, BlockPos pos, Facing facing);
    
}
