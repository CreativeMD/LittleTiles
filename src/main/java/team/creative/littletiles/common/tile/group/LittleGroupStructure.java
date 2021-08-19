package team.creative.littletiles.common.tile.group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.volume.LittleVolumes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.collection.LittleCollection;

public class LittleGroupStructure extends LittleCollection implements LittleGroup {
    
    protected final CompoundTag nbt;
    private LittleGroup parent;
    private List<LittleGroup> children = new ArrayList<>();
    private LittleGrid grid;
    
    public LittleGroupStructure(CompoundTag nbt, LittleGrid grid) {
        this.grid = grid;
        this.nbt = nbt;
    }
    
    @Override
    public Iterable<LittleGroup> children() {
        return children;
    }
    
    @Override
    public boolean hasStructure() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public String getStructureName() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String getStructureId() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public LittleStructureType getStructureType() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void setDynamicChild(boolean dynamic) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public boolean containsIngredients() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public boolean isDynamic() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    protected LittleGroup emptyCopy() {
        // TODO Auto-generated method stub
        return null;
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
    
    @Override
    public void moveBoxes(LittleVecGrid vec) {
        for (LittleBox box : allBoxes())
            box.add(vec.getVec());
    }
    
    @Override
    public void mirrorBoxes(Axis axis, LittleVec doubledCenter) {
        for (LittleBox box : allBoxes())
            box.mirror(axis, doubledCenter);
    }
    
    @Override
    public void rotateBoxes(Rotation rotation, LittleVec doubledCenter) {
        for (LittleBox box : allBoxes())
            box.rotate(rotation, doubledCenter);
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
    
    @Override
    public default LittleGroup copy() {
        LittleGroup previews = emptyCopy();
        for (LittleTile tile : this)
            previews.add(tile.copy());
        
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
    
    @Override
    public double getVolume() {
        double volume = 0;
        for (LittleTile tile : this)
            volume += tile.getPercentVolume(grid);
        return volume;
    }
    
    @Override
    public LittleVolumes getVolumes() {
        LittleVolumes volumes = new LittleVolumes(grid);
        volumes.add(this);
        return volumes;
    }
    
    @Override
    public void combineBlockwiseInternal() {
        super.combineBlockwise();
    }
    
    @Override
    public void advancedScaleBoxes(int from, int to) {
        for (LittleBox box : boxes())
            box.convertTo(from, to);
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
    
    @Override
    public LittleGroupType type() {
        return LittleGroupType.STRUCTURE;
    }
    
}
