package team.creative.littletiles.common.tile.group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.volume.LittleVolumes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.structure.LittleStructureAttribute;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.connection.ItemChildrenList;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.collection.LittleCollection;

public class LittleGroup extends LittleCollection implements IGridBased {
    
    protected CompoundTag structure;
    private LittleGroup parent;
    public final ItemChildrenList children;
    private LittleGrid grid;
    
    public LittleGroup(CompoundTag structure, LittleGrid grid, List<LittleGroup> children) {
        this.grid = grid;
        this.structure = structure;
        this.children = new ItemChildrenList(this, children);
        convertToSmallest();
    }
    
    public LittleGroup getParent() {
        return parent;
    }
    
    public boolean hasParent() {
        return parent != null;
    }
    
    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    public boolean hasStructure() {
        return structure != null;
    }
    
    public boolean hasStructureIncludeChildren() {
        if (hasStructure())
            return true;
        for (LittleGroup child : children)
            if (child.hasStructureIncludeChildren())
                return true;
        return false;
    }
    
    public String getStructureName() {
        if (!hasStructure())
            return null;
        return structure.contains("name") ? structure.getString("name") : null;
    }
    
    public String getStructureId() {
        if (hasStructure())
            return structure.getString("id");
        return null;
    }
    
    public LittleStructureType getStructureType() {
        if (hasStructure())
            return LittleStructureRegistry.getStructureType(structure.getString("id"));
        return null;
    }
    
    public CompoundTag getStructureTag() {
        return structure;
    }
    
    public boolean transformable() {
        for (LittleGroup child : children)
            if (!child.transformable())
                return false;
        return true;
    }
    
    public boolean containsIngredients() {
        if (hasStructure())
            return !LittleStructureAttribute.premade(getStructureType().attribute);
        return true;
    }
    
    @SuppressWarnings("deprecation")
    public void move(LittleVecGrid vec) {
        if (!transformable())
            throw new RuntimeException("Cannot transform group with links");
        
        forceSameGrid(vec);
        for (LittleBox box : allBoxes())
            box.add(vec.getVec());
        
        if (hasStructure())
            getStructureType().move(this, vec);
        
        if (hasChildren())
            for (LittleGroup child : children)
                child.move(vec);
    }
    
    public void mirror(Axis axis, LittleVec doubledCenter) {
        if (!transformable())
            throw new RuntimeException("Cannot transform group with links");
        
        for (LittleBox box : allBoxes())
            box.mirror(axis, doubledCenter);
        
        if (hasStructure())
            getStructureType().mirror(this, getGrid(), axis, doubledCenter);
        
        if (hasChildren())
            for (LittleGroup child : children)
                child.mirror(axis, doubledCenter);
    }
    
