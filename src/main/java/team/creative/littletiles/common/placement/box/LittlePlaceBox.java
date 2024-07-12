package team.creative.littletiles.common.placement.box;

import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.structure.LittleStructure;

public abstract class LittlePlaceBox {
    
    public LittleBox box;
    
    public LittlePlaceBox(LittleBox box) {
        this.box = box;
    }
    
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderBox(LittleGrid grid, LittleVec offset) {
        return box.getRenderingBox(grid, offset);
    }
    
    public abstract void place(Placement placement, LittleGrid grid, BlockPos pos, LittleStructure structure) throws LittleActionException;
    
    public void add(LittleVec vec) {
        box.add(vec);
    }
    
    public void convertTo(LittleGrid context, LittleGrid to) {
        box.convertTo(context, to);
    }
    
    public int getSmallest(LittleGrid grid) {
        return box.getSmallest(grid);
    }
    
}
