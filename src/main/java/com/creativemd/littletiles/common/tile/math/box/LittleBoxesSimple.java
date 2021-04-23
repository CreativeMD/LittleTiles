package com.creativemd.littletiles.common.tile.math.box;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.combine.BasicCombiner;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.util.grid.IGridBased;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public class LittleBoxesSimple extends LittleBoxes implements IGridBased, Iterable<LittleBox> {
    
    protected List<LittleBox> boxes = new ArrayList<>();
    
    public LittleBoxesSimple(BlockPos pos, LittleGridContext context) {
        super(pos, context);
    }
    
    @Override
    public void add(LittleBox box) {
        boxes.add(box);
    }
    
    @Override
    public boolean isEmpty() {
        return boxes.isEmpty();
    }
    
    @Override
    public void clear() {
        boxes.clear();
    }
    
    @Override
    public int size() {
        return boxes.size();
    }
    
    @Override
    public LittleBox addBox(IParentTileList parent, LittleTile tile) {
        return addBox(parent.getContext(), parent.getPos(), tile.getBox().copy());
    }
    
    @Override
    public LittleBox addBox(LittleGridContext context, BlockPos pos, LittleBox box) {
        if (this.context != context) {
            if (this.context.size > context.size) {
                box.convertTo(context, this.context);
                context = this.context;
            } else
                convertTo(context);
        }
        
        box.add(new LittleVec(context, pos.subtract(this.pos)));
        add(box);
        return box;
    }
    
    @Override
    public LittleGridContext getContext() {
        return context;
    }
    
    @Override
    public void convertTo(LittleGridContext to) {
        for (LittleBox box : boxes)
            box.convertTo(this.context, to);
        this.context = to;
    }
    
    @Override
    public void convertToSmallest() {
        int size = LittleGridContext.minSize;
        for (LittleBox box : boxes)
            size = Math.max(size, box.getSmallestContext(context));
        
        if (size < context.size)
            convertTo(LittleGridContext.get(size));
    }
    
    @Override
    public LittleBox getSurroundingBox() {
        if (isEmpty())
            return null;
        
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        
        for (LittleBox box : boxes) {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        
        return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    @Override
    public Iterator<LittleBox> iterator() {
        return boxes.iterator();
    }
    
    @Override
    public Iterable<LittleBox> all() {
        return this;
    }
    
    @Override
    public HashMapList<BlockPos, LittleBox> generateBlockWise() {
        HashMapList<BlockPos, LittleBox> map = new HashMapList<>();
        for (LittleBox box : this)
            box.split(context, pos, map, null);
        return map;
    }
    
    @Override
    public void flip(Axis axis, LittleAbsoluteBox absoluteBox) {
        ensureContext(absoluteBox, () -> {
            LittleVec center = absoluteBox.getDoubledCenter(pos);
            for (LittleBox box : boxes)
                box.flipBox(axis, center);
        });
    }
    
    @Override
    public LittleBoxes copy() {
        LittleBoxesSimple boxes = new LittleBoxesSimple(pos, context);
        boxes.boxes.addAll(this.boxes);
        return boxes;
    }
    
    @Override
    public void combineBoxesBlocks() {
        HashMapList<BlockPos, LittleBox> chunked = new HashMapList<>();
        for (int i = 0; i < boxes.size(); i++)
            chunked.add(boxes.get(i).getMinVec().getBlockPos(context), boxes.get(i));
        boxes.clear();
        for (Iterator<ArrayList<LittleBox>> iterator = chunked.values().iterator(); iterator.hasNext();) {
            ArrayList<LittleBox> list = iterator.next();
            BasicCombiner.combineBoxes(list);
            boxes.addAll(list);
        }
    }
    
}
