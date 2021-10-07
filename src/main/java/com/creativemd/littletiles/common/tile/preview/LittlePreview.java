package com.creativemd.littletiles.common.tile.preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.block.ISpecialBlockHandler;
import com.creativemd.littletiles.common.api.block.SpecialBlockHandler;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.combine.ICombinable;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.compression.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredientEntry;
import com.creativemd.littletiles.common.util.ingredient.IngredientUtils;
import com.creativemd.littletiles.common.util.outdated.LittleSize;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittlePreview implements ICombinable {
    
    // ================Type ID================
    
    private static HashMap<String, Class<? extends LittlePreview>> previewTypes = new HashMap<>();
    
    public static void registerPreviewType(String id, Class<? extends LittlePreview> type) {
        previewTypes.put(id, type);
    }
    
    public String getTypeId() {
        return LittleTileRegistry.getPreviewId(getClass());
    }
    
    public String getTypeIdToSave() {
        if (isCustomPreview())
            return LittleTileRegistry.getPreviewId(getClass());
        return null;
    }
    
    public boolean isCustomPreview() {
        return this.getClass() != LittleTileRegistry.getDefaultPreviewClass();
    }
    
    // ================Data================
    
    protected NBTTagCompound tileData;
    public LittleBox box;
    
    // ================Constructors================
    
    /** This constructor needs to be implemented in every subclass **/
    public LittlePreview(NBTTagCompound nbt) {
        if (nbt.hasKey("bBoxminX") || nbt.hasKey("bBox")) {
            box = LittleBox.loadBox("bBox", nbt);
        } else if (nbt.hasKey("sizex") || nbt.hasKey("size")) {
            LittleVec size = LittleSize.loadSize("size", nbt);
            box = new LittleBox(0, 0, 0, size.x, size.y, size.z);
        } else
            box = new LittleBox(0, 0, 0, 1, 1, 1);
        
        if (nbt.hasKey("tile")) // new way
            tileData = nbt.getCompoundTag("tile");
        else { // Old way
            tileData = nbt.copy();
            tileData.removeTag("bBox");
            tileData.removeTag("size");
        }
        
        tileData.removeTag("nodrop");
        if (tileData.hasKey("tID") && !LittleTileRegistry.getTileType(tileData.getString("tID")).saveId)
            tileData.removeTag("tID");
        if (tileData.hasKey("meta")) {
            int meta = tileData.getInteger("meta");
            tileData.removeTag("meta");
            if (meta != 0)
                tileData.setString("block", tileData.getString("block") + ":" + meta);
        }
    }
    
    public LittlePreview(LittleBox box, NBTTagCompound tileData) {
        this.box = box;
        this.tileData = tileData;
    }
    
    // ================Preview================
    
    public String getBlockInfo() {
        return tileData.getString("block");
    }
    
    public String getBlockName() {
        String[] parts = tileData.getString("block").split(":");
        if (parts.length < 2)
            return parts[0];
        return parts[0] + ":" + parts[1];
    }
    
    public Block getBlock() {
        return Block.getBlockFromName(getBlockName());
    }
    
    public int getMeta() {
        String[] parts = tileData.getString("block").split(":");
        if (parts.length == 3)
            return Integer.parseInt(parts[2]);
        return 0;
    }
    
    public boolean hasColor() {
        return tileData.hasKey("color");
    }
    
    public int getColor() {
        if (tileData.hasKey("color"))
            return tileData.getInteger("color");
        return -1;
    }
    
    public void setColor(int color) {
        if (ColorUtils.isWhite(color) && !ColorUtils.isTransparent(color)) {
            if (tileData.getString("tID").equals("BlockTileColored"))
                tileData.setString("tID", "BlockTileBlock");
            tileData.removeTag("color");
        } else {
            if (tileData.getString("tID").equals("BlockTileBlock"))
                tileData.setString("tID", "BlockTileColored");
            tileData.setInteger("color", color);
        }
    }
    
    /** Rendering inventory **/
    @SideOnly(Side.CLIENT)
    public RenderBox getCubeBlock(LittleGridContext context) {
        RenderBox cube = box.getRenderingCube(context, getBlock(), getMeta());
        cube.color = getColor();
        return cube;
    }
    
    public boolean isInvisible() {
        return tileData.getBoolean("invisible") || ColorUtils.isInvisible(getColor());
    }
    
    public void setInvisibile(boolean invisible) {
        tileData.setBoolean("invisible", invisible);
    }
    
    public NBTTagCompound getTileData() {
        return tileData;
    }
    
    @Nullable
    public BlockIngredientEntry getBlockIngredient(LittleGridContext context) {
        return IngredientUtils.getBlockIngredient(getBlock(), getMeta(), getPercentVolume(context));
    }
    
    /** @param context
     * @return An ordinary itemstack of the block, not taking care of actual preview
     *         size (it's always a full cube). */
    public ItemStack getBlockStack() {
        return new ItemStack(getBlock(), 1, getMeta());
    }
    
    public double getPercentVolume(LittleGridContext context) {
        return box.getPercentVolume(context);
    }
    
    public double getVolume() {
        return box.getVolume();
    }
    
    public int getSmallestContext(LittleGridContext context) {
        return box.getSmallestContext(context);
    }
    
    public void convertTo(LittleGridContext from, LittleGridContext to) {
        box.convertTo(from, to);
    }
    
    public void convertTo(int from, int to) {
        box.convertTo(from, to);
    }
    
    public boolean canBeCombined(LittlePreview preview) {
        return tileData.equals(preview.getTileData());
    }
    
    @Override
    public LittleBox getBox() {
        return box;
    }
    
    @Override
    public void setBox(LittleBox box) {
        this.box = box;
    }
    
    @Override
    public boolean canCombine(ICombinable combinable) {
        return canBeCombined((LittlePreview) combinable);
    }
    
    @Override
    public boolean fillInSpace(LittleBox otherBox, boolean[][][] filled) {
        return this.box.fillInSpace(otherBox, filled);
    }
    
    // ================Copy================
    
    @Override
    public LittlePreview copy() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        nbt.setTag("tile", tileData.copy());
        return LittleTileRegistry.loadPreview(nbt);
    }
    
    // ================Common================
    
    public ISpecialBlockHandler getSpecialHandler() {
        return SpecialBlockHandler.getSpecialBlockHandler(getBlock(), getMeta());
    }
    
    @Override
    public String toString() {
        return box.toString();
    }
    
    // ================Placing================
    
    /** Used for placing the tile **/
    public LittleTile getLittleTile() {
        return LittleTileRegistry.loadTile(tileData);
    }
    
    public PlacePreview getPlaceableTile(LittleVec offset) {
        LittleBox newBox = this.box.copy();
        if (offset != null)
            newBox.add(offset);
        return new PlacePreview(newBox, this);
    }
    
    // ================Rotating/Flipping================
    
    public void flipPreview(Axis axis, LittleVec doubledCenter) {
        box.flipBox(axis, doubledCenter);
        getSpecialHandler().flipPreview(axis, this, doubledCenter);
    }
    
    public void rotatePreview(Rotation rotation, LittleVec doubledCenter) {
        box.rotateBox(rotation, doubledCenter);
        getSpecialHandler().rotatePreview(rotation, this, doubledCenter);
    }
    
    // ================Save & Loading================
    
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("bBox", box.getNBTIntArray());
        nbt.setTag("tile", tileData);
        if (isCustomPreview())
            nbt.setString("type", getTypeId());
    }
    
    // ================Grouping================
    
    public List<NBTTagCompound> extractNBTFromGroup(NBTTagCompound nbt) {
        List<NBTTagCompound> tags = new ArrayList<>();
        NBTTagList list = nbt.getTagList("boxes", 11);
        
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound copy = nbt.copy();
            copy.removeTag("boxes");
            copy.setIntArray("bBox", list.getIntArrayAt(i));
            tags.add(copy);
        }
        return tags;
    }
    
    public boolean canBeNBTGrouped(LittlePreview preview) {
        return this.box != null && preview.box != null && preview.getTileData().equals(this.getTileData());
    }
    
    public NBTTagCompound startNBTGrouping() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        nbt.removeTag("bBox");
        NBTTagList list = new NBTTagList();
        list.appendTag(box.getNBTIntArray());
        nbt.setTag("boxes", list);
        return nbt;
    }
    
    public void groupNBTTile(NBTTagCompound nbt, LittlePreview preview) {
        NBTTagList list = nbt.getTagList("boxes", 11);
        list.appendTag(preview.box.getNBTIntArray());
    }
    
    // ================Static Helpers================
    
    public static int lowResolutionMode = 2000;
    
    public static LittlePreviews getPreview(ItemStack stack) {
        return getPreview(stack, false);
    }
    
    public static LittlePreviews getPreview(ItemStack stack, boolean allowLowResolution) {
        return LittlePreviews.getPreview(stack, allowLowResolution);
    }
    
    public static LittleVec getSize(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("size"))
            return LittleSize.loadSize("size", stack.getTagCompound());
        return new LittleVec(1, 1, 1);
    }
    
    public static LittleVec getOffset(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("min"))
            return new LittleVec("min", stack.getTagCompound());
        return null;
    }
    
    public static void removePreviewTiles(ItemStack stack) {
        if (!stack.hasTagCompound())
            return;
        NBTTagCompound nbt = stack.getTagCompound();
        LittleGridContext.remove(nbt);
        nbt.removeTag("size");
        nbt.removeTag("min");
        nbt.removeTag("pos");
        nbt.removeTag("tiles");
        nbt.removeTag("count");
        nbt.removeTag("children");
    }
    
    public static NBTTagCompound saveChildPreviews(LittlePreviews previews) {
        NBTTagCompound nbt = new NBTTagCompound();
        if (previews.hasStructure()) {
            nbt.setTag("structure", previews.structureNBT);
            if (previews.isDynamic())
                nbt.setBoolean("dynamic", true);
        }
        
        NBTTagList list = LittleNBTCompressionTools.writePreviews(previews);
        nbt.setTag("tiles", list);
        nbt.setInteger("count", previews.size());
        
        if (previews.hasChildren()) {
            NBTTagList children = new NBTTagList();
            for (LittlePreviews child : previews.getChildren())
                children.appendTag(saveChildPreviews(child));
            
            nbt.setTag("children", children);
        }
        return nbt;
    }
    
    public static void savePreview(LittlePreviews previews, ItemStack stack) {
        if (previews instanceof LittleAbsolutePreviews)
            throw new IllegalArgumentException("Absolute positions cannot be saved!");
        
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        if (previews.hasStructure())
            stack.getTagCompound().setTag("structure", previews.structureNBT);
        
        previews.getContext().set(stack.getTagCompound());
        
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        
        for (LittlePreview preview : previews.allPreviews()) {
            minX = Math.min(minX, preview.box.minX);
            minY = Math.min(minY, preview.box.minY);
            minZ = Math.min(minZ, preview.box.minZ);
            maxX = Math.max(maxX, preview.box.maxX);
            maxY = Math.max(maxY, preview.box.maxY);
            maxZ = Math.max(maxZ, preview.box.maxZ);
        }
        
        new LittleVec(maxX - minX, maxY - minY, maxZ - minZ).writeToNBT("size", stack.getTagCompound());
        new LittleVec(minX, minY, minZ).writeToNBT("min", stack.getTagCompound());
        
        if (previews.totalSize() >= lowResolutionMode) {
            NBTTagList list = new NBTTagList();
            // BlockPos lastPos = null;
            
            HashSet<BlockPos> positions = new HashSet<>();
            
            for (int i = 0; i < previews.size(); i++) { // Will not be sorted after rotating
                BlockPos pos = previews.get(i).box.getMinVec().getBlockPos(previews.getContext());
                if (!positions.contains(pos)) {
                    positions.add(pos);
                    list.appendTag(new NBTTagIntArray(new int[] { pos.getX(), pos.getY(), pos.getZ() }));
                }
            }
            stack.getTagCompound().setTag("pos", list);
        } else
            stack.getTagCompound().removeTag("pos");
        
        NBTTagList list = LittleNBTCompressionTools.writePreviews(previews);
        stack.getTagCompound().setTag("tiles", list);
        
        stack.getTagCompound().setInteger("count", previews.size());
        if (previews.hasChildren()) {
            NBTTagList children = new NBTTagList();
            for (LittlePreviews child : previews.getChildren()) {
                children.appendTag(saveChildPreviews(child));
            }
            
            stack.getTagCompound().setTag("children", children);
        } else
            stack.getTagCompound().removeTag("children");
    }
    
    public static void saveTiles(World world, LittleGridContext context, TileEntityLittleTiles te, ItemStack stack) {
        stack.setTagCompound(new NBTTagCompound());
        
        LittlePreviews previews = new LittlePreviews(context);
        for (Pair<IParentTileList, LittleTile> pair : te.allTiles())
            previews.addTile(pair.key, pair.value);
        savePreview(previews, stack);
    }
    
    @SideOnly(Side.CLIENT)
    public static List<RenderBox> getCubes(LittlePreviews previews) {
        List<RenderBox> cubes = new ArrayList<RenderBox>();
        for (LittlePreview preview : previews.allPreviews())
            if (!preview.isInvisible())
                cubes.add(preview.getCubeBlock(previews.getContext()));
        return cubes;
    }
    
    public static void shrinkCubesToOneBlock(List<RenderBox> cubes) {
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
        float offsetX = -minX;
        float offsetY = -minY;
        float offsetZ = -minZ;
        float offsetX2 = (1 - sizeX * scale) * 0.5F;
        float offsetY2 = (1 - sizeY * scale) * 0.5F;
        float offsetZ2 = (1 - sizeZ * scale) * 0.5F;
        for (RenderBox box : cubes) {
            box.add(offsetX, offsetY, offsetZ);
            box.scale(scale);
            box.add(offsetX2, offsetY2, offsetZ2);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public static List<RenderBox> getCubesForStackRendering(ItemStack stack) {
        List<RenderBox> cubes = getCubes(stack, true);
        shrinkCubesToOneBlock(cubes);
        return cubes;
    }
    
    public static int getTotalCount(NBTTagCompound nbt) {
        int count = nbt.getInteger("count");
        if (nbt.hasKey("children")) {
            NBTTagList list = nbt.getTagList("children", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                count += getTotalCount(list.getCompoundTagAt(i));
            }
        }
        return count;
    }
    
    @SideOnly(Side.CLIENT)
    public static ArrayList<RenderBox> getCubes(ItemStack stack, boolean allowLowResolution) {
        ArrayList<RenderBox> cubes = new ArrayList<RenderBox>();
        if (stack.hasTagCompound() && getTotalCount(stack.getTagCompound()) >= lowResolutionMode && allowLowResolution) {
            NBTTagList list = stack.getTagCompound().getTagList("pos", 11);
            for (int i = 0; i < list.tagCount(); i++) {
                int[] array = list.getIntArrayAt(i);
                cubes.add(new RenderBox(array[0], array[1], array[2], array[0] + 1, array[1] + 1, array[2] + 1, LittleTiles.dyeableBlock));
            }
        } else {
            LittlePreviews previews = getPreview(stack);
            for (LittlePreview preview : previews.allPreviews())
                if (!preview.isInvisible())
                    cubes.add(preview.getCubeBlock(previews.getContext()));
        }
        return cubes;
    }
}
