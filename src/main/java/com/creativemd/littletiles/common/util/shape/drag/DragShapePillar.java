package com.creativemd.littletiles.common.util.shape.drag;

import java.util.Collections;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DragShapePillar extends DragShape {
    
    public DragShapePillar() {
        super("pillar");
    }
    
    @Override
    public LittleBoxes getBoxes(LittleBoxes boxes, LittleVec min, LittleVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, PlacementPosition originalMin, PlacementPosition originalMax) {
        originalMin.convertTo(boxes.getContext());
        originalMax.convertTo(boxes.getContext());
        
        LittleTransformableBox box = new LittleTransformableBox(new LittleBox(min, max), new int[0]);
        LittleVec size = box.getSize();
        Axis axis = size.getLongestAxis();
        
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
        
        if (box.getSize(minFacing.getAxis()) == 1)
            minFacing = null;
        if (box.getSize(maxFacing.getAxis()) == 1)
            maxFacing = null;
        
        shrinkEdge(cache, axis, one, two, facingPositive, minFacing, minBox);
        shrinkEdge(cache, axis, one, two, !facingPositive, maxFacing, maxBox);
        
        box.setData(cache.getData());
        boxes.add(box);
        return boxes;
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
        return Collections.EMPTY_LIST;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
        
    }
    
    @Override
    public void rotate(NBTTagCompound nbt, Rotation rotation) {
        
    }
    
    @Override
    public void flip(NBTTagCompound nbt, Axis axis) {
        
    }
    
}
