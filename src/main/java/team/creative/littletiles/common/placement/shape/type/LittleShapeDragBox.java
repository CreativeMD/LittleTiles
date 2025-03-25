package team.creative.littletiles.common.placement.shape.type;

import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.config.HollowThicknessConfig;

public class LittleShapeDragBox extends LittleShape<HollowThicknessConfig> {
    
    public LittleShapeDragBox() {
        super(2);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, HollowThicknessConfig config) {
        LittleBox box = selection.getOverallBox();
        if (config.hollow) {
            int thickness = config.thickness;
            LittleVec size = box.getSize();
            if (thickness * 2 >= size.x || thickness * 2 >= size.y || thickness * 2 >= size.z)
                boxes.add(box);
            else {
                boxes.add(new LittleBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ + thickness));
                boxes.add(new LittleBox(box.minX, box.minY + thickness, box.minZ + thickness, box.minX + thickness, box.maxY - thickness, box.maxZ - thickness));
                boxes.add(new LittleBox(box.maxX - thickness, box.minY + thickness, box.minZ + thickness, box.maxX, box.maxY - thickness, box.maxZ - thickness));
                boxes.add(new LittleBox(box.minX, box.minY, box.minZ + thickness, box.maxX, box.minY + thickness, box.maxZ - thickness));
                boxes.add(new LittleBox(box.minX, box.maxY - thickness, box.minZ + thickness, box.maxX, box.maxY, box.maxZ - thickness));
                boxes.add(new LittleBox(box.minX, box.minY, box.maxZ - thickness, box.maxX, box.maxY, box.maxZ));
            }
        } else
            boxes.add(box);
    }
    
}
