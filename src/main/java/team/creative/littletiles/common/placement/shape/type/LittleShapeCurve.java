package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.util.math.interpolation.CubicInterpolation;
import team.creative.creativecore.common.util.math.interpolation.HermiteInterpolation;
import team.creative.creativecore.common.util.math.interpolation.Interpolation;
import team.creative.creativecore.common.util.math.interpolation.LinearInterpolation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.config.InterpolationThicknessConfig;

public class LittleShapeCurve extends LittleShape<InterpolationThicknessConfig> {
    
    public LittleShapeCurve() {
        super(2);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, InterpolationThicknessConfig config) {
        List<Vec3d> points = new ArrayList<>();
        double halfPixelSize = selection.grid.halfPixelLength;
        for (ShapePosition pos : selection)
            points.add(new Vec3d(pos.getPosX() + halfPixelSize, pos.getPosY() + halfPixelSize, pos.getPosZ() + halfPixelSize));
        
        int thickness = Math.max(0, config.thickness - 1);
        
        if (points.size() <= 1) {
            LittleBox box = selection.getOverallBox();
            box.growCentered(thickness);
            boxes.add(box);
            return;
        }
        
        Interpolation<Vec3d> interpolation = switch (config.interpolation) {
            case HERMITE -> new HermiteInterpolation<>(points.toArray(new Vec3d[0]));
            case CUBIC -> new CubicInterpolation<>(points.toArray(new Vec3d[0]));
            case LINEAR -> new LinearInterpolation<>(points.toArray(new Vec3d[0]));
        };
        
        Vec3d origin = new Vec3d(boxes.pos.getX(), boxes.pos.getY(), boxes.pos.getZ());
        
        double pointTime = 1D / (points.size() - 1);
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3d before = points.get(i);
            Vec3d end = points.get(i + 1);
            Vec3d middle = interpolation.valueAt(pointTime * (i + 0.5));
            
            double distance = before.distance(middle) + middle.distance(end);
            int stepCount = (int) Math.ceil(distance / boxes.grid.pixelLength * 2);
            double stepSize = pointTime / (stepCount - 1);
            for (int j = 0; j < stepCount; j++) {
                Vec3d vec = interpolation.valueAt(pointTime * i + stepSize * j);
                vec.sub(origin);
                LittleBox box = new LittleBox(new LittleVec(boxes.grid, vec));
                box.growCentered(thickness);
                boxes.add(box);
            }
        }
    }
    
    @Override
    protected boolean requiresNoOverlap(ShapeSelection selection, InterpolationThicknessConfig config) {
        return true;
    }
    
    public static enum ShapeInterpolation {
        HERMITE,
        CUBIC,
        LINEAR;
        
        public Component translatable() {
            return Component.translatable("gui.interpolation" + name().toLowerCase());
        }
    }
    
}
