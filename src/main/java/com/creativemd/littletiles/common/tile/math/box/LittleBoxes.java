package com.creativemd.littletiles.common.tile.math.box;

import java.util.ArrayList;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.util.grid.IGridBased;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public class LittleBoxes extends ArrayList<LittleBox> implements IGridBased {
    
    public BlockPos pos;
    public LittleGridContext context;
    
    public LittleBoxes(BlockPos pos, LittleGridContext context) {
        this.pos = pos;
        this.context = context;
    }
    
    public void addBox(IParentTileList parent, LittleTile tile) {
        addBox(parent.getContext(), parent.getPos(), tile.getBox().copy());
    }
    
    public void addBox(LittleGridContext context, BlockPos pos, LittleBox box) {
        if (this.context != context) {
            if (this.context.size > context.size) {
                box.convertTo(context, this.context);
                context = this.context;
            } else
                convertTo(context);
        }
        
        box.add(new LittleVec(context, pos.subtract(this.pos)));
        add(box);
    }
    
    @Override
    public LittleGridContext getContext() {
        return context;
    }
    
    @Override
    public void convertTo(LittleGridContext to) {
        for (LittleBox box : this) {
            box.convertTo(this.context, to);
        }
        this.context = to;
    }
    
    @Override
    public void convertToSmallest() {
        int size = LittleGridContext.minSize;
        for (LittleBox box : this) {
            size = Math.max(size, box.getSmallestContext(context));
        }
        
        if (size < context.size)
            convertTo(LittleGridContext.get(size));
    }
    
    public LittleBox getSurroundingBox() {
        if (isEmpty())
            return null;
        
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        
        for (LittleBox box : this) {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        
        return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public HashMapList<BlockPos, LittleBox> split() {
        HashMapList<BlockPos, LittleBox> map = new HashMapList<>();
        for (LittleBox box : this)
            box.split(context, pos, map, null);
        return map;
    }
    
    public void flip(Axis axis, LittleAbsoluteBox absoluteBox) {
        ensureContext(absoluteBox, () -> {
            LittleVec center = absoluteBox.getDoubledCenter(pos);
            for (LittleBox box : this)
                box.flipBox(axis, center);
        });
    }
    
    public LittleBoxes copy() {
        LittleBoxes boxes = new LittleBoxes(pos, context);
        boxes.addAll(this);
        return boxes;
    }
    
}
