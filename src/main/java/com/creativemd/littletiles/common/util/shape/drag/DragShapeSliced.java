package com.creativemd.littletiles.common.util.shape.drag;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.box.LittleTransformableBox;
import com.creativemd.littletiles.common.tile.math.box.slice.LittleSlice;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.PlacementPosition;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;

public class DragShapeSliced extends DragShape {
    
    public DragShapeSliced() {
        super("slice");
    }
    
    @Override
    public LittleBoxes getBoxes(LittleBoxes boxes, LittleVec min, LittleVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, PlacementPosition originalMin, PlacementPosition originalMax) {
        boxes.add(new LittleTransformableBox(min.x, min.y, min.z, max.x, max.y, max.z, LittleSlice.values()[nbt.getInteger("slice")]));
        return boxes;
    }
    
    @Override
    public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
        
    }
    
    @Override
    public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
        return new ArrayList<>();
    }
    
    @Override
    public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
        
    }
    
    @Override
    public void rotate(NBTTagCompound nbt, Rotation rotation) {
        LittleSlice slice = LittleSlice.values()[nbt.getInteger("slice")];
        slice = slice.rotate(rotation);
        nbt.setInteger("slice", slice.ordinal());
    }
    
    @Override
    public void flip(NBTTagCompound nbt, Axis axis) {
        LittleSlice slice = LittleSlice.values()[nbt.getInteger("slice")];
        slice = slice.flip(axis);
        nbt.setInteger("slice", slice.ordinal());
    }
    
}
