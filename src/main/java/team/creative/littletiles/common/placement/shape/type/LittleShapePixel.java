package team.creative.littletiles.common.placement.shape.type;

import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.shape.LittleShape;

public class LittleShapePixel extends LittleShape<Void> {
    
    public LittleShapePixel() {
        super(1);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, Void config) {
        for (ShapePosition pos : selection)
            boxes.addBox(pos.getGrid(), selection.pos, new LittleBox(pos.getVec()));
    }
    
}
