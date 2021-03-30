package com.creativemd.littletiles.common.util.shape.type;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.shape.LittleShape;
import com.creativemd.littletiles.common.util.shape.ShapeSelection;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleShapeBox extends LittleShape {
    
    public LittleShapeBox() {
        super(2);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        LittleBox box = selection.getOverallBox();
        if (selection.nbt.getBoolean("hollow")) {
            int thickness = selection.nbt.getInteger("thickness");
            LittleVec size = box.getSize();
            if (thickness * 2 >= size.x || thickness * 2 >= size.y || thickness * 2 >= size.z)
                boxes.add(box);
            else {
                boxes.add(new LittleBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ + thickness));
                boxes.add(new LittleBox(box.minX, box.minY + thickness, box.minZ + thickness, box.minX + thickness, box.maxY - thickness, box.maxZ - thickness));
                boxes.add(new LittleBox(box.maxX - thickness, box.minY + thickness, box.minZ + thickness, box.maxX, box.maxY - thickness, box.maxZ - thickness));
                boxes.add(new LittleBox(box.minX, box.minY, box.minZ + thickness, box.maxX, box.minY + thickness, box.maxZ - thickness));
                boxes.add(new LittleBox(box.minX, box.maxY - thickness, box.minZ + thickness, box.maxX, box.maxY, box.maxZ - thickness));
                boxes.add(new LittleBox(box.minX, box.minY, box.maxZ - thickness, box.maxX, box.maxY, box.maxZ));
            }
        } else
            boxes.add(box);
    }
    
    @Override
    public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
        if (nbt.getBoolean("hollow")) {
            list.add("type: hollow");
            list.add("thickness: " + nbt.getInteger("thickness") + " tiles");
        } else
            list.add("type: solid");
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
        List<GuiControl> controls = new ArrayList<>();
        
        controls.add(new GuiCheckBox("hollow", 5, 0, nbt.getBoolean("hollow")));
        controls.add(new GuiSteppedSlider("thickness", 5, 20, 100, 14, nbt.getInteger("thickness"), 1, context.size));
        
        return controls;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
        
        GuiCheckBox box = (GuiCheckBox) gui.get("hollow");
        nbt.setBoolean("hollow", box.value);
        GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
        nbt.setInteger("thickness", (int) slider.value);
    }
    
    @Override
    public void rotate(NBTTagCompound nbt, Rotation rotation) {
        
    }
    
    @Override
    public void flip(NBTTagCompound nbt, Axis axis) {
        
    }
    
}
