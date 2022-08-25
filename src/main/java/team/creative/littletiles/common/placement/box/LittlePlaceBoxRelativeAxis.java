package team.creative.littletiles.common.placement.box;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class LittlePlaceBoxRelativeAxis extends LittlePlaceBoxRelative {
    
    public Axis axis;
    
    public LittlePlaceBoxRelativeAxis(LittleBox box, StructureRelative relative, StructureDirectionalField relativeType, Axis axis) {
        super(box, relative, relativeType);
        this.axis = axis;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderBox(LittleGrid grid, LittleVec vec) {
        LittleRenderBox cube = super.getRenderBox(grid, vec);
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
        return cube;
    }
}
