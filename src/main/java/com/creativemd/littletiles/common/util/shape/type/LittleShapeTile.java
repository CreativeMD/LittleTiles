package com.creativemd.littletiles.common.util.shape.type;

import java.util.Collections;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.shape.LittleShape;
import com.creativemd.littletiles.common.util.shape.ShapeSelection;
import com.creativemd.littletiles.common.util.shape.ShapeSelection.ShapeSelectPos;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleShapeTile extends LittleShape {
    
    public LittleShapeTile() {
        super(1);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        for (ShapeSelectPos pos : selection) {
            if (pos.result.isComplete())
                if (selection.inside)
                    boxes.addBox(pos.result.parent, pos.result.tile);
                else {
                    LittleBox box = pos.result.tile.getBox().copy();
                    LittleVec vec = new LittleVec(pos.pos.facing);
                    vec.scale(box.getSize(pos.pos.facing.getAxis()));
                    box.add(vec);
                    boxes.addBox(pos.result.te.getContext(), pos.result.te.getPos(), box);
                }
            else {
                LittleGridContext context = selection.getContext();
                if (selection.inside)
                    boxes.addBox(context, pos.result.te.getPos(), new LittleBox(0, 0, 0, context.size, context.size, context.size));
                else
                    boxes.addBox(context, pos.result.te.getPos().offset(pos.pos.facing), new LittleBox(0, 0, 0, context.size, context.size, context.size));
            }
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
