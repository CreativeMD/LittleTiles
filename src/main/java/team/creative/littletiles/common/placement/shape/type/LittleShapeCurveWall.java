package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.interpolation.CubicInterpolation;
import team.creative.creativecore.common.util.math.interpolation.HermiteInterpolation;
import team.creative.creativecore.common.util.math.interpolation.Interpolation;
import team.creative.creativecore.common.util.math.interpolation.LinearInterpolation;
import team.creative.creativecore.common.util.math.vec.Vec2d;
import team.creative.creativecore.common.util.math.vec.VectorUtils;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.config.InterpolationAxisThicknessConfig;

public class LittleShapeCurveWall extends LittleShape<InterpolationAxisThicknessConfig> {
    
    public LittleShapeCurveWall() {
        super(2);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, InterpolationAxisThicknessConfig config) {
        
        LittleBox overallBox = selection.getOverallBox();
        Axis axis = config.axis;
        Axis one = axis.one();
        Axis two = axis.two();
        
        List<Vec2d> points = new ArrayList<>();
        double halfPixelSize = selection.grid.halfPixelLength;
        for (ShapePosition pos : selection)
            points.add(new Vec2d(pos.getVanillaGrid(one) + halfPixelSize, pos.getVanillaGrid(two) + halfPixelSize));
        
        int thickness = Math.max(0, config.thickness - 1);
        
        if (points.size() <= 1) {
            LittleBox box = selection.getOverallBox();
            box.growCentered(thickness);
            boxes.add(box);
            return;
        }
        
        Interpolation<Vec2d> interpolation = switch (config.interpolation) {
            case HERMITE -> new HermiteInterpolation<>(points.toArray(new Vec2d[0]));
            case CUBIC -> new CubicInterpolation<>(points.toArray(new Vec2d[0]));
            case LINEAR -> new LinearInterpolation<>(points.toArray(new Vec2d[0]));
        };
        
        Vec2d origin = new Vec2d(VectorUtils.get(one, boxes.pos), VectorUtils.get(two, boxes.pos));
        
        double pointTime = 1D / (points.size() - 1);
        for (int i = 0; i < points.size() - 1; i++) {
            Vec2d before = points.get(i);
            Vec2d end = points.get(i + 1);
            Vec2d middle = interpolation.valueAt(pointTime * (i + 0.5));
            
            double distance = before.distance(middle) + middle.distance(end);
            int stepCount = (int) Math.ceil(distance / boxes.grid.pixelLength * 2);
            double stepSize = pointTime / (stepCount - 1);
            for (int j = 0; j < stepCount; j++) {
                Vec2d vec = interpolation.valueAt(pointTime * i + stepSize * j);
                vec.sub(origin);
                LittleVec point = new LittleVec(0, 0, 0);
                point.set(one, boxes.grid.toGrid(vec.x));
                point.set(two, boxes.grid.toGrid(vec.y));
                LittleBox box = new LittleBox(point);
                box.setMin(axis, overallBox.getMin(axis));
                box.setMax(axis, overallBox.getMax(axis));
                box.growCentered(thickness);
                boxes.add(box);
            }
        }
    }
    
    @Override
    protected boolean requiresNoOverlap(ShapeSelection selection, InterpolationAxisThicknessConfig config) {
        return true;
    }
    
}
