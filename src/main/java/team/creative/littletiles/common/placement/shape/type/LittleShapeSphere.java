package team.creative.littletiles.common.placement.shape.type;

import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.config.HollowThicknessConfig;

public class LittleShapeSphere extends LittleShape<HollowThicknessConfig> {
    
    public LittleShapeSphere() {
        super(2);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, HollowThicknessConfig config) {
        LittleBox box = selection.getOverallBox();
        
        boolean hollow = config.hollow;
        LittleVec size = box.getSize();
        
        LittleVec invCenter = size.calculateInvertedCenter();
        invCenter.invert();
        
        double a = Math.pow(Math.max(1, size.x / 2), 2);
        double b = Math.pow(Math.max(1, size.y / 2), 2);
        double c = Math.pow(Math.max(1, size.z / 2), 2);
        
        double a2 = 1;
        double b2 = 1;
        double c2 = 1;
        
        int thickness = config.thickness;
        
        if (hollow && size.x > thickness * 2 && size.y > thickness * 2 && size.z > thickness * 2) {
            int all = size.x + size.y + size.z;
            
            double sizeXValue = (double) size.x / all;
            double sizeYValue = (double) size.y / all;
            double sizeZValue = (double) size.z / all;
            
            if (sizeXValue > 0.5)
                sizeXValue = 0.5;
            if (sizeYValue > 0.5)
                sizeYValue = 0.5;
            if (sizeZValue > 0.5)
                sizeZValue = 0.5;
            
            a2 = Math.pow(Math.max(1, (sizeXValue * all - thickness * 2) / 2), 2);
            b2 = Math.pow(Math.max(1, (sizeYValue * all - thickness * 2) / 2), 2);
            c2 = Math.pow(Math.max(1, (sizeZValue * all - thickness * 2) / 2), 2);
        } else
            hollow = false;
        
        boolean stretchedX = size.x % 2 == 0;
        boolean stretchedY = size.y % 2 == 0;
        boolean stretchedZ = size.z % 2 == 0;
        
        double centerX = size.x / 2;
        double centerY = size.y / 2;
        double centerZ = size.z / 2;
        
        LittleVec min = box.getMinVec();
        
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                for (int z = 0; z < size.z; z++) {
                    
                    double posX = x - centerX + (stretchedX ? 0.5 : 0);
                    double posY = y - centerY + (stretchedY ? 0.5 : 0);
                    double posZ = z - centerZ + (stretchedZ ? 0.5 : 0);
                    
                    double valueA = Math.pow(posX, 2) / a;
                    double valueB = Math.pow(posY, 2) / b;
                    double valueC = Math.pow(posZ, 2) / c;
                    
                    if (valueA + valueB + valueC <= 1) {
                        double valueA2 = Math.pow(posX, 2) / a2;
                        double valueB2 = Math.pow(posY, 2) / b2;
                        double valueC2 = Math.pow(posZ, 2) / c2;
                        if (!hollow || valueA2 + valueB2 + valueC2 > 1)
                            boxes.add(new LittleBox(new LittleVec(min.x + x, min.y + y, min.z + z)));
                    }
                }
            }
        }
        
        boxes.combineBoxesBlocks();
    }
}
