package team.creative.littletiles.common.placement.shape.type;

import team.creative.creativecore.common.util.math.geo.Ray3d;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;

public class LittleShapePolygon extends LittleShape<Void> {
    
    public LittleShapePolygon() {
        super(3);
    }
    
    public void generatePixels(LittleBoxes boxes, ShapePosition first, ShapePosition second, ShapePosition third) {
        Vec3d origin = first.getRelative(boxes.pos).getVec(first.getGrid());
        Vec3d secondVec = second.getRelative(boxes.pos).getVec(second.getGrid());
        Vec3d thirdVec = third.getRelative(boxes.pos).getVec(third.getGrid());
        double contextOffset = first.getGrid().halfPixelLength;
        origin.x += contextOffset;
        origin.y += contextOffset;
        origin.z += contextOffset;
        
        secondVec.x += contextOffset;
        secondVec.y += contextOffset;
        secondVec.z += contextOffset;
        
        thirdVec.x += contextOffset;
        thirdVec.y += contextOffset;
        thirdVec.z += contextOffset;
        Ray3d tRay = new Ray3d(origin, secondVec, false);
        int tStepCount = (int) Math.ceil(tRay.direction.length() / boxes.grid.pixelLength * 1.4);
        double tStepSize = 1D / (tStepCount - 1);
        
        Ray3d sRay = new Ray3d(origin, thirdVec, false);
        int sStepCount = (int) Math.ceil(sRay.direction.length() / boxes.grid.pixelLength * 1.4);
        double sStepSize = 1D / (sStepCount - 1);
        
        Vec3d temp = new Vec3d();
        Vec3d temp2 = new Vec3d();
        for (int tStep = 0; tStep < tStepCount; tStep++) {
            double t = tStep * tStepSize;
            for (int sStep = 0; sStep < sStepCount; sStep++) {
                double s = sStep * sStepSize;
                
                if (t > 1 || s > 1 || t + s > 1)
                    continue;
                
                temp.set(origin);
                temp2.set(tRay.direction.x * t, tRay.direction.y * t, tRay.direction.z * t);
                temp.add(temp2);
                temp2.set(sRay.direction.x * s, sRay.direction.y * s, sRay.direction.z * s);
                temp.add(temp2);
                boxes.add(new LittleBox(new LittleVec(boxes.grid, temp)));
            }
        }
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, Void config) {
        ShapePosition first = null;
        ShapePosition second = null;
        boolean remaining = true;
        for (ShapePosition pos : selection) {
            if (first == null)
                first = pos;
            else if (second == null)
                second = pos;
            else {
                generatePixels(boxes, first, second, pos);
                first = second;
                second = pos;
                remaining = false;
            }
        }
        if (remaining)
            if (second != null)
                generatePixels(boxes, first, second, second);
            else
                boxes.add(selection.getOverallBox());
    }
    
    @Override
    protected boolean requiresNoOverlap(ShapeSelection selection, Void config) {
        return true;
    }
}
