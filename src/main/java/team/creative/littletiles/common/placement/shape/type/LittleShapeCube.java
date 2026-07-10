package team.creative.littletiles.common.placement.shape.type;

import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.config.BrushSizeShapeConfig;

public class LittleShapeCube extends LittleShape<BrushSizeShapeConfig> {
    
    public LittleShapeCube() {
        super(1);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, BrushSizeShapeConfig config) {
        for (ShapePosition pos : selection) {
            LittleBox box = new LittleBox(-config.size, -config.size, -config.size, config.size, config.size, config.size);
            box.add(pos.getMin().getVec());
            boxes.addBox(selection.grid, pos.getPos(), box);
        }
    }
    
}
