package com.creativemd.littletiles.common.util.shape.type;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.shape.LittleShape;
import com.creativemd.littletiles.common.util.shape.ShapeSelection;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleShapePyramid extends LittleShape {
    
    public LittleShapePyramid() {
        super(2);
    }
    
    public EnumFacing getFacing(NBTTagCompound nbt) {
        if (nbt.hasKey("facing"))
            return EnumFacing.getFront(nbt.getInteger("facing"));
        return EnumFacing.UP;
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        LittleBox box = selection.getOverallBox();
        EnumFacing facing = getFacing(selection.nbt);
        Axis axis = facing.getAxis();
        boolean positive = facing.getAxisDirection() == AxisDirection.POSITIVE;
        int minAxis = box.getMin(axis);
        int maxAxis = box.getMax(axis);
        
        Axis one = RotationUtils.getOne(axis);
        Axis two = RotationUtils.getTwo(axis);
        
        int minOne = box.getMin(one);
        int minTwo = box.getMin(two);
        int maxOne = box.getMax(one);
        int maxTwo = box.getMax(two);
        
        int counter = 0;
        
        if (positive)
            for (int i = minAxis; i < maxAxis; i++) {
                LittleBox toAdd = new LittleBox(i, i, i, i + 1, i + 1, i + 1);
                toAdd.setMin(one, Math.min(minOne + counter, maxOne - counter));
                toAdd.setMin(two, Math.min(minTwo + counter, maxTwo - counter));
                toAdd.setMax(one, Math.max(minOne + counter, maxOne - counter));
                toAdd.setMax(two, Math.max(minTwo + counter, maxTwo - counter));
                boxes.add(toAdd);
                counter++;
            }
        else
            for (int i = maxAxis - 1; i >= minAxis; i--) {
                LittleBox toAdd = new LittleBox(i, i, i, i + 1, i + 1, i + 1);
                toAdd.setMin(one, Math.min(minOne + counter, maxOne - counter));
                toAdd.setMin(two, Math.min(minTwo + counter, maxTwo - counter));
                toAdd.setMax(one, Math.max(minOne + counter, maxOne - counter));
                toAdd.setMax(two, Math.max(minTwo + counter, maxTwo - counter));
                boxes.add(toAdd);
                counter++;
            }
    }
    
    @Override
    public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
        list.add("facing: " + getFacing(nbt).name().toLowerCase());
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
        List<GuiControl> controls = new ArrayList<>();
        String[] states = new String[6];
        for (int i = 0; i < states.length; i++)
            states[i] = "facing: " + EnumFacing.getFront(i).name().toLowerCase();
        controls.add(new GuiStateButton("direction", nbt.hasKey("facing") ? nbt.getInteger("facing") : EnumFacing.UP.ordinal(), 5, 27, states));
        return controls;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
        GuiStateButton state = (GuiStateButton) gui.get("direction");
        nbt.setInteger("facing", state.getState());
    }
    
    @Override
    public void rotate(NBTTagCompound nbt, Rotation rotation) {
        EnumFacing facing = getFacing(nbt);
        facing = RotationUtils.rotate(facing, rotation);
        nbt.setInteger("facing", facing.ordinal());
    }
    
    @Override
    public void flip(NBTTagCompound nbt, Axis axis) {
        EnumFacing facing = getFacing(nbt);
        facing = RotationUtils.flip(facing, axis);
        nbt.setInteger("facing", facing.ordinal());
    }
    
}
