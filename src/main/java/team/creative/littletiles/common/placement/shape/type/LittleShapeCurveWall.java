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
import team.creative.creativecore.common.util.math.vec.Vec2d;
import team.creative.creativecore.common.util.math.vec.VectorUtils;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeSelection;
import team.creative.littletiles.common.placement.shape.ShapeSelection.ShapeSelectPos;

public class LittleShapeCurveWall extends LittleShape {
    
    private static String[] interpolationTypes = new String[] { "hermite", "cubic", "linear" };
    
    public LittleShapeCurveWall() {
        super(2);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        int direction = selection.getNBT().getInt("direction");
        
        LittleBox overallBox = selection.getOverallBox();
        Axis axis = direction == 0 ? Axis.Y : direction == 1 ? Axis.X : Axis.Z;
        Axis one = axis.one();
        Axis two = axis.two();
        
        List<Vec2d> points = new ArrayList<>();
        double halfPixelSize = selection.getGrid().halfPixelLength;
        for (ShapeSelectPos pos : selection)
            points.add(new Vec2d(pos.pos.getVanillaGrid(one) + halfPixelSize, pos.pos.getVanillaGrid(two) + halfPixelSize));
        
        int thickness = Math.max(0, selection.getNBT().getInt("thickness") - 1);
        
        if (points.size() <= 1) {
            LittleBox box = selection.getOverallBox();
            box.growCentered(thickness);
            boxes.add(box);
            return;
        }
        
        Interpolation<Vec2d> interpolation;
        switch (selection.getNBT().getInt("interpolation")) {
            case 0:
                interpolation = new HermiteInterpolation<>(points.toArray(new Vec2d[0]));
                break;
            case 1:
                interpolation = new CubicInterpolation<>(points.toArray(new Vec2d[0]));
                break;
            default:
                interpolation = new LinearInterpolation<>(points.toArray(new Vec2d[0]));
                break;
        }
        
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
    public boolean requiresNoOverlap(ShapeSelection selection) {
        return true;
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {
        list.add(Component.translatable("gui.interpolation").append(": ").append(Component.translatable("gui." + interpolationTypes[nbt.getInt("interpolation")])));
        
        int facing = nbt.getInt("direction");
        String text;
        switch (facing) {
            case 1:
                text = "x";
                break;
            case 2:
                text = "z";
                break;
            default:
                text = "y";
                break;
        }
        list.add(Component.translatable("gui.facing").append(": ").append(Component.translatable("gui.axis." + text)));
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        List<GuiControl> controls = new ArrayList<>();
        controls.add(new GuiSteppedSlider("thickness", nbt.getInt("thickness"), 1, grid.count));
        
        controls.add(new GuiStateButton("interpolation", nbt.getInt("interpolation"), interpolationTypes));
        controls.add(new GuiStateButton("direction", nbt.getInt("direction"), "facing: y", "facing: x", "facing: z"));
        return controls;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {
        GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
        nbt.putInt("thickness", (int) slider.value);
        
        GuiStateButton state = (GuiStateButton) gui.get("direction");
        nbt.putInt("direction", state.getState());
        
        nbt.putInt("interpolation", ((GuiStateButton) gui.get("interpolation")).getState());
    }
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {
        int direction = nbt.getInt("direction");
        if (rotation.axis != Axis.Y)
            direction = 0;
        else {
            if (direction == 1)
                direction = 2;
            else
                direction = 1;
        }
        
        nbt.putInt("direction", direction);
    }
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {}
    
}
