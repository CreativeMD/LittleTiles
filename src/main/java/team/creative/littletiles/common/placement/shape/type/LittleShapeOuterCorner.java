package team.creative.littletiles.common.placement.shape.type;

import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.CornerCache;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.config.CornerShapeConfig;

public class LittleShapeOuterCorner extends LittleShape<CornerShapeConfig> {
    
    public LittleShapeOuterCorner() {
        super(2);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, CornerShapeConfig config) {
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        CornerCache cache = box.new CornerCache(false);
        
        BoxCorner from = BoxCorner.EDS.transform(config.matrix);
        BoxCorner to = BoxCorner.EUS.transform(config.matrix);
        
        Facing facing = from.facingTo(to);
        BoxFace face = BoxFace.get(facing);
        boolean flipped = false;
        if (face.getCornerInQuestion(false, false) != to && face.getCornerInQuestion(true, false) != to)
            flipped = true;
        
        int value = box.get(facing.opposite());
        
        cache.setAbsolute(to, facing.axis, box.get(facing.opposite()));
        
        cache.setAbsolute(to, facing.axis, value);
        cache.setAbsolute(to.mirror(facing.one()), facing.axis, value);
        cache.setAbsolute(to.mirror(facing.two()), facing.axis, value);
        
        box.setData(cache.getData());
        if (config.secondMode)
            flipped = !flipped;
        box.setFlipped(facing, flipped);
        boxes.add(box);
    }
    
}
