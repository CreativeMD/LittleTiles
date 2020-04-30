package com.creativemd.littletiles.common.tile.preview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.combine.AdvancedCombiner;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleVolumes;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.compression.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.util.grid.IGridBased;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public class LittlePreviews implements Iterable<LittlePreview>, IGridBased {
	
	protected LittleGridContext context;
	protected final List<LittlePreviews> children;
	protected final List<LittlePreview> previews;
	
	public final NBTTagCompound structure;
	
	public LittlePreviews(LittleGridContext context) {
		this(null, context);
	}
	
	public LittlePreviews(NBTTagCompound nbt, LittleGridContext context) {
		this.context = context;
		this.previews = new ArrayList<>();
		this.structure = nbt;
		this.children = new ArrayList<>();
	}
	
	public LittlePreviews(LittlePreviews previews) {
		this.context = previews.getContext();
		this.previews = new ArrayList<>(previews.previews);
		this.children = new ArrayList<>(previews.children);
		this.structure = null;
	}
	
	public LittlePreviews(NBTTagCompound nbt, LittlePreviews previews) {
		this.context = previews.getContext();
		this.previews = new ArrayList<>(previews.previews);
		this.children = new ArrayList<>(previews.children);
		this.structure = nbt;
	}
	
	public boolean isAbsolute() {
		return false;
	}
	
	public BlockPos getBlockPos() {
		return null;
	}
	
	public boolean hasStructure() {
		return structure != null && structure.getSize() > 0;
	}
	
	public boolean hasStructureIncludeChildren() {
		if (hasStructure())
			return true;
		
		for (LittlePreviews child : children)
			if (child.hasStructureIncludeChildren())
				return true;
			
		return false;
	}
	
	public LittleStructure createStructure() {
		return LittleStructure.createAndLoadStructure(this.structure, null);
	}
	
	public String getStructureName() {
		if (!hasStructure())
			return null;
		
		return structure.hasKey("name") ? structure.getString("name") : null;
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
	
	public boolean containsIngredients() {
		if (hasStructure())
			return !LittleStructureAttribute.premade(getStructureType().attribute);
		return true;
	}
	
	public List<PlacePreview> getPlacePreviews(LittleVec offset) {
		List<PlacePreview> placePreviews = new ArrayList<>();
		for (LittlePreview preview : this)
			placePreviews.add(preview.getPlaceableTile(offset));
		
		getStructureTiles(placePreviews, offset);
		
		return placePreviews;
	}
	
	protected void getStructureTiles(List<PlacePreview> placePreviews, LittleVec offset) {
		if (hasStructure()) {
			for (PlacePreview placePreviewTile : getStructureType().getSpecialTiles(this)) {
				if (offset != null)
					placePreviewTile.add(offset);
				placePreviews.add(placePreviewTile);
			}
		}
	}
	
	public List<PlacePreview> getPlacePreviewsIncludingChildren(LittleVec offset) {
		List<PlacePreview> placePreviews = new ArrayList<>();
		for (LittlePreview preview : this)
			placePreviews.add(preview.getPlaceableTile(offset));
		
		getStructureTiles(placePreviews, offset);
		
		if (hasChildren())
			for (LittlePreviews child : children)
				placePreviews.addAll(child.getPlacePreviews(offset));
			
		return placePreviews;
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public List<LittlePreviews> getChildren() {
		return children;
	}
	
	public void updateChild(int index, LittlePreviews child) {
		children.set(index, child);
	}
	
	public void addChild(LittlePreviews child) {
		if (child.isAbsolute())
			throw new RuntimeException("Absolute previews cannot be added as a child!");
		children.add(child);
		if (context.size < child.getSmallestContext().size)
			convertToSmallest();
		else if (child.context != context)
			child.convertTo(context);
	}
	
	@Override
	public LittleGridContext getContext() {
		return context;
	}
	
	@Override
	public void convertToSmallest() {
		LittleGridContext context = getSmallestContext();
		
		if (context != this.context)
			convertTo(context);
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
	
	protected LittleGridContext getSmallestContext() {
		int size = LittleGridContext.minSize;
		for (LittlePreview preview : previews)
			size = Math.max(size, preview.getSmallestContext(context));
		
		LittleGridContext context = LittleGridContext.get(size);
		if (hasStructure())
			context = LittleGridContext.max(this.context, getStructureType().getMinContext(this));
		
		if (hasChildren())
			for (LittlePreviews child : children)
				context = LittleGridContext.max(child.getSmallestContext(), context);
		return context;
	}
	
	@Override
	public void convertTo(LittleGridContext to) {
		if (context != to)
			for (LittlePreview preview : previews)
				preview.convertTo(this.context, to);
			
		if (hasChildren())
			for (LittlePreviews child : children)
				child.convertTo(to);
		this.context = to;
	}
	
	public LittlePreviews copy() {
		LittlePreviews previews = new LittlePreviews(structure == null ? null : structure.copy(), context);
		for (LittlePreview preview : this.previews)
			previews.previews.add(preview.copy());
		
		for (LittlePreviews child : this.children)
			previews.children.add(child.copy());
		return previews;
	}
	
	protected LittlePreview getPreview(LittleTile tile) {
		LittlePreview preview = tile.getPreviewTile();
		LittleGridContext context = tile.getContext();
		if (this.context != context)
			if (this.context.size > context.size)
				preview.convertTo(context, this.context);
			else
				convertTo(context);
			
		return preview;
	}
	
	public LittlePreview addPreview(BlockPos pos, LittlePreview preview, LittleGridContext context) {
		if (this.context != context)
			if (this.context.size > context.size)
				preview.convertTo(context, this.context);
			else
				convertTo(context);
			
		previews.add(preview);
		return preview;
	}
	
	public LittlePreview addTile(LittleTile tile) {
		LittlePreview preview = getPreview(tile);
		previews.add(preview);
		return preview;
		
	}
	
	public LittlePreview addTile(LittleTile tile, LittleVec offset) {
		LittlePreview preview = getPreview(tile);
		preview.box.add(offset);
		return addPreview(null, tile.getPreviewTile(), tile.getContext());
	}
	
	public void addTiles(List<LittleTile> tiles) {
		if (tiles.isEmpty())
			return;
		
		for (LittleTile tile : tiles)
			addTile(tile);
		
	}
	
	public void addTiles(TileEntityLittleTiles te) {
		if (te.isEmpty())
			return;
		
		for (LittleTile tile : te)
			addTile(tile);
		
	}
	
	@Override
	public Iterator<LittlePreview> iterator() {
		return previews.iterator();
	}
	
	protected Iterator<LittlePreview> allPreviewsIterator() {
		if (hasChildren())
			return new Iterator<LittlePreview>() {
				
				public int i = -1;
				public Iterator<LittlePreview> subIterator = iterator();
				public List<? extends LittlePreviews> children = getChildren();
				
				@Override
				public boolean hasNext() {
					if (subIterator.hasNext())
						return true;
					
					while (i < children.size() - 1 && !subIterator.hasNext()) {
						i++;
						subIterator = children.get(i).allPreviewsIterator();
					}
					
					return subIterator.hasNext();
				}
				
				@Override
				public LittlePreview next() {
					return subIterator.next();
				}
				
				@Override
				public void remove() {
					subIterator.remove();
				}
			};
		return iterator();
	}
	
	public Iterable<LittlePreview> allPreviews() {
		return new Iterable<LittlePreview>() {
			
			@Override
			public Iterator<LittlePreview> iterator() {
				return allPreviewsIterator();
			}
		};
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
				previews.addChild(getChild(context, child));
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
				LittleTile tile = new LittleTile(LittleTiles.coloredBlock, 0);
				tile.saveTileExtra(tileData);
				
				NBTTagList list = stack.getTagCompound().getTagList("pos", 11);
				for (int i = 0; i < list.tagCount(); i++) {
					int[] array = list.getIntArrayAt(i);
					previews.previews.add(new LittlePreview(new LittleBox(array[0] * context.size, array[1] * context.size, array[2] * context.size, array[0] * context.size + context.maxPos, array[1] * context.size + context.maxPos, array[02] * context.size + context.maxPos), tileData));
				}
				
				if (stack.getTagCompound().hasKey("children")) {
					list = stack.getTagCompound().getTagList("children", 10);
					for (int i = 0; i < list.tagCount(); i++) {
						NBTTagCompound child = list.getCompoundTagAt(i);
						previews.addChild(getChild(context, child));
					}
				}
				
				return previews;
			}
			LittlePreviews previews = stack.getTagCompound().hasKey("structure") ? new LittlePreviews(stack.getTagCompound().getCompoundTag("structure"), context) : new LittlePreviews(context);
			previews = LittleNBTCompressionTools.readPreviews(previews, stack.getTagCompound().getTagList("tiles", 10));
			
			if (stack.getTagCompound().hasKey("children")) {
				NBTTagList list = stack.getTagCompound().getTagList("children", 10);
				for (int i = 0; i < list.tagCount(); i++) {
					NBTTagCompound child = list.getCompoundTagAt(i);
					previews.addChild(getChild(context, child));
				}
			}
			
			return previews;
		}
	}
	
	public LittlePreview get(int index) {
		return previews.get(index);
	}
	
	public int size() {
		return previews.size();
	}
	
	public int totalSize() {
		if (!hasChildren())
			return size();
		int size = size();
		for (LittlePreviews child : children)
			size += child.totalSize();
		return size;
	}
	
	public boolean isEmptyIncludeChildren() {
		if (!isEmpty())
			return false;
		
		for (LittlePreviews child : children)
			if (!child.isEmptyIncludeChildren())
				return false;
		return true;
	}
	
	public boolean isEmpty() {
		return previews.isEmpty();
	}
	
	public void addWithoutCheckingPreview(LittlePreview preview) {
		previews.add(preview);
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
		AdvancedCombiner<LittlePreview> combiner = new AdvancedCombiner(new ArrayList<>());
		for (Iterator<ArrayList<LittlePreview>> iterator = chunked.values().iterator(); iterator.hasNext();) {
			ArrayList<LittlePreview> list = iterator.next();
			combiner.setCombinables(list);
			combiner.combine();
			previews.addAll(list);
		}
		
		if (hasChildren())
			for (LittlePreviews child : children)
				child.combinePreviewBlocks();
	}
	
	protected void advancedScale(int from, int to) {
		for (LittlePreview preview : previews) {
			preview.convertTo(from, to);
		}
		
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
		previews.context = context;
		if (previews.hasChildren())
			for (LittlePreviews child : previews.getChildren())
				setLittlePreviewsContextSecretly(child, context);
	}
}
