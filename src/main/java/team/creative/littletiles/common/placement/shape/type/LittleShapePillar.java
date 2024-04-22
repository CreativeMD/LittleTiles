package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.BoxCorner;
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

public class LittleShapePillar extends LittleShape {
    
    public static void setStartAndEndBox(CornerCache cache, Facing facing, Facing startFace, Facing endFace, LittleBox start, LittleBox end, boolean inside) {
        Axis axis = facing.axis; // startFace.axis is always the same or null in which case it will be the same
        Axis one = facing.one();
        Axis two = facing.two();
        
        if (startFace == null || startFace == facing.opposite())
            startFace = facing;
        
        BoxCorner[] corners = BoxCorner.faceCorners(startFace);
        for (int i = 0; i < corners.length; i++) {
            BoxCorner corner = corners[i];
            cache.setAbsolute(corner, one, corner.isFacingPositive(one) ? start.getMax(one) : start.getMin(one));
            cache.setAbsolute(corner, two, corner.isFacingPositive(two) ? start.getMax(two) : start.getMin(two));
        }
        
        if (endFace == null || startFace == endFace)
            endFace = facing.opposite();
        
        if (axis == endFace.axis) {
            corners = BoxCorner.faceCorners(endFace);
            for (int i = 0; i < corners.length; i++) {
                BoxCorner corner = corners[i];
                cache.setAbsolute(corner, one, corner.isFacingPositive(one) ? end.getMax(one) : end.getMin(one));
                cache.setAbsolute(corner, two, corner.isFacingPositive(two) ? end.getMax(two) : end.getMin(two));
            }
            return;
        }
        
        corners = BoxCorner.faceCorners(facing.opposite());
        Axis targetAxis = endFace.axis;
        Axis third = Axis.third(axis, targetAxis);
        for (int i = 0; i < corners.length; i++) {
            BoxCorner corner = corners[i];
            BoxCorner newCorner = BoxCorner.getCornerUnsorted(inside ? endFace : endFace.opposite(), axis.facing(inside != facing.positive == endFace.positive != corner
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
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        int thickness = Math.max(0, selection.getNBT().getInt("thickness") - 1);
        
        PlacementPosition originalMin = selection.getFirst().pos.copy();
        PlacementPosition originalMax = selection.getLast().pos.copy();
        originalMin.convertTo(boxes.getGrid());
        originalMax.convertTo(boxes.getGrid());
        
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        
        Axis axis = box.getSize().getLongestAxis();
        
        Facing minFacing = originalMin.facing;
        Facing maxFacing = originalMax.facing;
        
        LittleVec originalMinVec = originalMin.getRelative(boxes.pos);
        LittleVec originalMaxVec = originalMax.getRelative(boxes.pos);
        
        LittleBox minBox = new LittleBox(originalMinVec);
        LittleBox maxBox = new LittleBox(originalMaxVec);
        
        if (selection.getNBT().getBoolean("simple")) {
            minFacing = null;
            maxFacing = null;
        } else {
            if (box.getSize(minFacing.axis) == 1 || (minFacing.positive ? minBox.getMax(minFacing.axis) > maxBox.getMax(minFacing.axis) : minBox.getMin(minFacing.axis) < maxBox
                    .getMin(minFacing.axis)))
                minFacing = null;
            if (box.getSize(maxFacing.axis) == 1 || (maxFacing.positive ? minBox.getMax(maxFacing.axis) < maxBox.getMax(maxFacing.axis) : minBox.getMin(maxFacing.axis) > maxBox
                    .getMin(maxFacing.axis)))
                maxFacing = null;
        }
        
        if (minFacing != null) {
            if (minFacing.axis != axis)
                axis = minFacing.axis;
        } else if (maxFacing != null && maxFacing.axis != axis)
            axis = maxFacing.axis;
        
        CornerCache cache = box.new CornerCache(false);
        
        boolean facingPositive = originalMinVec.get(axis) > originalMaxVec.get(axis);
        
        if (minFacing != null && minFacing == maxFacing) {
            minFacing = Facing.get(axis, originalMinVec.get(axis) > originalMaxVec.get(axis));
            maxFacing = minFacing.opposite();
        }
        
        minBox.growAway(thickness, originalMin.facing);
        maxBox.growAway(thickness, originalMax.facing);
        
        box.growToInclude(minBox);
        box.growToInclude(maxBox);
        
        setStartAndEndBox(cache, axis.facing(facingPositive), minFacing, maxFacing, minBox, maxBox, selection.inside);
        
        box.setData(cache.getData());
        
        if (maxFacing == null)
            maxFacing = axis.facing(!facingPositive);
        
        Axis one = axis.one();
        Axis two = axis.two();
        switch (axis) {
            case X -> {
                if (maxFacing.positive != facingPositive == originalMinVec.get(one) < originalMaxVec.get(one)) {
                    box.setFlipped(one.facing(true), true);
                    box.setFlipped(one.facing(false), true);
                }
                if (maxFacing.positive == facingPositive == (originalMinVec.get(two) < originalMaxVec.get(two))) {
                    box.setFlipped(two.facing(true), true);
                    box.setFlipped(two.facing(false), true);
                }
            }
            case Y -> {
                if (maxFacing.positive == facingPositive == originalMinVec.get(one) < originalMaxVec.get(one)) {
                    box.setFlipped(one.facing(true), true);
                    box.setFlipped(one.facing(false), true);
                }
                if (maxFacing.positive != facingPositive == (originalMinVec.get(two) < originalMaxVec.get(two))) {
                    box.setFlipped(two.facing(true), true);
                    box.setFlipped(two.facing(false), true);
                }
            }
            case Z -> {
                if (maxFacing.positive != facingPositive == originalMinVec.get(one) < originalMaxVec.get(one)) {
                    box.setFlipped(one.facing(true), true);
                    box.setFlipped(one.facing(false), true);
                }
                if (maxFacing.positive != facingPositive == (originalMinVec.get(two) < originalMaxVec.get(two))) {
                    box.setFlipped(two.facing(true), true);
                    box.setFlipped(two.facing(false), true);
                }
            }
        }
        boxes.add(box);
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {}
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        List<GuiControl> controls = new ArrayList<>();
        controls.add(new GuiLabel("label").setTranslate("gui.thickness"));
        controls.add(new GuiSteppedSlider("thickness", nbt.getInt("thickness"), 1, grid.count));
        controls.add(new GuiCheckBox("simple", nbt.getBoolean("simple")).setTranslate("gui.simple"));
        return controls;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {
        GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
        nbt.putInt("thickness", (int) slider.getValue());
        nbt.putBoolean("simple", gui.get("simple", GuiCheckBox.class).value);
    }
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {}
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {}
    
}
