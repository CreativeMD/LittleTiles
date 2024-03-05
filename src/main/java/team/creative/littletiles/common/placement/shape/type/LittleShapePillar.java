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
        
        if (selection.getNBT().getBoolean("simple")) {
            minFacing = null;
            maxFacing = null;
        } else {
            if (box.getSize(minFacing.axis) == 1)
                minFacing = null;
            if (box.getSize(maxFacing.axis) == 1)
                maxFacing = null;
        }
        
        if (minFacing != null && minFacing.axis != axis)
            axis = minFacing.axis;
        else if (maxFacing != null && maxFacing.axis != axis)
            axis = maxFacing.axis;
        
        CornerCache cache = box.new CornerCache(false);
        
        LittleVec originalMinVec = originalMin.getRelative(boxes.pos);
        LittleVec originalMaxVec = originalMax.getRelative(boxes.pos);
        
        LittleBox minBox = new LittleBox(originalMinVec);
        LittleBox maxBox = new LittleBox(originalMaxVec);
        
        boolean facingPositive = originalMinVec.get(axis) > originalMaxVec.get(axis);
        
        Axis one = axis.one();
        Axis two = axis.two();
        
        if (minFacing != null && minFacing == maxFacing) {
            minFacing = Facing.get(axis, originalMinVec.get(axis) > originalMaxVec.get(axis));
            maxFacing = minFacing.opposite();
        }
        
        minBox.growAway(thickness, originalMin.facing);
        maxBox.growAway(thickness, originalMax.facing);
        
        box.growToInclude(minBox);
        box.growToInclude(maxBox);
        
        shrinkEdge(cache, axis, one, two, facingPositive, minFacing, minBox, selection.inside);
        shrinkEdge(cache, axis, one, two, !facingPositive, maxFacing, maxBox, selection.inside);
        
        box.setData(cache.getData());
        boxes.add(box);
    }
    
    public void shrinkEdge(CornerCache cache, Axis axis, Axis one, Axis two, boolean positive, Facing targetFace, LittleBox box, boolean inside) {
        Facing facing = Facing.get(axis, positive);
        if (targetFace == null)
            targetFace = facing;
        else if (targetFace == facing.opposite())
            targetFace = facing;
        
        BoxCorner[] corners = BoxCorner.faceCorners(facing);
        System.out.println(axis + " " + positive + " " + targetFace);
        
        if (facing == targetFace) {
            for (int i = 0; i < corners.length; i++) {
                BoxCorner corner = corners[i];
                cache.setAbsolute(corner, one, corner.isFacingPositive(one) ? box.getMax(one) : box.getMin(one));
                cache.setAbsolute(corner, two, corner.isFacingPositive(two) ? box.getMax(two) : box.getMin(two));
            }
        } else {
            Axis targetAxis = targetFace.axis;
            Axis third = Axis.third(axis, targetAxis);
            for (int i = 0; i < corners.length; i++) {
                BoxCorner corner = corners[i];
                BoxCorner newCorner = BoxCorner.getCornerUnsorted(inside ? targetFace : targetFace.opposite(), axis.facing(inside != corner.isFacingPositive(targetAxis)), corner
                        .getFacing(third));
                
                cache.setAbsolute(corner, axis, box.get(newCorner, axis));
                cache.setAbsolute(corner, one, box.get(newCorner, one));
                cache.setAbsolute(corner, two, box.get(newCorner, two));
            }
        }
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {}
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        List<GuiControl> controls = new ArrayList<>();
        controls.add(new GuiSteppedSlider("thickness", nbt.getInt("thickness"), 1, grid.count));
        controls.add(new GuiCheckBox("simple", nbt.getBoolean("simple")));
        return controls;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {
        GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
        nbt.putInt("thickness", (int) slider.value);
        nbt.putBoolean("simple", gui.get("simple", GuiCheckBox.class).value);
    }
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {}
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {}
    
}
