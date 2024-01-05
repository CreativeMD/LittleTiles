package team.creative.littletiles.common.block.little.tile.group;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.structure.LittleStructureType;

public class LittleGroupAbsolute implements IGridBased {
    
    public final BlockPos pos;
    public final LittleGroup group;
    
    public LittleGroupAbsolute(BlockPos pos, LittleGroup group) {
        this.pos = pos;
        this.group = group;
    }
    
    public LittleGroupAbsolute(LittleBoxes boxes, LittleElement element) {
        this.pos = boxes.pos;
        this.group = new LittleGroup();
        this.group.add(boxes.getGrid(), element, boxes.all());
    }
    
    public LittleGroupAbsolute(BlockPos pos) {
        this(pos, new LittleGroup());
    }
    
    @Override
    public LittleGrid getGrid() {
        return group.getGrid();
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        group.convertTo(to);
    }
    
    @Override
    public int getSmallest() {
        return group.getSmallest();
    }
    
    public boolean isEmpty() {
        return group.isEmpty();
    }
    
    public CompoundTag getStructureTag() {
        return group.getStructureTag();
    }
    
    public LittleStructureType getStructureType() {
        return group.getStructureType();
    }
    
    public void add(LittleGrid grid, LittleElement element, LittleBoxes boxes) {
        group.add(grid, element, boxes.all());
    }
    
    public void addFast(IParentCollection parent, LittleTile tile) {
        tile = tile.copy();
        
        if (this.getGrid() != parent.getGrid())
            if (this.getGrid().count > parent.getGrid().count)
                tile.convertTo(parent.getGrid(), this.getGrid());
            else
                convertTo(parent.getGrid());
            
        tile.move(new LittleVec(getGrid(), parent.getPos().subtract(pos)));
        group.addTileFast(getGrid(), tile);
    }
    
    public void add(IParentCollection parent, LittleTile tile) {
        addFast(parent, tile);
        group.convertToSmallest();
    }
    
    public void addFast(IParentCollection parent, LittleElement element, LittleBox box) {
        box = box.copy();
        
        if (this.getGrid() != parent.getGrid())
            if (this.getGrid().count > parent.getGrid().count)
                box.convertTo(parent.getGrid(), this.getGrid());
            else
                convertTo(parent.getGrid());
        box.add(new LittleVec(getGrid(), parent.getPos().subtract(pos)));
        group.addFast(getGrid(), element, box);
    }
    
    public void add(IParentCollection parent, LittleElement element, LittleBox box) {
        addFast(parent, element, box);
        group.convertToSmallest();
    }
    
    public LittleGroupAbsolute copy() {
        return new LittleGroupAbsolute(pos, group.copy());
    }
    
    public LittleBoxAbsolute getBox() {
        LittleVec vec = group.getSize();
        LittleVec min = group.getMinVec();
        vec.add(min);
        return new LittleBoxAbsolute(pos, new LittleBox(min, vec), getGrid());
    }
    
    public static void add(LittleGroup group, BlockPos pos, IParentCollection parent, LittleTile tile) {
        tile = tile.copy();
        
        if (group.getGrid() != parent.getGrid())
            if (group.getGrid().count > parent.getGrid().count)
                tile.convertTo(parent.getGrid(), group.getGrid());
            else
                group.convertTo(parent.getGrid());
            
        tile.move(new LittleVec(group.getGrid(), parent.getPos().subtract(pos)));
        group.addTile(group.getGrid(), tile);
    }
    
}
