package com.creativemd.littletiles.common.util.shape.type;

import java.util.Collections;
import java.util.List;

import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.geo.Ray3d;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.shape.LittleShape;
import com.creativemd.littletiles.common.util.shape.ShapeSelection;
import com.creativemd.littletiles.common.util.shape.ShapeSelection.ShapeSelectPos;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleShapePolygon extends LittleShape {
    
    public LittleShapePolygon() {
        super(3);
    }
    
    public void generatePixels(LittleBoxes boxes, ShapeSelectPos first, ShapeSelectPos second, ShapeSelectPos third) {
        Vector3d origin = first.pos.getRelative(boxes.pos).getVector(first.getContext());
        Vector3d secondVec = second.pos.getRelative(boxes.pos).getVector(second.getContext());
        Vector3d thirdVec = third.pos.getRelative(boxes.pos).getVector(third.getContext());
        double contextOffset = first.getContext().pixelSize * 0.5;
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
        int tStepCount = (int) Math.ceil(tRay.direction.length() / boxes.context.pixelSize * 1.4);
        double tStepSize = 1D / (tStepCount - 1);
        
        Ray3d sRay = new Ray3d(origin, thirdVec, false);
        int sStepCount = (int) Math.ceil(sRay.direction.length() / boxes.context.pixelSize * 1.4);
        double sStepSize = 1D / (sStepCount - 1);
        
        Vector3d temp = new Vector3d();
        Vector3d temp2 = new Vector3d();
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
                boxes.add(new LittleBox(new LittleVec(boxes.context, temp)));
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
    public boolean requiresNoOverlap() {
        return true;
    }
    
    @Override
    public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
        
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
        
    }
    
    @Override
    public void rotate(NBTTagCompound nbt, Rotation rotation) {}
    
    @Override
    public void flip(NBTTagCompound nbt, Axis axis) {}
    
}
