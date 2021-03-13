package com.creativemd.littletiles.common.util.shape.drag;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.math.Rotation;
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
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DragShapeCorner extends DragShape {
    
    public DragShapeCorner() {
        super("corner");
    }
    
    @Override
    public LittleBoxes getBoxes(LittleBoxes boxes, LittleVec min, LittleVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, PlacementPosition originalMin, PlacementPosition originalMax) {
        LittleTransformableBox box = new LittleTransformableBox(new LittleBox(min, max), new int[0]);
        CornerCache cache = box.new CornerCache(false);
        
        cache.setAbsolute(BoxCorner.EUS, Axis.Y, box.minY);
        
        box.setData(cache.getData());
        boxes.add(box);
        return boxes;
    }
    
    @Override
    public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
        
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
        List<GuiControl> controls = new ArrayList<>();
        return controls;
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
