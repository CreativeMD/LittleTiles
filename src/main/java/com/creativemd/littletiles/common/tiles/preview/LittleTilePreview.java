package com.creativemd.littletiles.common.tiles.preview;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.blocks.ISpecialBlockHandler;
import com.creativemd.littletiles.common.api.blocks.SpecialBlockHandler;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.combine.ICombinable;
import com.creativemd.littletiles.common.tiles.place.FixedHandler;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.compression.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredientEntry;
import com.creativemd.littletiles.common.utils.ingredients.IngredientUtils;

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

public class LittleTilePreview implements ICombinable {
	
	// ================Type ID================
	
	private static HashMap<String, Class<? extends LittleTilePreview>> previewTypes = new HashMap<>();
	
	public static void registerPreviewType(String id, Class<? extends LittleTilePreview> type) {
		previewTypes.put(id, type);
	}
	
	public String getTypeID() {
		if (!isCustomPreview())
			return "";
		for (Entry<String, Class<? extends LittleTilePreview>> type : previewTypes.entrySet())
			if (type.getValue() == this.getClass())
				return type.getKey();
		return "";
	}
	
	public boolean isCustomPreview() {
		return this.getClass() != LittleTilePreview.class;
	}
	
	// ================Data================
	
	public boolean canSplit = true;
	
	protected NBTTagCompound tileData;
	
	public LittleTileBox box;
	
	public List<FixedHandler> fixedhandlers = new ArrayList<FixedHandler>();
	
	// ================Constructors================
	
	/** This constructor needs to be implemented in every subclass **/
	public LittleTilePreview(NBTTagCompound nbt) {
		if (nbt.hasKey("bBoxminX") || nbt.hasKey("bBox")) {
			box = LittleTileBox.loadBox("bBox", nbt);
		} else if (nbt.hasKey("sizex") || nbt.hasKey("size")) {
			LittleTileSize size = new LittleTileSize("size", nbt);
			box = new LittleTileBox(0, 0, 0, size.sizeX, size.sizeY, size.sizeZ);
		} else
			box = new LittleTileBox(0, 0, 0, 1, 1, 1);
		
		if (nbt.hasKey("tile")) // new way
			tileData = nbt.getCompoundTag("tile");
		else { // Old way
			tileData = nbt.copy();
			tileData.removeTag("bBox");
			tileData.removeTag("size");
		}
	}
	
	public LittleTilePreview(LittleTileBox box, NBTTagCompound tileData) {
		this.box = box;
		this.tileData = tileData;
	}
	
	// ================Preview================
	
	public boolean canBeConvertedToBlockEntry() {
		return hasBlockIngredient();
	}
	
	public String getBlockName() {
		return tileData.getString("block");
	}
	
	public Block getBlock() {
		return Block.getBlockFromName(tileData.getString("block"));
	}
	