    public void rotate(Rotation rotation, LittleVec doubledCenter) {
        if (!transformable())
            throw new RuntimeException("Cannot transform group with links");
        
        for (LittleBox box : allBoxes())
            box.rotate(rotation, doubledCenter);
        
        if (hasStructure())
            getStructureType().rotate(this, getGrid(), rotation, doubledCenter);
        
        if (hasChildren())
            for (LittleGroup child : children)
                child.rotate(rotation, doubledCenter);
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public int getSmallest() {
        int size = LittleGrid.min().count;
        for (LittleTile tile : this)
            size = Math.max(size, tile.getSmallest(grid));
        
        LittleGrid context = LittleGrid.get(size);
        if (hasStructure())
            context = LittleGrid.max(context, getStructureType().getMinContext(this));
        
        size = context.count;
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
    
    public LittleGroup copy() {
        List<LittleGroup> newChildren = new ArrayList<>();
        for (LittleGroup group : children.children())
            newChildren.add(group.copy());
        LittleGroup group = new LittleGroup(structure, grid, newChildren);
        for (LittleTile tile : this)
            group.add(tile.copy());
        return group;
    }
    
    protected Iterator<LittleTile> allTilesIterator() {
        if (hasChildren())
            return new Iterator<LittleTile>() {
                
                public Iterator<LittleTile> subIterator = iterator();
                public Iterator<LittleGroup> children = LittleGroup.this.children.iterator();
                
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
    
    public Iterable<LittleBox> allBoxes() {
        return new Iterable<LittleBox>() {
            
            @Override
            public Iterator<LittleBox> iterator() {
                return new Iterator<LittleBox>() {
                    
                    public Iterator<LittleBox> subIterator = null;
                    public Iterator<LittleTile> children = allTilesIterator();
                    
                    @Override
                    public boolean hasNext() {
                        while (subIterator == null || !subIterator.hasNext()) {
                            if (!children.hasNext())
                                return false;
                            subIterator = children.next().iterator();
                        }
                        
                        return true;
                    }
                    
                    @Override
                    public LittleBox next() {
                        return subIterator.next();
                    }
                    
                    @Override
                    public void remove() {
                        subIterator.remove();
                    }
                };
            }
        };
    }
    
    public double getVolume() {
        double volume = 0;
        for (LittleTile tile : this)
            volume += tile.getPercentVolume(grid);
        return volume;
    }
    
    public double getVolumeIncludingChildren() {
        double volume = getVolume();
        for (LittleGroup child : children)
            volume += child.getVolumeIncludingChildren();
        return volume;
    }
    
    public LittleVolumes getVolumes() {
        LittleVolumes volumes = new LittleVolumes(grid);
        volumes.add(this);
        return volumes;
    }
    
    public LittleVolumes getVolumesIncludingChildren() {
        LittleVolumes volume = getVolumes();
        for (LittleGroup child : children)
            volume.add(child.getVolumesIncludingChildren());
        return volume;
    }
    
    @Override
    public void combineBlockwise() {
        super.combineBlockwise();
        
        if (hasChildren())
            for (LittleGroup child : children)
                child.combineBlockwise();
    }
    
    public void advancedScale(int from, int to) {
        for (LittleBox box : boxes())
            box.convertTo(from, to);
        
        if (hasStructure())
            getStructureType().advancedScale(this, from, to);
        
        if (hasChildren())
            for (LittleGroup child : children)
                child.advancedScale(from, to);
    }
    
    public boolean isEmptyIncludeChildren() {
        if (!isEmpty())
            return false;
        
        for (LittleGroup child : children)
            if (!child.isEmptyIncludeChildren())
                return false;
        return true;
    }
    
    public int totalSize() {
        if (!hasChildren())
            return size();
        int size = size();
        for (LittleGroup child : children)
            size += child.totalSize();
        return size;
    }
    
    public LittleBox getSurroundingBox() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        
        for (LittleBox box : allBoxes()) {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
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
        
        for (LittleBox box : allBoxes()) {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
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
        
        for (LittleBox box : allBoxes()) {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        
        return new LittleVec(maxX - minX, maxY - minY, maxZ - minZ);
    }
    
    public void removeOffset() {
        LittleVec min = getMinVec();
        min.x = grid.toGrid(grid.toBlockOffset(min.x));
        min.y = grid.toGrid(grid.toBlockOffset(min.y));
        min.z = grid.toGrid(grid.toBlockOffset(min.z));
        min.invert();
        move(new LittleVecGrid(min, grid));
    }
    
    public static void advancedScale(LittleGroup group, int from, int to) {
        group.advancedScale(from, to);
    }
    
    @Deprecated
    public static void setGroupParent(LittleGroup group, LittleGroup parent) {
        group.parent = parent;
    }
    
    @Deprecated
    public static void setContextSecretly(LittleGroup previews, LittleGrid grid) {
        if (previews.hasStructure())
            previews.getStructureType().advancedScale(previews, grid.count, previews.grid.count);
        previews.grid = grid;
        if (previews.hasChildren())
            for (LittleGroup child : previews.children)
                setContextSecretly(child, grid);
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
    
    public static LittleGroup load(ItemStack stack, boolean allowLowResolution) {
        if (!stack.hasTag())
            return new LittleGroup();
        
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
