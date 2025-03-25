package team.creative.littletiles.common.placement.shape.type;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.CornerCache;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.config.MatrixShapeConfig;

public class LittleShapeSlope extends LittleShape<MatrixShapeConfig> {
    
    public LittleShapeSlope() {
        super(2);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, MatrixShapeConfig config) {
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        CornerCache cache = box.new CornerCache(false);
        
        Axis axis = Axis.X.transform(config.matrix);
        Facing facing = Facing.DOWN.transform(config.matrix);
        Facing facing2 = Facing.NORTH.transform(config.matrix);
        
        BoxCorner corner = BoxCorner.getCornerUnsorted(axis.facing(false), facing, facing2);
        
        cache.setAbsolute(corner, facing.axis, box.get(facing.opposite()));
        cache.setAbsolute(corner.mirror(axis), facing.axis, box.get(facing.opposite()));
        
        cache.transform(config.matrix);
        
        box.setData(cache.getData());
        
        boxes.add(box);
    }
    
}