	public int getMeta() {
		return tileData.getInteger("meta");
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
	public RenderCubeObject getCubeBlock(LittleGridContext context) {
		RenderCubeObject cube = box.getRenderingCube(context, getBlock(), getMeta());
		cube.color = getColor();
		return cube;
	}
	
	public boolean isInvisible() {
		return tileData.getBoolean("invisible");
	}
	
	public void setInvisibile(boolean invisible) {
		tileData.setBoolean("invisible", invisible);
	}
	
	public NBTTagCompound getTileData() {
		return tileData;
	}
	
	public boolean hasBlockIngredient() {
		return !tileData.getBoolean("nodrop");
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
	
	public boolean canBeCombined(LittleTilePreview preview) {
		return tileData.equals(preview.getTileData());
	}
	
	@Override
	public LittleTileBox getBox() {
		return box;
	}
	
	@Override
	public void setBox(LittleTileBox box) {
		this.box = box;
	}
	
	@Override
	public boolean isChildOfStructure() {
		return false;
	}
	
	@Override
	public boolean canCombine(ICombinable combinable) {
		return canBeCombined((LittleTilePreview) combinable);
	}
	
	@Override
	public void combine(ICombinable combinable) {
		
	}
	
	@Override
	public void fillInSpace(LittleTileBox otherBox, boolean[][][] filled) {
		this.box.fillInSpace(otherBox, filled);
	}
	
	// ================Copy================
	
	@Override
	public LittleTilePreview copy() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		LittleTilePreview preview = loadPreviewFromNBT(nbt);
		
		if (preview == null) {
			// if(this.box != null)
			preview = new LittleTilePreview(box.copy(), tileData.copy()); // Maybe causes some crashes.
			// else
			// preview = new LittleTilePreview(size != null ? size.copy() : null,
			// tileData.copy());
		}
		preview.canSplit = this.canSplit;
		preview.fixedhandlers = new ArrayList<>(this.fixedhandlers);
		return preview;
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
	public LittleTile getLittleTile(TileEntityLittleTiles te) {
		return LittleTile.CreateandLoadTile(te, te.getWorld(), tileData);
	}
	
	public PlacePreviewTile getPlaceableTile(LittleTileBox overallBox, boolean fixed, LittleTileVec offset, LittlePreviews previews) {
		LittleTileBox newBox = this.box.copy();
		if (!fixed)
			newBox.add(offset);
		return new PlacePreviewTile(newBox, this, previews);
	}
	
	// ================Rotating/Flipping================
	
	public void flipPreview(Axis axis, LittleTileVec doubledCenter) {
		box.flipBox(axis, doubledCenter);
		getSpecialHandler().flipPreview(axis, this, doubledCenter);
	}
	
	public void rotatePreview(Rotation rotation, LittleTileVec doubledCenter) {
		box.rotateBox(rotation, doubledCenter);
		getSpecialHandler().rotatePreview(rotation, this, doubledCenter);
	}
	
	// ================Save & Loading================
	
	public static LittleTilePreview loadPreviewFromNBT(NBTTagCompound nbt) {
		if (nbt == null)
			return null;
		if (nbt.hasKey("type")) {
			Class<? extends LittleTilePreview> type = previewTypes.get(nbt.getString("type"));
			if (type != null) {
				LittleTilePreview preview = null;
				try {
					preview = type.getConstructor(NBTTagCompound.class).newInstance(nbt);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				return preview;
			}
		} else
			return new LittleTilePreview(nbt);
		return null;
	}
	
	public void writeToNBT(NBTTagCompound nbt) {
		box.writeToNBT("bBox", nbt);
		nbt.setTag("tile", tileData);
		if (isCustomPreview() && !getTypeID().equals(""))
			nbt.setString("type", getTypeID());
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
	
	public boolean canBeNBTGrouped(LittleTilePreview preview) {
		return this.box != null && preview.box != null && preview.canSplit == preview.canSplit && preview.getTileData().equals(this.getTileData());
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
	
	public void groupNBTTile(NBTTagCompound nbt, LittleTilePreview preview) {
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
	
	public static LittleTileSize getSize(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("size"))
			return new LittleTileSize("size", stack.getTagCompound());
		return new LittleTileSize(1, 1, 1);
	}
	
	public static LittleTileVec getOffset(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("min"))
			return new LittleTileVec("min", stack.getTagCompound());
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
		if (previews.hasStructure())
			nbt.setTag("structure", previews.getStructureData());
		
		NBTTagList list = LittleNBTCompressionTools.writePreviews(previews);
		nbt.setTag("tiles", list);
		nbt.setInteger("count", previews.size());
		
		if (previews.hasChildren()) {
			NBTTagList children = new NBTTagList();
			for (LittlePreviews child : previews.getChildren()) {
				children.appendTag(saveChildPreviews(child));
			}
			
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
			stack.getTagCompound().setTag("structure", previews.getStructureData());
		
		previews.context.set(stack.getTagCompound());
		
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		
		for (LittleTilePreview preview : previews.allPreviews()) {
			minX = Math.min(minX, preview.box.minX);
			minY = Math.min(minY, preview.box.minY);
			minZ = Math.min(minZ, preview.box.minZ);
			maxX = Math.max(maxX, preview.box.maxX);
			maxY = Math.max(maxY, preview.box.maxY);
			maxZ = Math.max(maxZ, preview.box.maxZ);
		}
		
		new LittleTileSize(maxX - minX, maxY - minY, maxZ - minZ).writeToNBT("size", stack.getTagCompound());
		new LittleTileVec(minX, minY, minZ).writeToNBT("min", stack.getTagCompound());
		
		if (previews.totalSize() >= lowResolutionMode) {
			NBTTagList list = new NBTTagList();
			// BlockPos lastPos = null;
			
			HashSet<BlockPos> positions = new HashSet<>();
			
			for (int i = 0; i < previews.size(); i++) { // Will not be sorted after rotating
				BlockPos pos = previews.get(i).box.getMinVec().getBlockPos(previews.context);
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
		previews.addTiles(te);
		savePreview(previews, stack);
	}
	
	public static void saveTiles(World world, LittleGridContext context, List<LittleTile> tiles, ItemStack stack) {
		stack.setTagCompound(new NBTTagCompound());
		
		LittlePreviews previews = new LittlePreviews(context);
		previews.addTiles(tiles);
		savePreview(previews, stack);
	}
	
	@SideOnly(Side.CLIENT)
	public static ArrayList<RenderCubeObject> getCubes(LittlePreviews previews) {
		ArrayList<RenderCubeObject> cubes = new ArrayList<RenderCubeObject>();
		for (LittleTilePreview preview : previews.allPreviews()) {
			cubes.add(preview.getCubeBlock(previews.context));
		}
		return cubes;
	}
	
	@SideOnly(Side.CLIENT)
	public static ArrayList<RenderCubeObject> getCubes(ItemStack stack) {
		return getCubes(stack, true);
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
	public static ArrayList<RenderCubeObject> getCubes(ItemStack stack, boolean allowLowResolution) {
		ArrayList<RenderCubeObject> cubes = new ArrayList<RenderCubeObject>();
		if (stack.hasTagCompound() && getTotalCount(stack.getTagCompound()) >= lowResolutionMode && allowLowResolution) {
			NBTTagList list = stack.getTagCompound().getTagList("pos", 11);
			for (int i = 0; i < list.tagCount(); i++) {
				int[] array = list.getIntArrayAt(i);
				cubes.add(new RenderCubeObject(array[0], array[1], array[2], array[0] + 1, array[1] + 1, array[2] + 1, LittleTiles.coloredBlock));
			}
		} else {
			LittlePreviews previews = getPreview(stack);
			for (LittleTilePreview preview : previews.allPreviews()) {
				cubes.add(preview.getCubeBlock(previews.context));
			}
		}
		return cubes;
	}
}
