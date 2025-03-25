package team.creative.littletiles.common.placement.shape.type;

import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;

public class LittleShapeTile extends LittleShapeSelectable<Void> {
    
    public LittleShapeTile() {
        super(1);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, Void config) {
        for (ShapePosition pos : selection)
            if (pos.result.isComplete())
                addBox(boxes, selection.inside, selection.grid, pos.result.parent, pos.result.box, pos.facing);
            else
                addBox(boxes, selection.inside, selection.grid, pos.ray.getBlockPos(), pos.facing);
    }
    
}
