package team.creative.littletiles.common.placement.box;

import java.util.ArrayList;
import java.util.List;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.Placement.PlacementBlock;
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
    public List<LittleRenderBox> getRenderBoxes(LittleGrid grid) {
        List<LittleRenderBox> cubes = new ArrayList<>();
        LittleRenderBox cube = new LittleRenderBox(box.getBox(grid), box, LittleTiles.CLEAN, 0);
        cube.setColor(color);
        float thickness = 1 / 32F;
        Axis axis = facing.axis;
        if (facing.positive) {
            cube.setMin(axis, cube.getMax(axis));
            cube.setMax(axis, cube.getMax(axis) + thickness);
        } else {
            cube.setMax(axis, cube.getMin(axis));
            cube.setMin(axis, cube.getMin(axis) - thickness);
        }
        cubes.add(cube);
        return cubes;
    }
    
    @Override
    public void place(Placement placement, PlacementBlock block, LittleStructure structure) throws LittleActionException {}
    
}
