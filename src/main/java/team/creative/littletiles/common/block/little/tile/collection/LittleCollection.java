package team.creative.littletiles.common.block.little.tile.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.type.itr.NestedIterator;
import team.creative.creativecore.common.util.type.list.CopyArrayCollection;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.api.common.block.LittleBlock;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.face.LittleServerFace;

public class LittleCollection implements Iterable<LittleTile> {
    
    protected Collection<LittleTile> content = createInternalCollection();
    
    public LittleCollection() {}
    
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
    
    protected Collection<LittleTile> createInternalCollection() {
        return new ArrayList<>();
    }
    
    public Iterator<LittleBox> boxes() {
        return new NestedIterator<LittleBox>(content);
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
    
    public int boxesCount() {
        int count = 0;
        for (LittleTile tile : content)
            count += tile.size();
        return count;
    }
    
    public LittleTile first() {
        if (content instanceof CopyArrayCollection)
            return ((CopyArrayCollection<LittleTile>) content).first();
        if (content instanceof List)
            return ((List<LittleTile>) content).get(0);
        return content.iterator().next();
    }
    
    @Override
    public String toString() {
        return content.toString();
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
            int color = -1;
            for (int j = 0; j < boxes.size(); j++) {
                int[] data = boxes.getIntArray(j);
                if (data.length == 1) {
                    if (tileBoxes != null)
                        collection.content.add(new LittleTile(state, block, color, tileBoxes));
                    tileBoxes = new ArrayList<>();
                    color = data[0];
                } else
                    tileBoxes.add(LittleBox.create(data));
            }
            if (tileBoxes != null && !tileBoxes.isEmpty())
                collection.content.add(new LittleTile(state, block, color, tileBoxes));
        }
        
    }
    
    public static void loadExtended(LittleCollection collection, CompoundTag nbt) {
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
            int color = -1;
            for (int j = 0; j < boxes.size(); j++) {
                int[] data = boxes.getIntArray(j);
                if (data.length == 1) {
                    if (tileBoxes != null)
                        collection.content.add(new LittleTile(state, block, color, tileBoxes));
                    tileBoxes = new ArrayList<>();
                    color = data[0];
                } else
                    tileBoxes.add(LittleBox.createExtended(data));
            }
            if (tileBoxes != null && !tileBoxes.isEmpty())
                collection.content.add(new LittleTile(state, block, color, tileBoxes));
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
    
    public static CompoundTag saveExtended(IParentCollection collection, LittleServerFace face) {
        HashMapList<String, LittleTile> sorted = new HashMapList<>();
        
        for (LittleTile tile : collection)
            sorted.add(tile.getBlockName(), tile);
        
        CompoundTag nbt = new CompoundTag();
        for (Entry<String, ArrayList<LittleTile>> entry : sorted.entrySet()) {
            ListTag boxes = new ListTag();
            for (LittleTile tile : entry.getValue()) {
                boxes.add(new IntArrayTag(new int[] { tile.color }));
                for (LittleBox box : tile)
                    boxes.add(box.getArrayTagExtended(collection, tile, face));
            }
            nbt.put(entry.getKey(), boxes);
        }
        return nbt;
    }
    
}
