package team.creative.littletiles.common.placement.box;

import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.structure.LittleStructure;

public class LittlePlaceBoxFacing extends LittlePlaceBox {
    
    public Facing facing;
    public int color;
    
    public LittlePlaceBoxFacing(LittleBox box, Facing facing, int color) {
        super(box.copy());
        this.facing = facing;
        this.color = color;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderBox(LittleGrid grid, LittleVec offset) {
        LittleBox box = this.box.copy();
        box.add(offset);
        LittleRenderBox cube = new LittleRenderBox(grid, box, new LittleElement(LittleTilesRegistry.CLEAN.get().defaultBlockState(), color));
        float thickness = 1 / 32F;
        Axis axis = facing.axis;
        if (facing.positive) {
            cube.setMin(axis, cube.getMax(axis));
            cube.setMax(axis, cube.getMax(axis) + thickness);
        } else {
            cube.setMax(axis, cube.getMin(axis));
            cube.setMin(axis, cube.getMin(axis) - thickness);
        }
        return cube;
    }
    
    @Override
    public void place(Placement placement, LittleGrid grid, BlockPos pos, LittleStructure structure) throws LittleActionException {}
    
}
