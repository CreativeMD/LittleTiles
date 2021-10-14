package team.creative.littletiles.common.placement.box;

import java.util.List;

import com.creativemd.littletiles.client.render.tile.LittleRenderBox;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class LittlePlaceBoxRelativeAxis extends LittlePlaceBoxRelative {
    
    public Axis axis;
    
    public LittlePlaceBoxRelativeAxis(LittleBox box, StructureRelative relative, StructureDirectionalField relativeType, Axis axis) {
        super(box, relative, relativeType);
        this.axis = axis;
    }
    
    @Override
    public List<LittleRenderBox> getRenderBoxes(LittleGrid grid) {
        List<LittleRenderBox> cubes = super.getRenderBoxes(grid);
        LittleRenderBox cube = cubes.get(0);
        int max = 40 * grid.count;
        int min = -max;
        switch (axis) {
        case X:
            cube.minX = min;
            cube.maxX = max;
            break;
        case Y:
            cube.minY = min;
            cube.maxY = max;
            break;
        case Z:
            cube.minZ = min;
            cube.maxZ = max;
            break;
        default:
            break;
        }
        cubes.add(cube);
        return cubes;
    }
}
