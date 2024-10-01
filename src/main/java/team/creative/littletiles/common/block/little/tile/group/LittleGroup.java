package team.creative.littletiles.common.block.little.tile.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.creativecore.common.util.type.Bunch;
import team.creative.creativecore.common.util.type.itr.FunctionIterator;
import team.creative.creativecore.common.util.type.itr.IterableIterator;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.collection.LittleCollection;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.volume.LittleVolumes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.box.LittlePlaceBox;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.attribute.LittleStructureAttribute;
import team.creative.littletiles.common.structure.connection.children.ItemChildrenList;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;

public class LittleGroup implements Bunch<LittleTile>, IGridBased {
    
    public static final String PARENT_KEY = "k";
    public static final String CHILDREN_KEY = "c";
    public static final String STRUCTURE_KEY = "s";
    public static final String EXTENSION_KEY = "e";
    public static final String EXTENSION_ID_KEY = "eid";
    public static final String TILES_KEY = "t";
    public static final String TRANSLUCENT_KEY = "trans";
    public static final String MIN_KEY = "min";
    public static final String SIZE_KEY = "size";
    public static final String TILES_COUNT_KEY = "tiles";
    public static final String BOXES_COUNT_KEY = "boxes";
    
    public static Component printTooltip(CompoundTag tag) {
        StringBuilder text = new StringBuilder();
        if (tag.contains(TILES_COUNT_KEY) && tag.contains(BOXES_COUNT_KEY))
            text.append(tag.getInt(TILES_COUNT_KEY) + " " + LanguageUtils.translate("gui.tile.count") + " " + tag.getInt(BOXES_COUNT_KEY) + " " + LanguageUtils.translate(
                "gui.box.count"));
        return Component.literal(text.toString());
    }
    
    public static void advancedScale(LittleGroup group, int from, int to) {
        group.advancedScale(from, to);
    }
    
    @Deprecated
    public static void setGridSecretly(LittleGroup previews, LittleGrid grid) {
        if (previews.hasStructure())
            previews.getStructureType().advancedScale(previews, grid.count, previews.grid.count);
        previews.grid = grid;
        if (previews.hasChildren())
            for (LittleGroup child : previews.children.all())
                setGridSecretly(child, grid);
    }
    
    public static LittleGroup loadLow(CompoundTag nbt) {
        if (nbt.getInt("count") > LittleTiles.CONFIG.building.lowResolutionBoxCount) {
            LittleGroup group = new LittleGroup();
            LittleVec max = getSize(nbt);
            LittleVec min = getMin(nbt);
            max.add(min);
            group.addTile(LittleGrid.get(nbt), new LittleTile(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE, new LittleBox(min, max)));
            return group;
        }
        return load(nbt);
    }
    
    public static LittleGroup load(CompoundTag nbt) {
        ListTag list = nbt.getList(CHILDREN_KEY, Tag.TAG_COMPOUND);
        List<LittleGroup> children = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++)
            children.add(load(list.getCompound(i)));
        
        CompoundTag structure = nbt.getCompound(STRUCTURE_KEY);
        if (structure.isEmpty())
            structure = null;
        LittleGroup group = new LittleGroup(structure, children);
        group.convertTo(LittleGrid.get(nbt));
        LittleCollection.load(group.content, nbt.getCompound(TILES_KEY));
        
