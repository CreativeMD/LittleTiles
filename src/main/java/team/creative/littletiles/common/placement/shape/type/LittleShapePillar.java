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
    
    public static void setStartAndEndBox(CornerCache cache, Facing startFace, Facing endFace, LittleBox start, LittleBox end, boolean inside) {
        Axis axis = startFace.axis; // startFace.axis is always the same or null in which case it will be the same
        Axis one = startFace.one();
        Axis two = startFace.two();
        
        BoxCorner[] corners = BoxCorner.faceCorners(startFace);
        for (int i = 0; i < corners.length; i++) {
            BoxCorner corner = corners[i];
            cache.setAbsolute(corner, one, corner.isFacingPositive(one) ? start.getMax(one) : start.getMin(one));
            cache.setAbsolute(corner, two, corner.isFacingPositive(two) ? start.getMax(two) : start.getMin(two));
        }
        
        if (axis == endFace.axis) {
            corners = BoxCorner.faceCorners(endFace);
            for (int i = 0; i < corners.length; i++) {
                BoxCorner corner = corners[i];
                cache.setAbsolute(corner, one, corner.isFacingPositive(one) ? end.getMax(one) : end.getMin(one));
                cache.setAbsolute(corner, two, corner.isFacingPositive(two) ? end.getMax(two) : end.getMin(two));
            }
            return;
        }
        
        corners = BoxCorner.faceCorners(startFace.opposite());
        Axis targetAxis = endFace.axis;
        Axis third = Axis.third(axis, targetAxis);
        for (int i = 0; i < corners.length; i++) {
            BoxCorner corner = corners[i];
            BoxCorner newCorner = BoxCorner.getCornerUnsorted(inside ? endFace : endFace.opposite(), axis.facing(inside != (startFace.positive != endFace.positive) != corner
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
        PlacementPosition originalMin = selection.getFirst().copy();
        PlacementPosition originalMax = selection.getLast().copy();
        originalMin.convertTo(boxes.getGrid());
        originalMax.convertTo(boxes.getGrid());
        
        LittleVec originalMinVec = originalMin.getRelative(boxes.pos);
        LittleVec originalMaxVec = originalMax.getRelative(boxes.pos);
        
        LittleBox minBox = new LittleBox(originalMin.getRelative(boxes.pos));
        LittleBox maxBox = new LittleBox(originalMax.getRelative(boxes.pos));
        
        Facing startFacing = originalMin.facing == null ? null : originalMin.facing.opposite();
        Facing endFacing = originalMax.facing == null ? null : originalMax.facing.opposite();
        
        boolean simple = config.simple;
        
        if (startFacing == null)
            simple = true;
        
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        
        boolean facingPositive = false;
        if (!simple) {
            if (endFacing == null)
                endFacing = startFacing.opposite();
            
            facingPositive = minBox.get(startFacing) < maxBox.get(startFacing.opposite());
            
            if (startFacing.positive == facingPositive || minBox.get(startFacing) == maxBox.get(startFacing.opposite()) || endFacing == startFacing)
                simple = true;
        }
        
        if (simple) {
            Axis axis = box.getSize().getLongestAxis();
            facingPositive = originalMinVec.get(axis) < originalMaxVec.get(axis);
            
            startFacing = axis.facing(!facingPositive);
            endFacing = axis.facing(facingPositive);
        }
        
        System.out.println(startFacing + " " + endFacing + " " + simple + " " + facingPositive);
        
        int width = Math.max(0, config.width - 1);
        int height = Math.max(0, config.height - 1);
        
        int invWidth = width / 2;
        int growWidth = width - invWidth;
        int invHeight = height / 2;
        int growHeight = height - invHeight;
        minBox.setMin(startFacing.one(), minBox.getMin(startFacing.one()) - invWidth);
        minBox.setMax(startFacing.one(), minBox.getMax(startFacing.one()) + growWidth);
        minBox.setMin(startFacing.two(), minBox.getMin(startFacing.two()) - invHeight);
        minBox.setMax(startFacing.two(), minBox.getMax(startFacing.two()) + growHeight);
        
        Axis widthAxis = startFacing.one() == endFacing.axis ? startFacing.axis : startFacing.one();
        Axis heightAxis = startFacing.two() == endFacing.axis ? startFacing.axis : startFacing.two();
        
        maxBox.setMin(widthAxis, maxBox.getMin(widthAxis) - invWidth);
        maxBox.setMax(widthAxis, maxBox.getMax(widthAxis) + growWidth);
        maxBox.setMin(heightAxis, maxBox.getMin(heightAxis) - invHeight);
        maxBox.setMax(heightAxis, maxBox.getMax(heightAxis) + growHeight);
        
        box.growToInclude(minBox);
        box.growToInclude(maxBox);
        
        CornerCache cache = box.new CornerCache(false);
        
        setStartAndEndBox(cache, startFacing, endFacing, minBox, maxBox, selection.inside);
        
        box.setData(cache.getData());
        
        Axis one = startFacing.one();
        Axis two = startFacing.two();
        switch (startFacing.axis) {
            case X -> {
                if (endFacing.positive != facingPositive == originalMinVec.get(one) < originalMaxVec.get(one)) {
                    box.setFlipped(one.facing(true), true);
                    box.setFlipped(one.facing(false), true);
                }
                if (endFacing.positive == facingPositive == (originalMinVec.get(two) < originalMaxVec.get(two))) {
                    box.setFlipped(two.facing(true), true);
                    box.setFlipped(two.facing(false), true);
                }
            }
            case Y -> {
                if (endFacing.positive == facingPositive == originalMinVec.get(one) < originalMaxVec.get(one)) {
                    box.setFlipped(one.facing(true), true);
                    box.setFlipped(one.facing(false), true);
                }
                if (endFacing.positive != facingPositive == (originalMinVec.get(two) < originalMaxVec.get(two))) {
                    box.setFlipped(two.facing(true), true);
                    box.setFlipped(two.facing(false), true);
                }
            }
            case Z -> {
                if (endFacing.positive != facingPositive == originalMinVec.get(one) < originalMaxVec.get(one)) {
                    box.setFlipped(one.facing(true), true);
                    box.setFlipped(one.facing(false), true);
                }
                if (endFacing.positive != facingPositive == (originalMinVec.get(two) < originalMaxVec.get(two))) {
                    box.setFlipped(two.facing(true), true);
                    box.setFlipped(two.facing(false), true);
                }
            }
        }
        boxes.add(box);
    }
    
}
