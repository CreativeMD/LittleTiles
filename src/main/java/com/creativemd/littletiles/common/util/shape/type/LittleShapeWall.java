package com.creativemd.littletiles.common.util.shape.type;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.BoxCorner;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.box.LittleTransformableBox;
import com.creativemd.littletiles.common.tile.math.box.LittleTransformableBox.CornerCache;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
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

public class LittleShapeWall extends LittleShape {
    
    public LittleShapeWall() {
        super(2);
    }
    
    public void shrinkEdge(CornerCache cache, Axis axis, Axis one, Axis two, boolean positive, LittleBox box) {
        EnumFacing facing = EnumFacing.getFacingFromAxis(positive ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, axis);
        BoxCorner[] corners = BoxCorner.faceCorners(facing);
        for (int i = 0; i < corners.length; i++) {
            BoxCorner corner = corners[i];
            cache.setAbsolute(corner, one, corner.isFacingPositive(one) ? box.getMax(one) : box.getMin(one));
            cache.setAbsolute(corner, two, corner.isFacingPositive(two) ? box.getMax(two) : box.getMin(two));
        }
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
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        int direction = selection.nbt.getInteger("direction");
        PlacementPosition originalMin = selection.getFirst().pos.copy();
        PlacementPosition originalMax = selection.getLast().pos.copy();
        
        if (selection.nbt.getBoolean("smooth")) {
            originalMin.convertTo(boxes.getContext());
            originalMax.convertTo(boxes.getContext());
            
            int thickness = Math.max(0, selection.nbt.getInteger("thickness") - 1);
            
            LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
            Axis toIgnore = direction == 0 ? Axis.Y : direction == 1 ? Axis.X : Axis.Z;
            Axis oneIgnore = RotationUtils.getOne(toIgnore);
            Axis twoIgnore = RotationUtils.getTwo(toIgnore);
            Axis axis = box.getSize(oneIgnore) > box.getSize(twoIgnore) ? oneIgnore : twoIgnore;
            
            CornerCache cache = box.new CornerCache(false);
            LittleVec originalMinVec = originalMin.getRelative(boxes.pos);
            LittleVec originalMaxVec = originalMax.getRelative(boxes.pos);
            
            Axis one = RotationUtils.getOne(axis);
            Axis two = RotationUtils.getTwo(axis);
            
            LittleBox minBox = new LittleBox(originalMinVec);
            LittleBox maxBox = new LittleBox(originalMaxVec);
            
            EnumFacing minFacing = originalMin.facing;
            EnumFacing maxFacing = originalMax.facing;
            
            if (minFacing.getAxis() == toIgnore || box.getSize(minFacing.getAxis()) == 1)
                minFacing = null;
            if (maxFacing.getAxis() == toIgnore || box.getSize(maxFacing.getAxis()) == 1)
                maxFacing = null;
            
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
            
            minBox.setMin(toIgnore, box.getMin(toIgnore));
            maxBox.setMin(toIgnore, box.getMin(toIgnore));
            minBox.setMax(toIgnore, box.getMax(toIgnore));
            maxBox.setMax(toIgnore, box.getMax(toIgnore));
            
            boolean facingPositive = originalMinVec.get(axis) > originalMaxVec.get(axis);
            
            shrinkEdge(cache, axis, one, two, facingPositive, minFacing, minBox);
            shrinkEdge(cache, axis, one, two, !facingPositive, maxFacing, maxBox);
            
            box.setData(cache.getData());
            boxes.add(box);
            
            return;
        }
        
        int thicknessXInv = 0;
        int thicknessX = 0;
        int thicknessYInv = selection.nbt.getInteger("thickness") > 1 ? (int) Math.ceil((selection.nbt.getInteger("thickness") - 1) / 2D) : 0;
        int thicknessY = selection.nbt.getInteger("thickness") > 1 ? (int) Math.floor((selection.nbt.getInteger("thickness") - 1) / 2D) : 0;
        
        LittleBox box = selection.getOverallBox();
        
        LittleAbsoluteVec absolute = new LittleAbsoluteVec(boxes.pos, boxes.context);
        
        LittleVec originalMinVec = originalMin.getRelative(absolute).getVec(boxes.context);
        LittleVec originalMaxVec = originalMax.getRelative(absolute).getVec(boxes.context);
        
        int w = originalMaxVec.x - originalMinVec.x;
        int h = originalMaxVec.z - originalMinVec.z;
        
        int x = originalMinVec.x;
        int y = originalMinVec.z;
        
        if (direction == 1) {
            w = originalMaxVec.y - originalMinVec.y;
            h = originalMaxVec.z - originalMinVec.z;
            x = originalMinVec.y;
            y = originalMinVec.z;
        } else if (direction == 2) {
            w = originalMaxVec.x - originalMinVec.x;
            h = originalMaxVec.y - originalMinVec.y;
            x = originalMinVec.x;
            y = originalMinVec.y;
        }
        
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
        if (w < 0)
            dx1 = -1;
        else if (w > 0)
            dx1 = 1;
        if (h < 0)
            dy1 = -1;
        else if (h > 0)
            dy1 = 1;
        if (w < 0)
            dx2 = -1;
        else if (w > 0)
            dx2 = 1;
        int longest = Math.abs(w);
        int shortest = Math.abs(h);
        if (!(longest > shortest)) {
            longest = Math.abs(h);
            shortest = Math.abs(w);
            if (h < 0)
                dy2 = -1;
            else if (h > 0)
                dy2 = 1;
            dx2 = 0;
            
            thicknessX = thicknessY;
            thicknessXInv = thicknessYInv;
            thicknessY = 0;
            thicknessYInv = 0;
        }
        int numerator = longest >> 1;
        for (int i = 0; i <= longest; i++) {
            
            LittleBox toAdd = null;
            switch (direction) {
            case 0:
                toAdd = new LittleBox(x - thicknessXInv, box.minY, y - thicknessYInv, x + thicknessX + 1, box.maxY, y + thicknessY + 1);
                break;
            case 1:
                toAdd = new LittleBox(box.minX, x - thicknessXInv, y - thicknessYInv, box.maxX, x + thicknessX + 1, y + thicknessY + 1);
                break;
            case 2:
                toAdd = new LittleBox(x - thicknessXInv, y - thicknessYInv, box.minZ, x + thicknessX + 1, y + thicknessY + 1, box.maxZ);
                break;
            }
            boxes.add(toAdd);
            
            numerator += shortest;
            if (!(numerator < longest)) {
                numerator -= longest;
                x += dx1;
                y += dy1;
            } else {
                x += dx2;
                y += dy2;
            }
        }
        
        LittleBox.combineBoxesBlocks(boxes);
    }
    
