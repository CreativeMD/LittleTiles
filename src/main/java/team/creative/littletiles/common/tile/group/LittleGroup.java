package team.creative.littletiles.common.tile.group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.tile.combine.BasicCombiner;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleVolumes;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.preview.NBTTagCompound;
import com.creativemd.littletiles.common.tile.preview.NBTTagInt;
import com.creativemd.littletiles.common.tile.preview.NBTTagList;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.util.compression.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.tile.LittleCollection;
import team.creative.littletiles.common.tile.LittleTile;

public abstract class LittleGroup extends LittleCollection implements IGridBased {
    
    private LittleGroup parent;
    private List<LittleGroup> children = new ArrayList<>();
    
    private LittleGrid grid;
    
    public Iterable<LittleGroup> children() {
        return children;
    }
    
    public abstract boolean hasStructure();
    
    public abstract boolean hasStructureIncludeChildren();
    
    public abstract String getStructureName();
    
    public abstract String getStructureId();
    
    public abstract LittleStructureType getStructureType();
    
    public abstract void setDynamicChild(boolean dynamic);
    
    public abstract boolean containsIngredients();
    
    public boolean hasParent() {
        return parent != null;
    }
    
    public LittleGroup getParent() {
        return parent;
    }
    
    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    public int childrenCount() {
        return children.size();
    }
    
    public LittleGroup getChild(int index) {
        return children.get(index);
    }
    
    public void updateChild(int index, LittleGroup child) {
        child.parent = this;
        children.set(index, child).parent = null;
    }
    
    public void addChild(LittleGroup child, boolean dynamic) {
        child.parent = this;
        setDynamicChild(dynamic);
        forceSameGrid(child);
        
        children.add(child);
        convertToSmallest();
    }
    
    public abstract boolean isDynamic();
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    public void movePreviews(LittleGridContext context, LittleVec offset) {
        if (context.size > this.context.size)
            convertTo(context);
        else if (context.size < this.context.size)
            offset.convertTo(context, this.context);
        
        context = this.context;
        
        for (LittlePreview preview : previews)
            preview.box.add(offset);
        
        if (hasStructure())
            getStructureType().move(this, context, offset);
        
        if (hasChildren())
            for (LittlePreviews child : children)
                child.movePreviews(context, offset);
    }
    
    public void flipPreviews(Axis axis, LittleVec doubledCenter) {
        for (LittlePreview preview : previews)
            preview.flipPreview(axis, doubledCenter);
        
        if (hasStructure())
            getStructureType().flip(this, context, axis, doubledCenter);
        
        if (hasChildren())
            for (LittlePreviews child : children)
                child.flipPreviews(axis, doubledCenter);
    }
    
    public void rotatePreviews(Rotation rotation, LittleVec doubledCenter) {
        for (LittlePreview preview : previews)
            preview.rotatePreview(rotation, doubledCenter);
        
        if (hasStructure())
            getStructureType().rotate(this, context, rotation, doubledCenter);
        
        if (hasChildren())
            for (LittlePreviews child : children)
                child.rotatePreviews(rotation, doubledCenter);
    }
    
    @Override
    public int getSmallest() {
        int size = LittleGridContext.minSize;
        for (LittleTile tile : this)
            size = Math.max(size, tile.getSmallest(grid));
        
        LittleGrid context = LittleGrid.get(size);
        if (hasStructure())
            context = LittleGrid.max(context, getStructureType().getMinContext(this));
        
        size = context.size;
        if (hasChildren())
            for (LittleGroup child : children)
                size = Math.max(child.getSmallest(), size);
        return size;
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        if (grid != to)
            for (LittleTile tile : this)
                tile.convertTo(this.grid, to);
            
        if (hasChildren())
            for (LittleGroup child : children)
                child.convertTo(to);
        this.grid = to;
    }
    
    protected abstract LittleGroup emptyCopy();
    
    public LittleGroup copy() {
        LittleGroup previews = emptyCopy();
        for (LittleTile tile : this)
            previews.previews.add(preview.copy());
        
        for (LittleGroup child : this.children)
            previews.children.add(child.copy());
        return previews;
    }
    
