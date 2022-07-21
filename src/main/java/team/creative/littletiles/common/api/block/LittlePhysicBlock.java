package team.creative.littletiles.common.api.block;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.util.math.base.Facing;

public interface LittlePhysicBlock {
    
    public default float weight(CreativeLevel level, BlockPos pos) {
        return 1;
    }
    
    public double bound(CreativeLevel level, BlockPos pos, Facing facing);
    
}
