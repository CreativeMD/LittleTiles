package team.creative.littletiles.common.math.box.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.LittleBoxCombiner;
import team.creative.littletiles.common.math.vec.LittleVec;

public final class LittleBoxesNoOverlap extends LittleBoxes {
    
    protected HashMapList<BlockPos, LittleBox> blockMap = new HashMapList<>();
    protected HashMapList<BlockPos, LittleBox> tempMap = new HashMapList<>();
    protected List<LittleBox> cutOutTemp = new ArrayList<>();
    
    public LittleBoxesNoOverlap(BlockPos pos, LittleGrid grid, HashMapList<BlockPos, LittleBox> map) {
        super(pos, grid);
        this.blockMap = map;
    }
    
    public LittleBoxesNoOverlap(BlockPos pos, LittleGrid grid) {
        super(pos, grid);
    }
    
    public LittleBoxesNoOverlap(CompoundTag nbt) {
        super(null, null);
        int[] posArray = nbt.getIntArray("p");
        this.pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
        this.grid = LittleGrid.get(nbt);
        ListTag list = nbt.getList("b", Tag.TAG_INT_ARRAY);
        for (int i = 0; i < list.size(); i++)
            add(LittleBox.create(list.getIntArray(i)));
    }
    
    @Override
    public void add(LittleBox box) {
        tempMap.clear();
        box.split(grid, pos, LittleVec.ZERO, tempMap, null);
        for (Entry<BlockPos, ArrayList<LittleBox>> entry : tempMap.entrySet()) {
            List<LittleBox> existingBoxes = blockMap.get(entry.getKey());
            boolean missing = existingBoxes == null;
            
            if (missing) {
                existingBoxes = new ArrayList<>();
                for (LittleBox splitted : entry.getValue())
                    existingBoxes.add(splitted.copy());
            } else
                for (LittleBox splitted : entry.getValue()) {
                    cutOutTemp.clear();
                    existingBoxes.addAll(splitted.cutOut(getGrid(), existingBoxes, cutOutTemp, null));
                }
            
            LittleBoxCombiner.combineLast(existingBoxes);
            if (missing)
                blockMap.add(entry.getKey(), existingBoxes);
        }
        tempMap.clear();
    }
    
    public void cutOut(LittleGrid grid, BlockPos pos, LittleBox box) {
        if (this.grid != grid) {
            if (this.grid.count > grid.count) {
                box.convertTo(grid, this.grid);
                grid = this.grid;
            } else
                convertTo(grid);
        }
        
        tempMap.clear();
        box.split(grid, pos, LittleVec.ZERO, tempMap, null);
        SingletonList<LittleBox> cutter = new SingletonList<>(box);
        for (Entry<BlockPos, ArrayList<LittleBox>> entry : tempMap.entrySet()) {
            List<LittleBox> existingBoxes = blockMap.get(entry.getKey());
            if (existingBoxes == null || existingBoxes.isEmpty())
                continue;
            
            List<LittleBox> remaining = new ArrayList<>();
            for (LittleBox existing : existingBoxes) {
                cutOutTemp.clear();
                remaining.addAll(existing.cutOut(getGrid(), cutter, cutOutTemp, null));
            }
            
            LittleBoxCombiner.combineLast(remaining);
            
            blockMap.removeKey(entry.getKey());
            if (!remaining.isEmpty())
                blockMap.add(entry.getKey(), remaining);
        }
        tempMap.clear();
    }
    
    @Override
    public LittleBoxesNoOverlap copy() {
        return new LittleBoxesNoOverlap(pos, grid, generateBlockWise());
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        for (Iterator<LittleBox> iterator = blockMap.iterator(); iterator.hasNext();) {
            LittleBox box = iterator.next();
            box.convertTo(this.grid, to);
        }
        this.grid = to;
    }
    
    @Override
    public int getSmallest() {
        int size = LittleGrid.MIN.count;
        for (LittleBox box : blockMap)
            size = Math.max(size, box.getSmallest(grid));
        return size;
    }
    
    @Override
    public void clear() {
        blockMap.clear();
    }
    
    @Override
    public boolean isEmpty() {
        return blockMap.isEmpty();
    }
    
    @Override
    public int size() {
        return blockMap.sizeOfValues();
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
        
        for (Entry<BlockPos, ArrayList<LittleBox>> entry : blockMap.entrySet()) {
            int x = entry.getKey().getX() * grid.count;
            int y = entry.getKey().getY() * grid.count;
            int z = entry.getKey().getZ() * grid.count;
            
            for (LittleBox box : entry.getValue()) {
                minX = Math.min(minX, x + box.minX);
                minY = Math.min(minY, y + box.minY);
                minZ = Math.min(minZ, z + box.minZ);
                maxX = Math.max(maxX, x + box.maxX);
                maxY = Math.max(maxY, y + box.maxY);
                maxZ = Math.max(maxZ, z + box.maxZ);
            }
        }
        
        return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    @Override
    public HashMapList<BlockPos, LittleBox> generateBlockWise() {
        HashMapList<BlockPos, LittleBox> copyMap = new HashMapList<>();
        for (Entry<BlockPos, ArrayList<LittleBox>> entry : blockMap.entrySet()) {
            List<LittleBox> copyList = new ArrayList<>(entry.getValue().size());
            for (LittleBox box : entry.getValue())
                copyList.add(box.copy());
            copyMap.add(entry.getKey(), copyList);
        }
        return copyMap;
    }
    
    @Override
    public List<LittleBox> all() {
        List<LittleBox> boxes = new ArrayList<>();
        LittleVec vec = new LittleVec(0, 0, 0);
        for (Entry<BlockPos, ArrayList<LittleBox>> entry : blockMap.entrySet()) {
            vec.set(grid, entry.getKey().subtract(this.pos));
            for (LittleBox box : entry.getValue()) {
                LittleBox toAdd = box.copy();
                toAdd.add(vec);
                boxes.add(toAdd);
            }
        }
        return boxes;
    }
    
    @Override
    public void mirror(Axis axis, LittleBoxAbsolute absoluteBox) {
        sameGrid(absoluteBox, () -> {
            Iterable<LittleBox> boxes = all();
            blockMap = new HashMapList<>();
            LittleVec center = absoluteBox.getDoubledCenter(pos);
            for (LittleBox box : boxes) {
                box.transform(axis.getMatrix(), center);
                add(box);
            }
        });
    }
    
    @Override
    public void combineBoxesBlocks() {
        // Already done when adding
    }
    
    @Override
    public int littleVolume() {
        int volume = 0;
        for (ArrayList<LittleBox> boxes : blockMap.values())
            for (LittleBox box : boxes)
                volume += box.getVolume();
        return volume;
    }
    
}
