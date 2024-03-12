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
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.CornerCache;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class LittleShapeWall extends LittleShape {
    
    public LittleShapeWall() {
        super(2);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        int direction = selection.getNBT().getInt("direction");
        PlacementPosition originalMin = selection.getFirst().pos.copy();
        PlacementPosition originalMax = selection.getLast().pos.copy();
        originalMin.convertTo(boxes.getGrid());
        originalMax.convertTo(boxes.getGrid());
        
        int thickness = Math.max(0, selection.getNBT().getInt("thickness") - 1);
        
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        Axis toIgnore = direction == 0 ? Axis.Y : direction == 1 ? Axis.X : Axis.Z;
        Axis oneIgnore = toIgnore.one();
        Axis twoIgnore = toIgnore.two();
        Axis axis = box.getSize(oneIgnore) > box.getSize(twoIgnore) ? oneIgnore : twoIgnore;
        Facing minFacing = originalMin.facing;
        Facing maxFacing = originalMax.facing;
        
        if (minFacing.axis == toIgnore || box.getSize(minFacing.axis) == 1)
            minFacing = null;
        if (maxFacing.axis == toIgnore || box.getSize(maxFacing.axis) == 1)
            maxFacing = null;
        
        if (minFacing != null && minFacing.axis != axis)
            axis = minFacing.axis;
        
        CornerCache cache = box.new CornerCache(false);
        LittleVec originalMinVec = originalMin.getRelative(boxes.pos);
        LittleVec originalMaxVec = originalMax.getRelative(boxes.pos);
        
        LittleBox minBox = new LittleBox(originalMinVec);
        LittleBox maxBox = new LittleBox(originalMaxVec);
        
        if (minFacing != null && minFacing == maxFacing) {
            minFacing = Facing.get(axis, originalMinVec.get(axis) > originalMaxVec.get(axis));
            maxFacing = minFacing.opposite();
        }
        
        minBox.growAway(thickness, originalMin.facing);
        maxBox.growAway(thickness, originalMax.facing);
        
        box.growToInclude(minBox);
        box.growToInclude(maxBox);
        
        minBox.setMin(toIgnore, box.getMin(toIgnore));
        maxBox.setMin(toIgnore, box.getMin(toIgnore));
        minBox.setMax(toIgnore, box.getMax(toIgnore));
        maxBox.setMax(toIgnore, box.getMax(toIgnore));
        
        boolean facingPositive = originalMinVec.get(axis) > originalMaxVec.get(axis);
        
        LittleShapePillar.setStartAndEndBox(cache, axis.facing(facingPositive), minFacing, maxFacing, minBox, maxBox);
        
        box.setData(cache.getData());
        
        boxes.add(box);
        
        return;
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {
        list.add(Component.translatable("gui.thickness").append(": " + nbt.getInt("thickness")).append(Component.translatable("gui.pixel.length")));
        
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
