package com.creativemd.littletiles.common.util.shape;

import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleShape {
    
    public final int pointsBeforePlacing;
    
    public LittleShape(int pointsBeforePlacing) {
        this.pointsBeforePlacing = pointsBeforePlacing;
    }
    
    public String getKey() {
        return ShapeRegistry.getShapeName(this);
    }
    
    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        return GuiControl.translateOrDefault("shape." + getKey(), getKey());
    }
    
    public int maxAllowed() {
        return -1;
    }
    
    protected abstract void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution);
    
    public LittleBoxes getBoxes(ShapeSelection selection, boolean lowResolution) {
        LittleBoxes boxes = new LittleBoxes(selection.getPos(), selection.getContext());
        addBoxes(boxes, selection, lowResolution);
        return boxes;
    }
    
    public abstract void addExtraInformation(NBTTagCompound nbt, List<String> list);
    
    @SideOnly(Side.CLIENT)
    public abstract List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context);
    
    @SideOnly(Side.CLIENT)
    public abstract void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context);
    
    public abstract void rotate(NBTTagCompound nbt, Rotation rotation);
    
    public abstract void flip(NBTTagCompound nbt, Axis axis);
    
}
