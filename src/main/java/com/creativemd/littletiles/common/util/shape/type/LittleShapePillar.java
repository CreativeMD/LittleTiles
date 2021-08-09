package com.creativemd.littletiles.common.util.shape.type;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.BoxCorner;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.box.LittleTransformableBox;
import com.creativemd.littletiles.common.tile.math.box.LittleTransformableBox.CornerCache;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.PlacementPosition;
import com.creativemd.littletiles.common.util.shape.LittleShape;
import com.creativemd.littletiles.common.util.shape.ShapeSelection;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        int thickness = Math.max(0, selection.getNBT().getInteger("thickness") - 1);
        
        PlacementPosition originalMin = selection.getFirst().pos.copy();
        PlacementPosition originalMax = selection.getLast().pos.copy();
        originalMin.convertTo(boxes.getContext());
        originalMax.convertTo(boxes.getContext());
        
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        Axis axis = box.getSize().getLongestAxis();
        
        CornerCache cache = box.new CornerCache(false);
        
        LittleVec originalMinVec = originalMin.getRelative(boxes.pos);
        LittleVec originalMaxVec = originalMax.getRelative(boxes.pos);
        
        LittleBox minBox = new LittleBox(originalMinVec);
        LittleBox maxBox = new LittleBox(originalMaxVec);
        
        boolean facingPositive = originalMinVec.get(axis) > originalMaxVec.get(axis);
        
        Axis one = RotationUtils.getOne(axis);
        Axis two = RotationUtils.getTwo(axis);
        
        EnumFacing minFacing = originalMin.facing;
        EnumFacing maxFacing = originalMax.facing;
        
        if (selection.getNBT().getBoolean("simple")) {
            minFacing = null;
            maxFacing = null;
        } else {
            if (box.getSize(minFacing.getAxis()) == 1)
                minFacing = null;
            if (box.getSize(maxFacing.getAxis()) == 1)
                maxFacing = null;
        }
        
        if (minFacing != null && minFacing == maxFacing) {
            if (originalMinVec.get(axis) <= originalMaxVec.get(axis)) {
                minFacing = EnumFacing.getFacingFromAxis(AxisDirection.NEGATIVE, axis);
                maxFacing = minFacing.getOpposite();
            } else {
                minFacing = EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, axis);
                maxFacing = minFacing.getOpposite();
            }
        }
        
        int invSize = thickness / 2;
        int size = thickness - invSize;
        minBox.growCentered(thickness);
        LittleVec vec = new LittleVec(originalMin.facing);
        if (originalMin.facing.getAxisDirection() == AxisDirection.POSITIVE)
            vec.scale(size);
        else
            vec.scale(-invSize);
        minBox.add(vec);
        
        maxBox.growCentered(thickness);
        vec = new LittleVec(originalMax.facing);
        if (originalMax.facing.getAxisDirection() == AxisDirection.POSITIVE)
            vec.scale(size);
        else
            vec.scale(-invSize);
        maxBox.add(vec);
        
        box.growToInclude(minBox);
        box.growToInclude(maxBox);
        
        shrinkEdge(cache, axis, one, two, facingPositive, minFacing, minBox);
        shrinkEdge(cache, axis, one, two, !facingPositive, maxFacing, maxBox);
        
        box.setData(cache.getData());
        boxes.add(box);
    }
    
    public void shrinkEdge(CornerCache cache, Axis axis, Axis one, Axis two, boolean positive, EnumFacing targetFace, LittleBox box) {
        EnumFacing facing = EnumFacing.getFacingFromAxis(positive ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, axis);
        if (targetFace == null)
            targetFace = facing;
        else if (targetFace == facing.getOpposite())
            targetFace = facing;
        Axis targetAxis = targetFace.getAxis();
        BoxCorner[] corners = BoxCorner.faceCorners(facing);
        for (int i = 0; i < corners.length; i++) {
            BoxCorner corner = corners[i];
            cache.setAbsolute(corner, one, corner.isFacingPositive(one) ? box.getMax(one) : box.getMin(one));
            cache.setAbsolute(corner, two, corner.isFacingPositive(two) ? box.getMax(two) : box.getMin(two));
            if (facing != targetFace) {
                if (corner.isFacingPositive(targetAxis) != (targetFace.getAxisDirection() == AxisDirection.POSITIVE))
                    cache.setAbsolute(corner, axis, positive ? box.getMin(axis) : box.getMax(axis));
                else
                    cache.setAbsolute(corner, targetAxis, (targetFace.getAxisDirection() == AxisDirection.POSITIVE) ? box.getMin(targetAxis) : box.getMax(targetAxis));
            }
        }
    }
    
    @Override
    public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
        
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
        List<GuiControl> controls = new ArrayList<>();
        controls.add(new GuiSteppedSlider("thickness", 5, 5, 100, 14, nbt.getInteger("thickness"), 1, context.size));
        controls.add(new GuiCheckBox("simple", 5, 25, nbt.getBoolean("simple")));
        return controls;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
        GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
        nbt.setInteger("thickness", (int) slider.value);
        GuiCheckBox simple = (GuiCheckBox) gui.get("simple");
        nbt.setBoolean("simple", simple.value);
    }
    
    @Override
    public void rotate(NBTTagCompound nbt, Rotation rotation) {
        
    }
    
    @Override
    public void flip(NBTTagCompound nbt, Axis axis) {
        
    }
    
}