    @Override
    public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
        list.add("thickness: " + nbt.getInteger("thickness") + " tiles");
        
        int facing = nbt.getInteger("direction");
        String text = "facing: ";
        switch (facing) {
        case 0:
            text += "y";
            break;
        case 1:
            text += "x";
            break;
        case 2:
            text += "z";
            break;
        }
        list.add(text);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
        List<GuiControl> controls = new ArrayList<>();
        
        controls.add(new GuiSteppedSlider("thickness", 5, 5, 100, 14, nbt.getInteger("thickness"), 1, context.size));
        controls.add(new GuiStateButton("direction", nbt.getInteger("direction"), 5, 27, "facing: y", "facing: x", "facing: z"));
        controls.add(new GuiCheckBox("smooth", 60, 27, nbt.getBoolean("smooth")));
        return controls;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
        
        GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
        nbt.setInteger("thickness", (int) slider.value);
        
        GuiStateButton state = (GuiStateButton) gui.get("direction");
        nbt.setInteger("direction", state.getState());
        
        GuiCheckBox box = (GuiCheckBox) gui.get("smooth");
        nbt.setBoolean("smooth", box.value);
        
    }
    
    @Override
    public void rotate(NBTTagCompound nbt, Rotation rotation) {
        int direction = nbt.getInteger("direction");
        if (rotation.axis != Axis.Y)
            direction = 0;
        else {
            if (direction == 1)
                direction = 2;
            else
                direction = 1;
        }
        
        nbt.setInteger("direction", direction);
    }
    
    @Override
    public void flip(NBTTagCompound nbt, Axis axis) {
        
    }
}
