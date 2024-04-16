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
    public int min;
    public int max;
    
    public LittlePlaceBoxRelativeAxis(LittleBox box, StructureRelative relative, StructureDirectionalField relativeType, Axis axis, int min, int max) {
        super(box, relative, relativeType);
        this.axis = axis;
        this.min = min - box.getMin(axis);
        this.max = max - box.getMax(axis);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderBox(LittleGrid grid, LittleVec vec) {
        LittleRenderBox cube = super.getRenderBox(grid, vec);
        cube.setMin(axis, cube.getMin(axis) + grid.toVanillaGridF(min));
        cube.setMax(axis, cube.getMax(axis) + grid.toVanillaGridF(max));
        return cube;
    }
}
