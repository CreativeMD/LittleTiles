package team.creative.littletiles.common.placement.second;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;

public class InsideFixedHandler extends SecondModeHandler {
    
    protected void updateBox(Axis axis, LittleGrid grid, LittleBox box) {
        int offset = 0;
        if (box.getSize(axis) <= grid.count) {
            if (box.getMin(axis) < 0)
                offset = -box.getMin(axis);
            
            else if (box.getMax(axis) > grid.count)
                offset = grid.count - box.getMax(axis);
            LittleVec vec = new LittleVec(0, 0, 0);
            vec.set(axis, offset);
            box.add(vec);
        }
    }
    
    @Override
    public LittleBox getBox(Level level, BlockPos pos, LittleGrid grid, LittleBox suggested) {
        updateBox(Axis.X, grid, suggested);
        updateBox(Axis.Y, grid, suggested);
        updateBox(Axis.Z, grid, suggested);
        return suggested;
    }
    
}
