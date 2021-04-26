package com.creativemd.littletiles.common.util.shape.type;

import java.util.Collections;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.shape.ShapeSelection;
import com.creativemd.littletiles.common.util.shape.ShapeSelection.ShapeSelectPos;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleShapeType extends LittleShapeSelectable {
    
    public LittleShapeType() {
        super(1);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        for (ShapeSelectPos pos : selection) {
            if (pos.result.isComplete()) {
                if (pos.result.parent.isStructure())
                    continue;
                
                LittleTile tile = pos.result.tile;
                for (LittleTile toDestroy : pos.result.te.noneStructureTiles())
                    if (tile.canBeCombined(toDestroy) && toDestroy.canBeCombined(tile))
                        addBox(boxes, selection.inside, selection.getContext(), pos.result.te.noneStructureTiles(), toDestroy.getBox(), pos.pos.facing);
                    
            } else
                addBox(boxes, selection.inside, selection.getContext(), pos.ray.getBlockPos(), pos.pos.facing);
        }
    }
    
    @Override
    public void addExtraInformation(NBTTagCompound nbt, List<String> list) {}
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {}
    
    @Override
    public void rotate(NBTTagCompound nbt, Rotation rotation) {
        
    }
    
    @Override
    public void flip(NBTTagCompound nbt, Axis axis) {
        
    }
}
