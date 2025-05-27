package team.creative.littletiles.common.placement.shape.type;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.CornerCache;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.config.AxisThicknessShapeConfig;

public class LittleShapeWall extends LittleShape<AxisThicknessShapeConfig> {
    
    public LittleShapeWall() {
        super(2);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, AxisThicknessShapeConfig config) {
        
        PlacementPosition originalMin = selection.getFirst().copy();
        PlacementPosition originalMax = selection.getLast().copy();
        originalMin.convertTo(boxes.getGrid());
        originalMax.convertTo(boxes.getGrid());
        
        int thickness = Math.max(0, config.thickness - 1);
        
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        Axis toIgnore = config.axis;
        Axis oneIgnore = toIgnore.one();
        Axis twoIgnore = toIgnore.two();
        Axis axis = box.getSize(oneIgnore) > box.getSize(twoIgnore) ? oneIgnore : twoIgnore;
        Facing minFacing = originalMin.facing;
        Facing maxFacing = originalMax.facing;
        
        if (minFacing == null || minFacing.axis == toIgnore || box.getSize(minFacing.axis) == 1)
            minFacing = null;
        if (maxFacing == null || maxFacing.axis == toIgnore || box.getSize(maxFacing.axis) == 1)
            maxFacing = null;
        
        if (minFacing != null && minFacing.axis != axis)
            axis = minFacing.axis;
        
        CornerCache cache = box.new CornerCache(false);
        LittleVec originalMinVec = originalMin.getRelative(boxes.pos);
        LittleVec originalMaxVec = originalMax.getRelative(boxes.pos);
        
        LittleBox minBox = new LittleBox(originalMinVec);
        LittleBox maxBox = new LittleBox(originalMaxVec);
        
        if (minFacing != null && minFacing == maxFacing) {
            minFacing = Facing.get(axis, originalMinVec.get(axis) > originalMaxVec.get(axis));
            maxFacing = minFacing.opposite();
        }
        
        minBox.growAway(thickness, originalMin.facing);
        maxBox.growAway(thickness, originalMax.facing);
        
        box.growToInclude(minBox);
        box.growToInclude(maxBox);
        
        minBox.setMin(toIgnore, box.getMin(toIgnore));
        maxBox.setMin(toIgnore, box.getMin(toIgnore));
        minBox.setMax(toIgnore, box.getMax(toIgnore));
        maxBox.setMax(toIgnore, box.getMax(toIgnore));
        
        boolean facingPositive = originalMinVec.get(axis) > originalMaxVec.get(axis);
        
        LittleShapePillar.setStartAndEndBox(cache, axis.facing(facingPositive), minFacing, maxFacing, minBox, maxBox, selection.inside);
        
        box.setData(cache.getData());
        
        boxes.add(box);
        
        return;
    }
}
