package team.creative.littletiles.common.placement.second;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public abstract class SecondModeHandler {
    
    public LittleBox getBox(Level level, BlockPos pos, LittleGrid grid, LittleBox suggested) {
        return suggested;
    }
    
}
