package team.creative.littletiles.common.placement.shape.type;

import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.geo.Ray3d;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeSelection;
import team.creative.littletiles.common.placement.shape.ShapeSelection.ShapeSelectPos;

public class LittleShapePolygon extends LittleShape {
    
    public LittleShapePolygon() {
        super(3);
    }
    
    public void generatePixels(LittleBoxes boxes, ShapeSelectPos first, ShapeSelectPos second, ShapeSelectPos third) {
        Vec3d origin = first.pos.getRelative(boxes.pos).getVec(first.getGrid());
        Vec3d secondVec = second.pos.getRelative(boxes.pos).getVec(second.getGrid());
        Vec3d thirdVec = third.pos.getRelative(boxes.pos).getVec(third.getGrid());
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
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        ShapeSelectPos first = null;
        ShapeSelectPos second = null;
        boolean remaining = true;
        for (ShapeSelectPos pos : selection) {
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
    public boolean requiresNoOverlap(ShapeSelection selection) {
        return true;
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {}
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {
        
    }
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {}
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {}
    
}
