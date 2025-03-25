package team.creative.littletiles.common.placement.shape.type;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.config.AxisHollowThicknessShapeConfig;

public class LittleShapeCylinder extends LittleShape<AxisHollowThicknessShapeConfig> {
    
    public LittleShapeCylinder() {
        super(2);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, AxisHollowThicknessShapeConfig config) {
        LittleBox box = selection.getOverallBox();
        
        boolean hollow = config.hollow;
        LittleVec size = box.getSize();
        
        int sizeA = size.x;
        int sizeB = size.z;
        
        if (config.axis == Axis.X) {
            sizeA = size.y;
            sizeB = size.z;
        } else if (config.axis == Axis.Z) {
            sizeA = size.x;
            sizeB = size.y;
        }
        
        //outer circle
        //Added D to the twos in order to get a decimal value
        double a = Math.pow(Math.max(1, sizeA / 2D), 2);
        double b = Math.pow(Math.max(1, sizeB / 2D), 2);
        
        double a2 = 1;
        double b2 = 1;
        
        int thickness = config.thickness;
        
        if (hollow && sizeA > thickness * 2 && sizeB > thickness * 2) {
            //Gets size for a circle that is 1 smaller than the thickness of the outer circle
            int sizeAValue = sizeA - thickness - 1;
            int sizeBValue = sizeB - thickness - 1;
            
            //inner circle
            a2 = Math.pow(Math.max(1, (sizeAValue) / 2D), 2);
            b2 = Math.pow(Math.max(1, (sizeBValue) / 2D), 2);
        } else
            hollow = false;
        
        boolean stretchedA = sizeA % 2 == 0;
        boolean stretchedB = sizeB % 2 == 0;
        
        double centerA = sizeA / 2;
        double centerB = sizeB / 2;
        
        LittleVec min = box.getMinVec();
        LittleVec max = box.getMaxVec();
        for (int incA = 0; incA < sizeA; incA++) {
            for (int incB = 0; incB < sizeB; incB++) {
                double posA = incA - centerA + (stretchedA ? 0.5 : 0);
                double posB = incB - centerB + (stretchedB ? 0.5 : 0);
                
                double valueA = Math.pow(posA, 2) / a;
                double valueB = Math.pow(posB, 2) / b;
                
                if (valueA + valueB <= 1) {
                    LittleBox toAdd = switch (config.axis) {
                        case X -> new LittleBox(min.x, min.y + incA, min.z + incB, max.x, min.y + incA + 1, min.z + incB + 1);
                        case Y -> new LittleBox(min.x + incA, min.y, min.z + incB, min.x + incA + 1, max.y, min.z + incB + 1);
                        case Z -> new LittleBox(min.x + incA, min.y + incB, min.z, min.x + incA + 1, min.y + incB + 1, max.z);
                    };
                    
                    if (hollow) {
                        double valueA2 = Math.pow(posA, 2) / a2;
                        double valueB2 = Math.pow(posB, 2) / b2;
                        //if the box is found in the inner circle, Do not add it.
                        if (!(valueA2 + valueB2 <= 1))
                            boxes.add(toAdd);
                    } else
                        boxes.add(toAdd);
                }
                
            }
            
        }
        
        boxes.combineBoxesBlocks();
    }
    
}
