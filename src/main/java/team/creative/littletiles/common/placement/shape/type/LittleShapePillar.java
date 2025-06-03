package team.creative.littletiles.common.placement.shape.type;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.CornerCache;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.config.PillarShapeConfig;

public class LittleShapePillar extends LittleShape<PillarShapeConfig> {
    
    public static void setStartAndEndBox(CornerCache cache, Facing facing, Facing startFace, Facing endFace, LittleBox start, LittleBox end, boolean inside) {
        Axis axis = facing.axis; // startFace.axis is always the same or null in which case it will be the same
        Axis one = facing.one();
        Axis two = facing.two();
        
        if (startFace == null || startFace == facing.opposite())
            startFace = facing;
        
        BoxCorner[] corners = BoxCorner.faceCorners(startFace);
        for (int i = 0; i < corners.length; i++) {
            BoxCorner corner = corners[i];
            cache.setAbsolute(corner, one, corner.isFacingPositive(one) ? start.getMax(one) : start.getMin(one));
            cache.setAbsolute(corner, two, corner.isFacingPositive(two) ? start.getMax(two) : start.getMin(two));
        }
        
        if (endFace == null || startFace == endFace)
            endFace = facing.opposite();
        
        if (axis == endFace.axis) {
            corners = BoxCorner.faceCorners(endFace);
            for (int i = 0; i < corners.length; i++) {
                BoxCorner corner = corners[i];
                cache.setAbsolute(corner, one, corner.isFacingPositive(one) ? end.getMax(one) : end.getMin(one));
                cache.setAbsolute(corner, two, corner.isFacingPositive(two) ? end.getMax(two) : end.getMin(two));
            }
            return;
        }
        
        corners = BoxCorner.faceCorners(facing.opposite());
        Axis targetAxis = endFace.axis;
        Axis third = Axis.third(axis, targetAxis);
        for (int i = 0; i < corners.length; i++) {
            BoxCorner corner = corners[i];
            BoxCorner newCorner = BoxCorner.getCornerUnsorted(inside ? endFace : endFace.opposite(), axis.facing(inside != facing.positive == endFace.positive != corner
                    .isFacingPositive(targetAxis)), corner.getFacing(third));
            
            cache.setAbsolute(corner, axis, end.get(newCorner, axis));
            cache.setAbsolute(corner, one, end.get(newCorner, one));
            cache.setAbsolute(corner, two, end.get(newCorner, two));
        }
    }
    
    public LittleShapePillar() {
        super(2);
    }
    
    @Override
    public int maxAllowed() {
        return 2;
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, PillarShapeConfig config) {
        int thickness = Math.max(0, config.thickness - 1);
        
        PlacementPosition originalMin = selection.getFirst().copy();
        PlacementPosition originalMax = selection.getLast().copy();
        originalMin.convertTo(boxes.getGrid());
        originalMax.convertTo(boxes.getGrid());
        
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        
        Axis axis = box.getSize().getLongestAxis();
        
        LittleVec originalMinVec = originalMin.getRelative(boxes.pos);
        LittleVec originalMaxVec = originalMax.getRelative(boxes.pos);
        boolean facingPositive = originalMinVec.get(axis) > originalMaxVec.get(axis);
        
        boolean simple = config.simple;
        
        Facing startFacing = originalMin.facing;
        Facing endFacing = originalMax.facing;
        if (startFacing == null || endFacing == null) {
            simple = true;
            
            if (originalMax.facing != null)
                startFacing = originalMax.facing.opposite();
            else
                startFacing = axis.facing(facingPositive);
            if (endFacing == null)
                endFacing = startFacing.opposite();
        }
        
        Facing minFacing = startFacing;
        Facing maxFacing = endFacing;
        
        LittleBox minBox = new LittleBox(originalMinVec);
        LittleBox maxBox = new LittleBox(originalMaxVec);
        
        if (simple) {
            minFacing = null;
            maxFacing = null;
        } else {
            if (box.getSize(minFacing.axis) == 1 || (minFacing.positive ? minBox.getMax(minFacing.axis) > maxBox.getMax(minFacing.axis) : minBox.getMin(minFacing.axis) < maxBox
                    .getMin(minFacing.axis)))
                minFacing = null;
            if (box.getSize(maxFacing.axis) == 1 || (maxFacing.positive ? minBox.getMax(maxFacing.axis) < maxBox.getMax(maxFacing.axis) : minBox.getMin(maxFacing.axis) > maxBox
                    .getMin(maxFacing.axis)))
                maxFacing = null;
        }
        
        if (minFacing != null) {
            if (minFacing.axis != axis)
                axis = minFacing.axis;
        } else if (maxFacing != null && maxFacing.axis != axis)
            axis = maxFacing.axis;
        
        CornerCache cache = box.new CornerCache(false);
        
        if (minFacing != null && minFacing == maxFacing) {
            minFacing = Facing.get(axis, originalMinVec.get(axis) > originalMaxVec.get(axis));
            maxFacing = minFacing.opposite();
        }
        
        minBox.growAway(thickness, startFacing);
        maxBox.growAway(thickness, endFacing);
        
        box.growToInclude(minBox);
        box.growToInclude(maxBox);
        
        setStartAndEndBox(cache, axis.facing(facingPositive), minFacing, maxFacing, minBox, maxBox, selection.inside);
        
        box.setData(cache.getData());
        
        if (maxFacing == null)
            maxFacing = axis.facing(!facingPositive);
        
        Axis one = axis.one();
        Axis two = axis.two();
        switch (axis) {
            case X -> {
                if (maxFacing.positive != facingPositive == originalMinVec.get(one) < originalMaxVec.get(one)) {
                    box.setFlipped(one.facing(true), true);
                    box.setFlipped(one.facing(false), true);
                }
                if (maxFacing.positive == facingPositive == (originalMinVec.get(two) < originalMaxVec.get(two))) {
                    box.setFlipped(two.facing(true), true);
                    box.setFlipped(two.facing(false), true);
                }
            }
            case Y -> {
                if (maxFacing.positive == facingPositive == originalMinVec.get(one) < originalMaxVec.get(one)) {
                    box.setFlipped(one.facing(true), true);
                    box.setFlipped(one.facing(false), true);
                }
                if (maxFacing.positive != facingPositive == (originalMinVec.get(two) < originalMaxVec.get(two))) {
                    box.setFlipped(two.facing(true), true);
                    box.setFlipped(two.facing(false), true);
                }
            }
            case Z -> {
                if (maxFacing.positive != facingPositive == originalMinVec.get(one) < originalMaxVec.get(one)) {
                    box.setFlipped(one.facing(true), true);
                    box.setFlipped(one.facing(false), true);
                }
                if (maxFacing.positive != facingPositive == (originalMinVec.get(two) < originalMaxVec.get(two))) {
                    box.setFlipped(two.facing(true), true);
                    box.setFlipped(two.facing(false), true);
                }
            }
        }
        boxes.add(box);
    }
    
}
