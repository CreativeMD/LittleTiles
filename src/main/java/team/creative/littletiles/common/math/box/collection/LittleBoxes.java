package team.creative.littletiles.common.math.box.collection;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.vec.LittleVec;

public abstract class LittleBoxes implements IGridBased {
    
    public static LittleBoxes of(LittleBoxAbsolute box) {
        LittleBoxes boxes = new LittleBoxesSimple(box.pos, box.grid);
        boxes.add(box.box);
        return boxes;
    }
    
    public BlockPos pos;
    public LittleGrid grid;
    
    public LittleBoxes(BlockPos pos, LittleGrid grid) {
        this.pos = pos;
        this.grid = grid;
    }
    
    public LittleBoxes(CompoundTag nbt) {
        int[] posArray = nbt.getIntArray("p");
        this.pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
        this.grid = LittleGrid.get(nbt);
        ListTag list = nbt.getList("b", Tag.TAG_INT_ARRAY);
        for (int i = 0; i < list.size(); i++)
            add(LittleBox.create(list.getIntArray(i)));
    }
    
    public CompoundTag save(CompoundTag nbt) {
        nbt.putIntArray("p", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        grid.set(nbt);
        ListTag list = new ListTag();
        for (LittleBox box : all())
            list.add(new IntArrayTag(box.getArray()));
        nbt.put("b", list);
        return nbt;
    }
    
    public abstract void add(LittleBox box);
    
    public void addBoxes(IParentCollection parent, LittleTile tile) {
        for (LittleBox box : tile)
            addBox(parent.getGrid(), parent.getPos(), box.copy());
    }
    
    public void addBoxes(LittleGrid grid, BlockPos pos, Iterable<LittleBox> boxes) {
        for (LittleBox box : boxes)
            addBox(grid, pos, box.copy());
    }
    
    public LittleBox addBox(LittleGrid grid, BlockPos pos, LittleBox box) {
        if (this.grid != grid) {
            if (this.grid.count > grid.count) {
                box.convertTo(grid, this.grid);
                grid = this.grid;
            } else
                convertTo(grid);
        }
        
        box.add(new LittleVec(grid, pos.subtract(this.pos)));
        add(box);
        return box;
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public abstract void convertTo(LittleGrid to);
    
    @Override
    public abstract int getSmallest();
    
    public abstract void clear();
    
    public abstract boolean isEmpty();
    
    public abstract int size();
    
    public abstract LittleBox getSurroundingBox();
    
    public abstract HashMapList<BlockPos, LittleBox> generateBlockWise();
    
    public abstract List<LittleBox> all();
    
    public abstract void mirror(Axis axis, LittleBoxAbsolute absoluteBox);
    
    public abstract LittleBoxes copy();
    
    public abstract void combineBoxesBlocks();
    
    public abstract int littleVolume();
    
    public double volume() {
        return grid.pixelVolume * littleVolume();
    }
    
}
