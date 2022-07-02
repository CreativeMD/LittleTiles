package team.creative.littletiles.common.block.little.tile.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleCollection implements Iterable<LittleTile> {
    
    private final Iterable<LittleBox> boxesIterable = new Iterable<LittleBox>() {
        
        @Override
        public Iterator<LittleBox> iterator() {
            return iteratorBoxes();
        }
    };
    
    protected List<LittleTile> content = createInternalList();
    
    public LittleCollection() {
        
    }
    
    public void add(LittleElement element, Iterable<LittleBox> boxes) {
        for (LittleTile other : this)
            if (other.is(element)) {
                other.add(boxes);
                return;
            }
        
        content.add(new LittleTile(element, boxes));
    }
    
    public void add(LittleElement element, LittleBox box) {
        for (LittleTile other : this)
            if (other.is(element)) {
                other.add(box);
                return;
            }
        
        content.add(new LittleTile(element, box));
    }
    
    public void add(LittleTile tile) {
        if (!canAdd())
            return;
        for (LittleTile other : this) {
            if (other.equals(tile)) {
                other.add(tile);
                return;
            }
        }
        content.add(tile);
    }
    
    public void addAll(Iterable<LittleTile> tiles) {
        for (LittleTile tile : tiles)
            add(tile);
    }
    
    protected boolean canAdd() {
        return true;
    }
    
    protected void added(LittleTile tile) {}
    
    protected void refresh() {}
    
    protected void removed(LittleTile tile) {}
    
    public void removeAll(Iterable<LittleTile> tiles) {
        for (LittleTile tile : tiles)
            remove(tile);
    }
    
    public boolean remove(LittleTile tile) {
        if (content.remove(tile)) {
            removed(tile);
            return true;
        }
        return false;
    }
    
    public boolean remove(LittleElement element, LittleBox box) {
        for (LittleTile other : this)
            if (other.is(element))
                return other.remove(this, box);
        return false;
    }
    
    public void clear() {
        content.clear();
        refresh();
    }
    
    protected List<LittleTile> createInternalList() {
        return new ArrayList<>();
    }
    
    public Iterable<LittleBox> boxes() {
        return boxesIterable;
    }
    
    protected Iterator<LittleBox> iteratorBoxes() {
        return new Iterator<LittleBox>() {
            
            Iterator<LittleTile> itr = content.iterator();
            Iterator<LittleBox> itrBox = null;
            LittleBox next;
            boolean seek = true;
            
            @Override
            public boolean hasNext() {
                if (seek) {
                    if (itrBox.hasNext()) {
                        next = itrBox.next();
                        seek = false;
                        return true;
                    } else
                        next = null;
                    while (itr.hasNext()) {
                        LittleTile tile = itr.next();
                        itrBox = tile.iterator();
                        if (itrBox.hasNext()) {
                            next = itrBox.next();
                            seek = false;
                            return true;
                        }
                        next = null;
                    }
                    seek = false;
                }
                return next != null;
            }
            
            @Override
            public LittleBox next() {
                seek = true;
                return next;
            }
            
            @Override
            public void remove() {
                itr.remove();
            }
        };
    }
    
    @Override
    public Iterator<LittleTile> iterator() {
        return new Iterator<LittleTile>() {
            
            Iterator<LittleTile> itr = content.iterator();
            
            @Override
            public LittleTile next() {
                return itr.next();
            }
            
            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }
            
            @Override
            public void remove() {
                itr.remove();
                refresh();
            }
        };
    }
    
    public boolean hasTranslucentBlocks() {
        for (LittleTile tile : content)
            if (tile.isTranslucent())
                return true;
        return false;
    }
    
    public boolean combine() {
        boolean result = false;
        for (LittleTile tile : content)
            result |= tile.combine();
        return result;
    }
    
    public void combineBlockwise(LittleGrid grid) {
        for (LittleTile tile : content)
            tile.combineBlockwise(grid);
    }
    
    public boolean isEmpty() {
        return content.isEmpty();
    }
    
    public int size() {
        return content.size();
    }
    
    public LittleTile first() {
        return content.get(0);
    }
    
    public static void load(LittleCollection collection, CompoundTag nbt) {
        collection.clear();
        
        for (String name : nbt.getAllKeys()) {
            ListTag boxes = nbt.getList(name, Tag.TAG_INT_ARRAY);
            BlockState state = LittleBlockRegistry.loadState(name);
            LittleBlock block;
            if (state.getBlock() instanceof AirBlock)
                block = LittleBlockRegistry.getMissing(name);
            else
                block = LittleBlockRegistry.get(state);
            List<LittleBox> tileBoxes = null;
            for (int j = 0; j < boxes.size(); j++) {
                int[] data = boxes.getIntArray(j);
                if (data.length == 1) {
                    tileBoxes = new ArrayList<>();
                    collection.content.add(new LittleTile(state, block, data[0], tileBoxes));
                } else
                    tileBoxes.add(LittleBox.create(data));
            }
        }
        
    }
    
    public static CompoundTag save(LittleCollection collection) {
        HashMapList<String, LittleTile> sorted = new HashMapList<>();
        
        for (LittleTile tile : collection)
            sorted.add(tile.getBlockName(), tile);
        
        CompoundTag nbt = new CompoundTag();
        for (Entry<String, ArrayList<LittleTile>> entry : sorted.entrySet()) {
            ListTag boxes = new ListTag();
            for (LittleTile tile : entry.getValue()) {
                boxes.add(new IntArrayTag(new int[] { tile.color }));
                for (LittleBox box : tile)
                    boxes.add(box.getArrayTag());
            }
            nbt.put(entry.getKey(), boxes);
        }
        return nbt;
    }
    
}
