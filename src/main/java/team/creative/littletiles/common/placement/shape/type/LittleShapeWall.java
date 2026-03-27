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
        
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        
        Axis toIgnore = config.axis;
        Axis oneIgnore = toIgnore.one();
        Axis twoIgnore = toIgnore.two();
        Axis longest = box.getSize(oneIgnore) > box.getSize(twoIgnore) ? oneIgnore : twoIgnore;
        
        LittleVec originalMinVec = originalMin.getRelative(boxes.pos);
        LittleVec originalMaxVec = originalMax.getRelative(boxes.pos);
        
        Facing startFacing = originalMin.facing;
        if (startFacing == null || startFacing.axis == toIgnore || box.getSize(startFacing.axis) == 1)
            startFacing = longest.facing(originalMinVec.get(longest) > originalMaxVec.get(longest));
        
        if ((originalMinVec.get(startFacing.axis) > originalMaxVec.get(startFacing.axis)) != startFacing.positive)
            startFacing = startFacing.opposite();
        
        Facing endFacing = originalMax.facing;
        if (endFacing == null || endFacing == startFacing || endFacing.axis == toIgnore || box.getSize(endFacing.axis) == 1)
            endFacing = startFacing.opposite();
        
        if ((originalMinVec.get(endFacing.axis) > originalMaxVec.get(endFacing.axis)) == endFacing.positive)
            endFacing = endFacing.opposite();
        
        int thickness = Math.max(0, config.thickness - 1);
        
        LittleBox minBox = new LittleBox(originalMinVec);
        LittleBox maxBox = new LittleBox(originalMaxVec);
        
        minBox.growAway(thickness, startFacing);
        maxBox.growAway(thickness, endFacing);
        
        box.growToInclude(minBox);
        box.growToInclude(maxBox);
        
        minBox.setMin(toIgnore, box.getMin(toIgnore));
        maxBox.setMin(toIgnore, box.getMin(toIgnore));
        minBox.setMax(toIgnore, box.getMax(toIgnore));
        maxBox.setMax(toIgnore, box.getMax(toIgnore));
        
        CornerCache cache = box.new CornerCache(false);
        
        LittleShapePillar.setStartAndEndBox(cache, startFacing, endFacing, minBox, maxBox, selection.inside);
        
        box.setData(cache.getData());
        
        boxes.add(box);
        
        return;
    }
}