    protected Iterator<LittleTile> allTilesIterator() {
        if (hasChildren())
            return new Iterator<LittleTile>() {
                
                public Iterator<LittleTile> subIterator = iterator();
                public Iterator<LittleGroup> children = children().iterator();
                
                @Override
                public boolean hasNext() {
                    while (!subIterator.hasNext()) {
                        if (!children.hasNext())
                            return false;
                        subIterator = children.next().allTilesIterator();
                    }
                    
                    return true;
                }
                
                @Override
                public LittleTile next() {
                    return subIterator.next();
                }
                
                @Override
                public void remove() {
                    subIterator.remove();
                }
            };
        return iterator();
    }
    
    public Iterable<LittleTile> allTiles() {
        return new Iterable<LittleTile>() {
            
            @Override
            public Iterator<LittleTile> iterator() {
                return allTilesIterator();
            }
        };
    }
    
    public LittlePreview get(int index) {
        return previews.get(index);
    }
    
    public int totalCount() {
        if (!hasChildren())
            return tilesCount();
        int size = tilesCount();
        for (LittleGroup child : children)
            size += child.totalCount();
        return size;
    }
    
    public boolean isEmptyIncludeChildren() {
        if (!isEmpty())
            return false;
        
        for (LittleGroup child : children)
            if (!child.isEmptyIncludeChildren())
                return false;
        return true;
    }
    
    public void addWithoutCheckingPreview(LittlePreview preview) {
        previews.add(preview);
    }
    
    public double getVolume() {
        double volume = 0;
        for (LittlePreview preview : this)
            volume += preview.getPercentVolume(context);
        return volume;
    }
    
    public double getVolumeIncludingChildren() {
        double volume = 0;
        for (LittlePreview preview : allPreviews())
            volume += preview.getPercentVolume(context);
        return volume;
    }
    
    public LittleVolumes getVolumes() {
        LittleVolumes volumes = new LittleVolumes(context);
        volumes.addPreviews(this);
        return volumes;
    }
    
    public boolean isVolumeEqual(LittlePreviews previews) {
        return getVolumes().equals(previews.getVolumes());
    }
    
    public void combinePreviewBlocks() {
        HashMapList<BlockPos, LittlePreview> chunked = new HashMapList<>();
        for (int i = 0; i < previews.size(); i++)
            chunked.add(previews.get(i).box.getMinVec().getBlockPos(context), previews.get(i));
        
        previews.clear();
        for (Iterator<ArrayList<LittlePreview>> iterator = chunked.values().iterator(); iterator.hasNext();) {
            ArrayList<LittlePreview> list = iterator.next();
            BasicCombiner.combine(list);
            previews.addAll(list);
        }
        
        if (hasChildren())
            for (LittlePreviews child : children)
                child.combinePreviewBlocks();
    }
    
    protected void advancedScale(int from, int to) {
        for (LittlePreview preview : previews)
            preview.convertTo(from, to);
        
        if (hasStructure())
            getStructureType().advancedScale(this, from, to);
        
        if (hasChildren())
            for (LittlePreviews child : children)
                child.advancedScale(from, to);
    }
    
    public LittleBox getSurroundingBox() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        
        for (LittlePreview preview : allPreviews()) {
            minX = Math.min(minX, preview.box.minX);
            minY = Math.min(minY, preview.box.minY);
            minZ = Math.min(minZ, preview.box.minZ);
            maxX = Math.max(maxX, preview.box.maxX);
            maxY = Math.max(maxY, preview.box.maxY);
            maxZ = Math.max(maxZ, preview.box.maxZ);
        }
        
        return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public LittleVec getMinVec() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        
        for (LittlePreview preview : allPreviews()) {
            minX = Math.min(minX, preview.box.minX);
            minY = Math.min(minY, preview.box.minY);
            minZ = Math.min(minZ, preview.box.minZ);
            maxX = Math.max(maxX, preview.box.maxX);
            maxY = Math.max(maxY, preview.box.maxY);
            maxZ = Math.max(maxZ, preview.box.maxZ);
        }
        
