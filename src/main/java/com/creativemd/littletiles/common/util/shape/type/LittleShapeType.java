package com.creativemd.littletiles.common.util.shape.type;

import java.util.Collections;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.shape.LittleShape;
import com.creativemd.littletiles.common.util.shape.ShapeSelection;
import com.creativemd.littletiles.common.util.shape.ShapeSelection.ShapeSelectPos;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleShapeType extends LittleShape {
    
    public LittleShapeType() {
        super(1);
    }
    
    private void addBox(LittleBoxes boxes, boolean inside, IParentTileList parent, LittleTile tile, EnumFacing facing) {
        if (inside)
            boxes.addBox(parent, tile);
        else {
            LittleBox box = tile.getBox().copy();
            LittleVec vec = new LittleVec(facing);
            vec.scale(box.getSize(facing.getAxis()));
            box.add(vec);
            boxes.addBox(parent.getContext(), parent.getPos(), box);
        }
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
                        addBox(boxes, selection.inside, pos.result.te.noneStructureTiles(), toDestroy, pos.pos.facing);
                    
            } else {
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
