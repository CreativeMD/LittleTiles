package team.creative.littletiles.common.tile.group;

import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.preview.NBTTagCompound;
import com.creativemd.littletiles.common.tile.preview.NBTTagInt;
import com.creativemd.littletiles.common.tile.preview.NBTTagList;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.util.compression.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

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
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.tile.LittleTile;

public interface LittleGroup extends IGridBased {
    
    public Iterable<LittleGroup> children();
    
    public boolean hasStructure();
    
    public default boolean hasStructureIncludeChildren() {
        if (hasStructure())
            return true;
        for (LittleGroup child : children())
            if (child.hasStructureIncludeChildren())
                return true;
        return false;
    }
    
    public abstract String getStructureName();
    
    public abstract String getStructureId();
    
    public abstract LittleStructureType getStructureType();
    
    public abstract boolean containsIngredients();
    
    public default boolean hasParent() {
        return getParent() != null;
    }
    
    public LittleGroup getParent();
    
    public boolean hasChildren();
    
    public int childrenCount();
    
    public LittleGroupType type();
    
    public default boolean transformable() {
        if (type() == LittleGroupType.LINK)
            return false;
        for (LittleGroup child : children())
            if (!child.transformable())
                return false;
        return true;
    }
    
    @Deprecated
    public void moveBoxes(LittleVecGrid vec);
    
    public default void move(LittleVecGrid vec) {
        if (!transformable())
            throw new RuntimeException("Cannot transform group with links");
        
        forceSameGrid(vec);
        moveBoxes(vec);
        
        if (hasStructure())
            getStructureType().move(this, vec);
        
        if (hasChildren())
            for (LittleGroup child : children())
                child.move(vec);
    }
    
    @Deprecated
    public void mirrorBoxes(Axis axis, LittleVec doubledCenter);
    
    public default void mirror(Axis axis, LittleVec doubledCenter) {
        if (!transformable())
            throw new RuntimeException("Cannot transform group with links");
        
        mirrorBoxes(axis, doubledCenter);
        
        if (hasStructure())
            getStructureType().mirror(this, getGrid(), axis, doubledCenter);
        
        if (hasChildren())
            for (LittleGroup child : children())
                child.mirror(axis, doubledCenter);
    }
    
    @Deprecated
    public void rotateBoxes(Rotation rotation, LittleVec doubledCenter);
    
    public default void rotate(Rotation rotation, LittleVec doubledCenter) {
        if (!transformable())
            throw new RuntimeException("Cannot transform group with links");
        
        rotateBoxes(rotation, doubledCenter);
        
        if (hasStructure())
            getStructureType().rotate(this, getGrid(), rotation, doubledCenter);
        
        if (hasChildren())
            for (LittleGroup child : children())
                child.rotate(rotation, doubledCenter);
    }
    
    public LittleGroup copy();
    
    public int tileCount();
    
    public boolean isEmpty();
    
    public default int totalTileCount() {
        if (!hasChildren())
            return tileCount();
        int size = tileCount();
        for (LittleGroup child : children())
            size += child.totalTileCount();
        return size;
    }
    
    public default boolean isEmptyIncludeChildren() {
        if (!isEmpty())
            return false;
        
        for (LittleGroup child : children())
            if (!child.isEmptyIncludeChildren())
                return false;
        return true;
    }
    
    public double getVolume();
    
    public default double getVolumeIncludingChildren() {
        double volume = getVolume();
        for (LittleGroup child : children())
            volume += child.getVolumeIncludingChildren();
        return volume;
    }
    
    public LittleVolumes getVolumes();
    
    public default LittleVolumes getVolumesIncludingChildren() {
        LittleVolumes volume = getVolumes();
        for (LittleGroup child : children())
            volume.add(child.getVolumesIncludingChildren());
        return volume;
    }
    
    @Deprecated
    public void combineBlockwiseInternal();
    
    public default void combineBlockwise() {
        combineBlockwiseInternal();
        
        if (hasChildren())
            for (LittleGroup child : children())
                child.combineBlockwise();
    }
    
    @Deprecated
    public void advancedScaleBoxes(int from, int to);
    
    public default void advancedScale(int from, int to) {
        advancedScaleBoxes(from, to);
        
        if (hasStructure())
            getStructureType().advancedScale(this, from, to);
        
        if (hasChildren())
            for (LittleGroup child : children())
                child.advancedScale(from, to);
    }
    
    public static void advancedScale(LittleGroup group, int from, int to) {
        group.advancedScale(from, to);
    }
    
    @Deprecated
    public static void setContextSecretly(LittleGroup previews, LittleGrid grid) {
        if (previews.hasStructure())
            previews.getStructureType().advancedScale(previews, grid.count, previews.grid.count);
        previews.grid = grid;
        if (previews.hasChildren())
            for (LittleGroup child : previews.children())
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