        return new LittleVec(minX, minY, minZ);
    }
    
    public LittleVec getSize() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        
        for (LittlePreview preview : allPreviews()) {
            minX = Math.min(minX, preview.box.minX);
            minY = Math.min(minY, preview.box.minY);
            minZ = Math.min(minZ, preview.box.minZ);
            maxX = Math.max(maxX, preview.box.maxX);
            maxY = Math.max(maxY, preview.box.maxY);
            maxZ = Math.max(maxZ, preview.box.maxZ);
        }
        
        return new LittleVec(maxX - minX, maxY - minY, maxZ - minZ);
    }
    
    public static void advancedScale(LittlePreviews previews, int from, int to) {
        previews.advancedScale(from, to);
    }
    
    public void removeOffset() {
        LittleVec min = getMinVec();
        min.x = context.toGrid(context.toBlockOffset(min.x));
        min.y = context.toGrid(context.toBlockOffset(min.y));
        min.z = context.toGrid(context.toBlockOffset(min.z));
        min.invert();
        movePreviews(context, min);
    }
    
    @Deprecated
    public static void setLittlePreviewsContextSecretly(LittlePreviews previews, LittleGridContext context) {
        if (previews.hasStructure())
            previews.getStructureType().advancedScale(previews, context.size, previews.context.size);
        previews.context = context;
        if (previews.hasChildren())
            for (LittlePreviews child : previews.getChildren())
                setLittlePreviewsContextSecretly(child, context);
    }
    
    public static LittlePreviews getChild(LittleGridContext context, NBTTagCompound nbt) {
        LittlePreviews previews;
        if (nbt.hasKey("structure"))
            previews = new LittlePreviews(nbt.getCompoundTag("structure"), context);
        else
            previews = new LittlePreviews(context);
        
        previews = LittleNBTCompressionTools.readPreviews(previews, nbt.getTagList("tiles", 10));
        if (nbt.hasKey("children")) {
            NBTTagList list = nbt.getTagList("children", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound child = list.getCompoundTagAt(i);
                previews.addChild(getChild(context, child), child.getBoolean("dynamic"));
            }
        }
        return previews;
    }
    
    public static LittlePreviews getPreview(ItemStack stack, boolean allowLowResolution) {
        if (!stack.hasTagCompound())
            return new LittlePreviews(LittleGridContext.get());
        
        LittleGridContext context = LittleGridContext.get(stack.getTagCompound());
        if (stack.getTagCompound().getTag("tiles") instanceof NBTTagInt) {
            LittlePreviews previews = new LittlePreviews(context);
            int tiles = stack.getTagCompound().getInteger("tiles");
            for (int i = 0; i < tiles; i++) {
                NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("tile" + i);
                LittlePreview preview = LittleTileRegistry.loadPreview(nbt);
                if (preview != null)
                    previews.previews.add(preview);
            }
            
            if (stack.getTagCompound().hasKey("structure"))
                return new LittlePreviews(stack.getTagCompound().getCompoundTag("structure"), previews);
            return previews;
        } else {
            if (allowLowResolution && stack.getTagCompound().hasKey("pos")) {
                LittlePreviews previews = new LittlePreviews(context);
                NBTTagCompound tileData = new NBTTagCompound();
                LittleTile tile = new LittleTile(LittleTiles.dyeableBlock, 0);
                tile.saveTileExtra(tileData);
                
                NBTTagList list = stack.getTagCompound().getTagList("pos", 11);
                for (int i = 0; i < list.tagCount(); i++) {
                    int[] array = list.getIntArrayAt(i);
                    previews.previews
                            .add(new LittlePreview(new LittleBox(array[0] * context.size, array[1] * context.size, array[2] * context.size, array[0] * context.size + context.maxPos, array[1] * context.size + context.maxPos, array[02] * context.size + context.maxPos), tileData));
                }
                
                if (stack.getTagCompound().hasKey("children")) {
                    list = stack.getTagCompound().getTagList("children", 10);
                    for (int i = 0; i < list.tagCount(); i++) {
                        NBTTagCompound child = list.getCompoundTagAt(i);
                        previews.addChild(getChild(context, child), child.getBoolean("dynamic"));
                    }
                }
                
                return previews;
            }
            LittlePreviews previews = stack.getTagCompound()
                    .hasKey("structure") ? new LittlePreviews(stack.getTagCompound().getCompoundTag("structure"), context) : new LittlePreviews(context);
            previews = LittleNBTCompressionTools.readPreviews(previews, stack.getTagCompound().getTagList("tiles", 10));
            
            if (stack.getTagCompound().hasKey("children")) {
                NBTTagList list = stack.getTagCompound().getTagList("children", 10);
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound child = list.getCompoundTagAt(i);
                    previews.addChild(getChild(context, child), child.getBoolean("dynamic"));
                }
            }
            
            return previews;
        }
    }
    
}
