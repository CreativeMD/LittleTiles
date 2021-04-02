package com.creativemd.littletiles.common.util.shape.type;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.interpolation.CubicInterpolation;
import com.creativemd.creativecore.common.utils.math.interpolation.HermiteInterpolation;
import com.creativemd.creativecore.common.utils.math.interpolation.Interpolation;
import com.creativemd.creativecore.common.utils.math.interpolation.LinearInterpolation;
import com.creativemd.creativecore.common.utils.math.vec.Vec3;
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

public class LittleShapeCurve extends LittleShape {
    
    private static String[] interpolationTypes = new String[] { "hermite", "cubic", "linear" };
    
    public LittleShapeCurve() {
        super(2);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        List<Vec3> points = new ArrayList<>();
        double halfPixelSize = selection.getContext().pixelSize * 0.5;
        for (ShapeSelectPos pos : selection) {
            LittleGridContext context = pos.getContext();
            points.add(new Vec3(pos.pos.getPosX() + halfPixelSize, pos.pos.getPosY() + halfPixelSize, pos.pos.getPosZ() + halfPixelSize));
        }
        
        if (points.size() <= 1) {
            boxes.add(selection.getOverallBox());
            return;
        }
        
        Interpolation<Vec3> interpolation;
        switch (selection.nbt.getInteger("interpolation")) {
        case 0:
            interpolation = new HermiteInterpolation<>(points.toArray(new Vec3[0]));
            break;
        case 1:
            interpolation = new CubicInterpolation<>(points.toArray(new Vec3[0]));
            break;
        default:
            interpolation = new LinearInterpolation<>(points.toArray(new Vec3[0]));
            break;
        }
        
        Vec3 origin = new Vec3(boxes.pos.getX(), boxes.pos.getY(), boxes.pos.getZ());
        
        int amount = 0;
        double pointTime = 1D / (points.size() - 1);
        double currentTime = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 before = points.get(i);
            Vec3 end = points.get(i + 1);
            
            double distance = before.distance(end);
            int stepCount = (int) Math.ceil(distance / boxes.context.pixelSize * 2);
            double stepSize = pointTime / (stepCount - 1);
            for (int j = 0; j < stepCount; j++) {
                Vec3 vec = interpolation.valueAt(pointTime * i + stepSize * j);
                boxes.add(new LittleBox(new LittleVec(boxes.context, (Vec3) vec.sub(origin))));
                amount++;
            }
        }
    }
    
    @Override
    public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
        list.add("interpolation: " + interpolationTypes[nbt.getInteger("interpolation")]);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
        List<GuiControl> controls = new ArrayList<>();
        controls
            .add(new GuiStateButton("interpolation", nbt.getInteger("interpolation"), 0, 30, 40, 7, interpolationTypes));
        return controls;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
        nbt.setInteger("interpolation", ((GuiStateButton) gui.get("interpolation")).getState());
    }
    
    @Override
    public void rotate(NBTTagCompound nbt, Rotation rotation) {}
    
    @Override
    public void flip(NBTTagCompound nbt, Axis axis) {}
    
}
