package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.interpolation.CubicInterpolation;
import team.creative.creativecore.common.util.math.interpolation.HermiteInterpolation;
import team.creative.creativecore.common.util.math.interpolation.Interpolation;
import team.creative.creativecore.common.util.math.interpolation.LinearInterpolation;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeSelection;
import team.creative.littletiles.common.placement.shape.ShapeSelection.ShapeSelectPos;

public class LittleShapeCurve extends LittleShape {
    
    private static String[] interpolationTypes = new String[] { "hermite", "cubic", "linear" };
    
    public LittleShapeCurve() {
        super(2);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        List<Vec3d> points = new ArrayList<>();
        double halfPixelSize = selection.getGrid().halfPixelLength;
        for (ShapeSelectPos pos : selection)
            points.add(new Vec3d(pos.pos.getPosX() + halfPixelSize, pos.pos.getPosY() + halfPixelSize, pos.pos.getPosZ() + halfPixelSize));
        
        int thickness = Math.max(0, selection.getNBT().getInt("thickness") - 1);
        
        if (points.size() <= 1) {
            LittleBox box = selection.getOverallBox();
            box.growCentered(thickness);
            boxes.add(box);
            return;
        }
        
        Interpolation<Vec3d> interpolation;
        switch (selection.getNBT().getInt("interpolation")) {
            case 0:
                interpolation = new HermiteInterpolation<>(points.toArray(new Vec3d[0]));
                break;
            case 1:
                interpolation = new CubicInterpolation<>(points.toArray(new Vec3d[0]));
                break;
            default:
                interpolation = new LinearInterpolation<>(points.toArray(new Vec3d[0]));
                break;
        }
        
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
    public boolean requiresNoOverlap(ShapeSelection selection) {
        return true;
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {
        list.add(Component.translatable("gui.interpolation").append(": ").append(Component.translatable("gui." + interpolationTypes[nbt.getInt("interpolation")])));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        List<GuiControl> controls = new ArrayList<>();
        controls.add(new GuiSteppedSlider("thickness", nbt.getInt("thickness"), 1, grid.count));
        
        controls.add(new GuiStateButton("interpolation", nbt.getInt("interpolation"), interpolationTypes));
        return controls;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {
        GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
        nbt.putInt("thickness", (int) slider.value);
        
        nbt.putInt("interpolation", ((GuiStateButton) gui.get("interpolation")).getState());
    }
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {}
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {}
    
}