        CompoundTag extensions = nbt.getCompound(EXTENSION_KEY);
        for (String id : extensions.getAllKeys())
            group.children.addExtension(id, load(extensions.getCompound(id)));
        return group;
    }
    
    public static CompoundTag saveChild(LittleGroup group) {
        CompoundTag nbt = new CompoundTag();
        if (group.hasStructure())
            nbt.put(STRUCTURE_KEY, group.getStructureTag());
        nbt.put(TILES_KEY, LittleCollection.save(group.content));
        group.grid.set(nbt);
        ListTag list = new ListTag();
        for (LittleGroup child : group.children.children())
            list.add(saveChild(child));
        if (!list.isEmpty())
            nbt.put(CHILDREN_KEY, list);
        
        CompoundTag extensions = new CompoundTag();
        for (Entry<String, LittleGroup> entry : group.children.extensionEntries())
            extensions.put(entry.getKey(), saveChild(entry.getValue()));
        
        if (!extensions.isEmpty())
            nbt.put(EXTENSION_KEY, extensions);
        return nbt;
    }
    
    public static CompoundTag save(LittleGroup group) {
        CompoundTag nbt = saveChild(group);
        
        group.getSize().save(SIZE_KEY, nbt);
        group.getMinVec().save(MIN_KEY, nbt);
        nbt.putInt(TILES_COUNT_KEY, group.totalTiles());
        nbt.putInt(BOXES_COUNT_KEY, group.totalBoxes());
        if (group.hasTranslucentBlocks())
            nbt.putBoolean(TRANSLUCENT_KEY, true);
        return nbt;
    }
    
    public static LittleVec getSize(CompoundTag nbt) {
        if (nbt.contains(SIZE_KEY))
            return new LittleVec(SIZE_KEY, nbt);
        return null;
    }
    
    public static LittleVec getMin(CompoundTag nbt) {
        if (nbt.contains(MIN_KEY))
            return new LittleVec(MIN_KEY, nbt);
        return null;
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void shrinkCubesToOneBlock(List<? extends RenderBox> cubes) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        for (RenderBox box : cubes) {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        float scale = 1;
        float sizeX = maxX - minX;
        if (sizeX > 1)
            scale = Math.min(scale, 1 / sizeX);
        float sizeY = maxY - minY;
        if (sizeY > 1)
            scale = Math.min(scale, 1 / sizeY);
        float sizeZ = maxZ - minZ;
        if (sizeZ > 1)
            scale = Math.min(scale, 1 / sizeZ);
        float offsetX = (minX + maxX) * -0.5F;
        float offsetY = (minY + maxY) * -0.5F;
        float offsetZ = (minZ + maxZ) * -0.5F;
        for (RenderBox box : cubes) {
            box.add(offsetX, offsetY, offsetZ);
            box.scale(scale);
            box.add(0.5F, 0.5F, 0.5F);
        }
    }
    
    protected CompoundTag structure;
    protected LittleCollection content = new LittleCollection();
    public final ItemChildrenList children;
    private LittleGrid grid;
    
    public LittleGroup() {
        this((CompoundTag) null, Collections.EMPTY_LIST);
    }
    
    public LittleGroup(CompoundTag structure, List<LittleGroup> children) {
        this.grid = LittleGrid.MIN;
        this.structure = structure;
        this.children = new ItemChildrenList(this, children);
        convertToSmallest(); // should ensure same grid for all
    }
    
    public LittleGroup(LittleGroup group, List<LittleGroup> children) {
        this.grid = group.getGrid();
        this.structure = group.structure;
        this.content = group.content;
        this.children = new ItemChildrenList(this, children);
        convertToSmallest(); // should ensure same grid for all
    }
    
    public LittleGroup(CompoundTag nbt, LittleGroup group, List<LittleGroup> children) {
        this.grid = group.getGrid();
        this.structure = nbt;
        this.content = group.content;
        this.children = new ItemChildrenList(this, children);
        convertToSmallest(); // should ensure same grid for all
    }
    
    public LittleGroup getParent() {
        return children.getParent();
    }
    
    public boolean hasParent() {
        return children.hasParent();
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
        for (LittleGroup child : children.all())
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
            return LittleStructureRegistry.REGISTRY.get(structure.getString("id"));
        return null;
    }
    
    public CompoundTag getStructureTag() {
        return structure;
    }
    
    public boolean transformable() {
        for (LittleGroup child : children.all())
            if (!child.transformable())
                return false;
        return true;
    }
    
    public boolean containsIngredients() {
        if (hasStructure())
            return !LittleStructureAttribute.premade(getStructureType().attribute);
        return true;
    }
    
    public void move(LittleVecGrid vec) {
        if (!transformable())
            throw new RuntimeException("Cannot transform group with links");
        
        forceSameGrid(vec);
        for (LittleBox box : boxes())
            box.add(vec.getVec());
        
        if (hasStructure())
            getStructureType().move(this, vec);
        
        if (hasChildren())
            for (LittleGroup child : children.all())
                child.move(vec);
    }
    
    public void mirror(Axis axis, LittleVec doubledCenter) {
        if (!transformable())
            throw new RuntimeException("Cannot transform group with links");
        
        for (LittleBox box : boxes())
            box.mirror(axis, doubledCenter);
        
        if (hasStructure())
            getStructureType().mirror(this, getGrid(), axis, doubledCenter);
        
        if (hasChildren())
            for (LittleGroup child : children.all())
                child.mirror(axis, doubledCenter);
    }
    
    public void rotate(Rotation rotation, LittleVec doubledCenter) {
        if (!transformable())
            throw new RuntimeException("Cannot transform group with links");
        
        for (LittleBox box : boxes())
            box.rotate(rotation, doubledCenter);
        
        if (hasStructure())
            getStructureType().rotate(this, getGrid(), rotation, doubledCenter);
        
        if (hasChildren())
            for (LittleGroup child : children.all())
                child.rotate(rotation, doubledCenter);
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public void convertToSmallest() {
        convertTo(LittleGrid.get(getSmallest()));
    }
    
    @Override
    public int getSmallest() {
        int size = LittleGrid.MIN.count;
        for (LittleTile tile : this)
            size = Math.max(size, tile.getSmallest(grid));
        
        LittleGrid context = LittleGrid.get(size);
        if (hasStructure())
            context = LittleGrid.max(context, getStructureType().getMinContext(this));
        
        size = context.count;
        if (hasChildren())
            for (LittleGroup child : children.all())
                size = Math.max(child.getSmallest(), size);
        return size;
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        if (grid != to)
            for (LittleTile tile : this)
                tile.convertTo(this.grid, to);
            
        if (hasChildren())
            for (LittleGroup child : children.all())
                child.convertTo(to);
        this.grid = to;
    }
    
    public LittleGroup copyExceptChildren() {
        LittleGroup group = new LittleGroup(structure, Collections.EMPTY_LIST);
        for (Entry<String, LittleGroup> extension : children.extensionEntries())
            group.children.addExtension(extension.getKey(), extension.getValue().copy());
        group.addAll(grid, new FunctionIterator<>(this, x -> x.copy()));
        return group;
    }
    
    public LittleGroup copy() {
        List<LittleGroup> newChildren = new ArrayList<>();
        for (LittleGroup group : children.children())
            newChildren.add(group.copy());
        LittleGroup group = new LittleGroup(structure, newChildren);
        for (Entry<String, LittleGroup> extension : children.extensionEntries())
            group.children.addExtension(extension.getKey(), extension.getValue().copy());
        group.addAll(grid, new FunctionIterator<>(this, x -> x.copy()));
        return group;
    }
    
    @Override
    public Iterator<LittleTile> iterator() {
        return content.iterator();
    }
    
    public Iterable<LittleTile> allTiles() {
        if (hasChildren())
            return new IterableIterator<LittleTile>() {
                
                public Iterator<LittleTile> subIterator = LittleGroup.this.iterator();
                public Iterator<LittleGroup> children = LittleGroup.this.children.all().iterator();
                
                @Override
                public boolean hasNext() {
                    while (!subIterator.hasNext()) {
                        if (!children.hasNext())
                            return false;
                        subIterator = children.next().allTiles().iterator();
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
        return this;
    }
    
    public Iterable<LittleBox> allBoxes() {
        return new IterableIterator<LittleBox>() {
            
            public Iterator<LittleBox> subIterator = null;
            public Iterator<LittleTile> children = allTiles().iterator();
            
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
    
    public double getVolume() {
        double volume = 0;
        for (LittleTile tile : this)
            volume += tile.getPercentVolume(grid);
        return volume;
    }
    
    public double getVolumeIncludingChildren() {
        double volume = getVolume();
        for (LittleGroup child : children.all())
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
        for (LittleGroup child : children.all())
            volume.add(child.getVolumesIncludingChildren());
        return volume;
    }
    
    public void combineBlockwise() {
        content.combineBlockwise(this.grid);
        
        if (hasChildren())
            for (LittleGroup child : children.all())
                child.combineBlockwise();
    }
    
    public void advancedScale(int from, int to) {
        for (LittleBox box : boxes())
            box.convertTo(from, to);
        
        if (hasStructure())
            getStructureType().advancedScale(this, from, to);
        
        if (hasChildren())
            for (LittleGroup child : children.all())
                child.advancedScale(from, to);
    }
    
    public boolean isEmptyIncludeChildren() {
        if (!isEmpty())
            return false;
        
        for (LittleGroup child : children.all())
            if (!child.isEmptyIncludeChildren())
                return false;
        return true;
    }
    
    public Iterable<LittleBox> boxes() {
        return content.boxes();
    }
    
    public boolean isEmpty() {
        return content.isEmpty();
    }
    
    @Override
    public int size() {
        return content.size();
    }
    
    public int boxesCount() {
        return content.boxesCount();
    }
    
    public int totalTiles() {
        if (!hasChildren())
            return size();
        int size = size();
        for (LittleGroup child : children.all())
            size += child.totalTiles();
        return size;
    }
    
    public int totalBoxes() {
        if (!hasChildren())
            return boxesCount();
        int size = boxesCount();
        for (LittleGroup child : children.all())
            size += child.totalBoxes();
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
    
    public List<LittlePlaceBox> getSpecialBoxes() {
        if (hasStructure())
            return getStructureType().getSpecialBoxes(this);
        return Collections.EMPTY_LIST;
    }
    
    public void add(LittleGroup group) {
        sameGrid(group, () -> content.addAll(group));
    }
    
    public void addTileFast(LittleGrid grid, LittleTile tile) {
        if (grid != this.grid)
            if (grid.count > this.grid.count)
                convertTo(grid);
            else
                tile.convertTo(grid, this.grid);
            
        content.add(tile);
    }
    
    public void addTile(LittleGrid grid, LittleTile tile) {
        addTileFast(grid, tile);
        convertToSmallest();
    }
    
    public void addAll(LittleGrid grid, Iterable<LittleTile> tiles) {
        boolean needsConvert = false;
        if (grid != this.grid) {
            if (grid.count > this.grid.count)
                convertTo(grid);
            else
                needsConvert = true;
        }
        
        for (LittleTile tile : tiles) {
            if (needsConvert)
                tile.convertTo(grid, this.grid);
            content.add(tile);
        }
        
        convertToSmallest();
    }
    
    public void add(LittleGrid grid, LittleElement element, Iterable<LittleBox> boxes) {
        if (grid != this.grid)
            if (grid.count > this.grid.count)
                convertTo(grid);
            else
                for (LittleBox box : boxes)
                    box.convertTo(grid, this.grid);
                
        content.add(element, boxes);
        
        convertToSmallest();
    }
    
    public void addFast(LittleGrid grid, LittleElement element, LittleBox box) {
        if (grid != this.grid)
            if (grid.count > this.grid.count)
                convertTo(grid);
            else
                box.convertTo(grid, this.grid);
            
        content.add(element, box);
    }
    
    public void add(LittleGrid grid, LittleElement element, LittleBox box) {
        addFast(grid, element, box);
        convertToSmallest();
    }
    
    public void combine(boolean optimized) {
        content.combine(grid, optimized);
    }
    
    public Set<BlockPos> getPositions(BlockPos pos) {
        HashSet<BlockPos> positions = new HashSet<>();
        for (LittleBox box : allBoxes())
            positions.add(box.getMinVec().getBlockPos(grid).offset(pos));
        return positions;
    }
    
    @OnlyIn(Dist.CLIENT)
    public List<RenderBox> getPlaceBoxes(LittleVec offset) {
        List<RenderBox> boxes = new ArrayList<>();
        addPlaceBoxes(boxes, offset);
        return boxes;
    }
    
    @OnlyIn(Dist.CLIENT)
    protected void addPlaceBoxes(List<RenderBox> boxes, LittleVec offset) {
        for (LittleTile tile : content)
            tile.addPlaceBoxes(grid, boxes, offset);
        if (hasStructure()) {
            List<LittlePlaceBox> structureBoxes = getStructureType().getSpecialBoxes(this);
            if (structureBoxes != null)
                for (LittlePlaceBox box : structureBoxes)
                    boxes.add(box.getRenderBox(grid, offset));
        }
        for (LittleGroup child : children.all())
            child.addPlaceBoxes(boxes, offset);
    }
    
    public boolean hasTranslucentBlocks() {
        for (LittleTile tile : content)
            if (tile.isTranslucent())
                return true;
        if (hasStructure() && getStructureType().hasTranslucentItemPreview(this))
            return true;
        for (LittleGroup child : children.all())
            if (child.hasTranslucentBlocks())
                return true;
        return false;
    }
    
    @OnlyIn(Dist.CLIENT)
    public List<RenderBox> getRenderingBoxes(boolean translucent) {
        List<RenderBox> boxes = new ArrayList<>();
        addRenderingBoxes(boxes, translucent, !hasChildren());
        return boxes;
    }
    
    @OnlyIn(Dist.CLIENT)
    protected void addRenderingBoxes(List<RenderBox> boxes, boolean translucent, boolean itemPreview) {
        for (LittleTile tile : content)
            if (tile.isTranslucent() == translucent)
                tile.addRenderingBoxes(grid, boxes);
        if (hasStructure() && itemPreview) {
            List<RenderBox> structureBoxes = getStructureType().getItemPreview(this, translucent);
            if (structureBoxes != null)
                boxes.addAll(structureBoxes);
        }
        for (LittleGroup child : children.all())
            child.addRenderingBoxes(boxes, translucent, false);
    }
    
    public boolean isVolumeEqual(LittleGroup previews) {
        return getVolumes().equals(previews.getVolumes());
    }
    
}
