package com.creativemd.littletiles.common.util.shape.drag;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DragShapeBox extends DragShape {
    
    public DragShapeBox() {
        super("box");
    }
    
    @Override
    public LittleBoxes getBoxes(LittleBoxes boxes, LittleVec min, LittleVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, LittleAbsoluteVec originalMin, LittleAbsoluteVec originalMax) {
        LittleBox box = new LittleBox(min, max);
        if (nbt.getBoolean("hollow")) {
            int thickness = nbt.getInteger("thickness");
            LittleVec size = box.getSize();
            if (thickness * 2 >= size.x || thickness * 2 >= size.y || thickness * 2 >= size.z)
                boxes.add(box);
            else {
                boxes.add(new LittleBox(min.x, min.y, min.z, max.x, max.y, min.z + thickness));
                boxes.add(new LittleBox(min.x, min.y + thickness, min.z + thickness, min.x + thickness, max.y - thickness, max.z - thickness));
                boxes.add(new LittleBox(max.x - thickness, min.y + thickness, min.z + thickness, max.x, max.y - thickness, max.z - thickness));
                boxes.add(new LittleBox(min.x, min.y, min.z + thickness, max.x, min.y + thickness, max.z - thickness));
                boxes.add(new LittleBox(min.x, max.y - thickness, min.z + thickness, max.x, max.y, max.z - thickness));
                boxes.add(new LittleBox(min.x, min.y, max.z - thickness, max.x, max.y, max.z));
            }
        } else
            boxes.add(box);
        return boxes;
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
