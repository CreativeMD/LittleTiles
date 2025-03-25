package team.creative.littletiles.common.placement.shape.type;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.config.FacingShapeConfig;

public class LittleShapePyramid extends LittleShape<FacingShapeConfig> {
    
    public LittleShapePyramid() {
        super(2);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, FacingShapeConfig config) {
        LittleBox box = selection.getOverallBox();
        Facing facing = config.facing;
        Axis axis = facing.axis;
        int minAxis = box.getMin(axis);
        int maxAxis = box.getMax(axis);
        
        Axis one = axis.one();
        Axis two = axis.two();
        
        int minOne = box.getMin(one);
        int minTwo = box.getMin(two);
        int maxOne = box.getMax(one);
        int maxTwo = box.getMax(two);
        
        int counter = 0;
        
        if (facing.positive)
            for (int i = minAxis; i < maxAxis; i++) {
                LittleBox toAdd = new LittleBox(i, i, i, i + 1, i + 1, i + 1);
                toAdd.setMin(one, Math.min(minOne + counter, maxOne - counter));
                toAdd.setMin(two, Math.min(minTwo + counter, maxTwo - counter));
                toAdd.setMax(one, Math.max(minOne + counter, maxOne - counter));
                toAdd.setMax(two, Math.max(minTwo + counter, maxTwo - counter));
                boxes.add(toAdd);
                counter++;
            }
        else
            for (int i = maxAxis - 1; i >= minAxis; i--) {
                LittleBox toAdd = new LittleBox(i, i, i, i + 1, i + 1, i + 1);
                toAdd.setMin(one, Math.min(minOne + counter, maxOne - counter));
                toAdd.setMin(two, Math.min(minTwo + counter, maxTwo - counter));
                toAdd.setMax(one, Math.max(minOne + counter, maxOne - counter));
                toAdd.setMax(two, Math.max(minTwo + counter, maxTwo - counter));
                boxes.add(toAdd);
                counter++;
            }
    }
    
}
