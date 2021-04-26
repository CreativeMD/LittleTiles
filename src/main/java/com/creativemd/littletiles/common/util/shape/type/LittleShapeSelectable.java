package com.creativemd.littletiles.common.util.shape.type;

import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.shape.LittleShape;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

public abstract class LittleShapeSelectable extends LittleShape {
    
    public LittleShapeSelectable(int pointsBeforePlacing) {
        super(pointsBeforePlacing);
    }
    
    @Override
    public boolean requiresNoOverlap() {
        return true;
    }
    
    public static void addBox(LittleBoxes boxes, boolean inside, LittleGridContext context, IParentTileList parent, LittleBox box, EnumFacing facing) {
        if (inside)
            boxes.addBox(parent.getContext(), parent.getPos(), box.copy());
        else {
            int size = 1;
            if (parent.getContext().size > context.size) {
                size = parent.getContext().size / context.size;
                context = parent.getContext();
            } else
                box.convertTo(parent.getContext(), context);
            box = box.copy();
            if (facing.getAxisDirection() == AxisDirection.POSITIVE) {
                int min = box.getMax(facing.getAxis());
                box.setMin(facing.getAxis(), min);
                box.setMax(facing.getAxis(), min + 1);
            } else {
                int max = box.getMin(facing.getAxis());
                box.setMin(facing.getAxis(), max - 1);
                box.setMax(facing.getAxis(), max);
            }
            boxes.addBox(context, parent.getPos(), box);
        }
    }
    
    public void addBox(LittleBoxes boxes, boolean inside, LittleGridContext context, BlockPos pos, EnumFacing facing) {
        LittleBox box = new LittleBox(0, 0, 0, context.size, context.size, context.size);
        if (facing.getAxisDirection() == AxisDirection.POSITIVE)
            box.setMax(facing.getAxis(), 1);
        else
            box.setMin(facing.getAxis(), context.size - 1);
        if (inside)
            boxes.addBox(context, pos, box);
        else
            boxes.addBox(context, pos.offset(facing), box);
    }
    
}
