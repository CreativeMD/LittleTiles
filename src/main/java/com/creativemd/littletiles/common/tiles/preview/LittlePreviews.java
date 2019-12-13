package com.creativemd.littletiles.common.tiles.preview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.combine.AdvancedCombiner;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.compression.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public class LittlePreviews implements Iterable<LittleTilePreview> {
	
	protected List<LittleTilePreview> previews;
	public LittleGridContext context;
	
	protected List<LittlePreviews> children = new ArrayList<>();
	
	public LittlePreviews(LittleGridContext context) {
		this.context = context;
		this.previews = new ArrayList<>();
	}
	
	protected LittlePreviews(LittlePreviews previews) {
		this.previews = new ArrayList<>(previews.previews);
		this.context = previews.context;
	}
	
	public void assign(LittlePreviews previews) {
		this.context = previews.context;
		this.previews = previews.previews;
		this.children = previews.children;
	}
	
	public boolean isAbsolute() {
		return false;
	}
	
	public BlockPos getBlockPos() {
		return null;
	}
	
	public boolean hasStructure() {
		return false;
	}
	
	public void deleteCachedStructure() {
		if (hasChildren())
			for (LittlePreviews child : getChildren())
				child.deleteCachedStructure();
	}
	
	public LittleStructure getStructure() {
		return null;
	}
	
	public NBTTagCompound getStructureData() {
		return null;
	}
	
	public String getStructureName() {
		if (!hasStructure())
			return null;
		
		NBTTagCompound nbt = getStructureData();
		return nbt.hasKey("name") ? nbt.getString("name") : null;
	}
	
	public String getStructureId() {
		if (!hasStructure())
			return null;
		
		NBTTagCompound nbt = getStructureData();
		return nbt.getString("id");
	}
	
	public LittleGridContext getMinContext() {
		LittleGridContext context = this.context;
		if (hasStructure())
			context = LittleGridContext.max(this.context, getStructure().getMinContext());
		if (hasChildren())
			for (LittlePreviews child : getChildren())
				context = LittleGridContext.max(context, child.getMinContext());
		return context;
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
		if (context.size < child.getSmallestContext())
			convertToSmallest();
		else if (child.context != context)
			child.convertTo(context);
	}
	
	public void getPlacePreviews(List<PlacePreviewTile> placePreviews, LittleTileBox overallBox, boolean fixed, LittleTileVec offset) {
		for (LittleTilePreview preview : this) {
			placePreviews.add(preview.getPlaceableTile(overallBox, fixed, offset, this));
		}
		
		if (hasStructure()) {
			for (PlacePreviewTile placePreviewTile : getStructure().getSpecialTiles(this)) {
				if (!fixed)
					placePreviewTile.add(offset);
				placePreviews.add(placePreviewTile);
			}
		}
		
		if (hasChildren()) {
			for (LittlePreviews child : getChildren()) {
				child.getPlacePreviews(placePreviews, overallBox, fixed, offset);
			}
		}
	}
	
	public void movePreviews(LittleGridContext context, LittleTileVec offset) {
		if (context.size > this.context.size)
			convertTo(context);
		else if (context.size < this.context.size)
			offset.convertTo(context, this.context);
		
		context = this.context;
		
		for (LittleTilePreview preview : previews) {
			preview.box.add(offset);
		}
		if (hasStructure()) {
			getStructure().onMove(context, offset);
		}
		
		if (hasChildren())
			for (LittlePreviews child : children) {
				child.movePreviews(context, offset);
			}
	}
	
	public void flipPreviews(Axis axis, LittleTileVec doubledCenter) {
		for (LittleTilePreview preview : previews) {
			preview.flipPreview(axis, doubledCenter);
		}
		if (hasStructure()) {
			getStructure().onFlip(context, axis, doubledCenter);
			getStructure().writeToNBT(getStructureData());
		}
		
		if (hasChildren())
			for (LittlePreviews child : children) {
				child.flipPreviews(axis, doubledCenter);
			}
	}
	
	public void rotatePreviews(Rotation rotation, LittleTileVec doubledCenter) {
		for (LittleTilePreview preview : previews) {
			preview.rotatePreview(rotation, doubledCenter);
		}
		if (hasStructure()) {
			getStructure().onRotate(context, rotation, doubledCenter);
			getStructure().writeToNBT(getStructureData());
		}
		
		if (hasChildren())
			for (LittlePreviews child : children) {
				child.rotatePreviews(rotation, doubledCenter);
			}
	}
	
	public void convertTo(LittleGridContext to) {
		if (context != to) {
			for (LittleTilePreview preview : previews) {
				preview.convertTo(this.context, to);
			}
		}
		if (hasChildren())
			for (LittlePreviews child : getChildren())
				child.convertTo(to);
		this.context = to;
	}
	
	protected int getSmallestContext() {
		int size = LittleGridContext.minSize;
		for (LittleTilePreview preview : previews)
			size = Math.max(size, preview.getSmallestContext(context));
		return size;
	}
	
	public void convertToSmallest() {
		int size = getSmallestContext();
		if (hasChildren())
			for (LittlePreviews child : getChildren())
				size = Math.max(child.getSmallestContext(), size);
			
		if (size != context.size)
			convertTo(LittleGridContext.get(size));
	}
	
	public LittlePreviews copy() {
		LittlePreviews previews = new LittlePreviews(context);
		previews.previews.addAll(this.previews);
		previews.children.addAll(children);
		return previews;
	}
	
	protected LittleTilePreview getPreview(LittleTile tile) {
		LittleTilePreview preview = tile.getPreviewTile();
		LittleGridContext context = tile.getContext();
		if (this.context != context) {
			if (this.context.size > context.size)
				preview.convertTo(context, this.context);
			else
				convertTo(context);
		}
		
		return preview;
	}
	
	public LittleTilePreview addPreview(BlockPos pos, LittleTilePreview preview, LittleGridContext context) {
		if (this.context != context) {
			if (this.context.size > context.size)
				preview.convertTo(context, this.context);
			else
				convertTo(context);
		}
		
		previews.add(preview);
		return preview;
	}
	
	public LittleTilePreview addTile(LittleTile tile) {
		LittleTilePreview preview = getPreview(tile);
		previews.add(preview);
		return preview;
		
	}
	
	public LittleTilePreview addTile(LittleTile tile, LittleTileVec offset) {
		LittleTilePreview preview = getPreview(tile);
		preview.box.add(offset);
		return addPreview(null, tile.getPreviewTile(), tile.getContext());
	}
	
	public void addTiles(List<LittleTile> tiles) {
		if (tiles.isEmpty())
			return;
		
		for (LittleTile tile : tiles) {
			addTile(tile);
		}
	}
	
	public void addTiles(TileEntityLittleTiles te) {
		if (te.isEmpty())
			return;
		
		for (LittleTile tile : te) {
			addTile(tile);
		}
	}
	
	@Override
	public Iterator<LittleTilePreview> iterator() {
		return previews.iterator();
	}
	
	public static LittlePreviews getChild(LittleGridContext context, NBTTagCompound nbt) {
		LittlePreviews previews;
		if (nbt.hasKey("structure"))
			previews = new LittlePreviewsStructure(nbt.getCompoundTag("structure"), context);
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
				LittleTilePreview preview = LittleTilePreview.loadPreviewFromNBT(nbt);
				if (preview != null)
					previews.previews.add(preview);
			}
			
			if (stack.getTagCompound().hasKey("structure"))
				return new LittlePreviewsStructure(stack.getTagCompound().getCompoundTag("structure"), previews);
			return previews;
		} else {
			if (allowLowResolution && stack.getTagCompound().hasKey("pos")) {
				LittlePreviews previews = new LittlePreviews(context);
				NBTTagCompound tileData = new NBTTagCompound();
				LittleTile tile = new LittleTileBlock(LittleTiles.coloredBlock);
				tile.saveTileExtra(tileData);
				tileData.setString("tID", tile.getID());
				
				NBTTagList list = stack.getTagCompound().getTagList("pos", 11);
				for (int i = 0; i < list.tagCount(); i++) {
					int[] array = list.getIntArrayAt(i);
					previews.previews.add(new LittleTilePreview(new LittleTileBox(array[0] * context.size, array[1] * context.size, array[2] * context.size, array[0] * context.size + context.maxPos, array[1] * context.size + context.maxPos, array[02] * context.size + context.maxPos), tileData));
				}
				return previews;
			}
			LittlePreviews previews = stack.getTagCompound().hasKey("structure") ? new LittlePreviewsStructure(stack.getTagCompound().getCompoundTag("structure"), context) : new LittlePreviews(context);
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
	
	public LittleTilePreview get(int index) {
		return previews.get(index);
	}
	
	protected Iterator<LittleTilePreview> allPreviewsIterator() {
		if (hasChildren())
			return new Iterator<LittleTilePreview>() {
				
				public int i = -1;
				public Iterator<LittleTilePreview> subIterator = previews.iterator();
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
				public LittleTilePreview next() {
					return subIterator.next();
				}
				
				@Override
				public void remove() {
					subIterator.remove();
				}
			};
		return previews.iterator();
	}
	
	public Iterable<LittleTilePreview> allPreviews() {
		return new Iterable<LittleTilePreview>() {
			
			@Override
			public Iterator<LittleTilePreview> iterator() {
				return allPreviewsIterator();
			}
		};
	}
	
	public int size() {
		return previews.size();
	}
	
	public int totalSize() {
		if (!hasChildren())
			return size();
		int size = size();
		for (LittlePreviews child : getChildren()) {
			size += child.totalSize();
		}
		return size;
	}
	
	public void ensureContext(LittleGridContext context) {
		if (this.context.size < context.size)
			convertTo(context);
	}
	
	public boolean isEmpty() {
		return previews.isEmpty();
	}
	
	public void addWithoutCheckingPreview(LittleTilePreview preview) {
		previews.add(preview);
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
		HashMapList<BlockPos, LittleTilePreview> chunked = new HashMapList<>();
		for (int i = 0; i < previews.size(); i++) {
			chunked.add(previews.get(i).box.getMinVec().getBlockPos(context), previews.get(i));
		}
		previews.clear();
		AdvancedCombiner<LittleTilePreview> combiner = new AdvancedCombiner(new ArrayList<>());
		for (Iterator<ArrayList<LittleTilePreview>> iterator = chunked.values().iterator(); iterator.hasNext();) {
			ArrayList<LittleTilePreview> list = iterator.next();
			combiner.setCombinables(list);
			combiner.combine();
			previews.addAll(list);
		}
		
		if (hasChildren())
			for (LittlePreviews child : getChildren())
				child.combinePreviewBlocks();
	}
	
	private void advancedScale(int from, int to) {
		for (LittleTilePreview preview : previews) {
			preview.convertTo(from, to);
		}
		
		if (hasChildren())
			for (LittlePreviews child : getChildren())
				child.advancedScale(from, to);
	}
	
	public LittleTileBox getSurroundingBox() {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		
		for (LittleTilePreview preview : allPreviews()) {
			minX = Math.min(minX, preview.box.minX);
			minY = Math.min(minY, preview.box.minY);
			minZ = Math.min(minZ, preview.box.minZ);
			maxX = Math.max(maxX, preview.box.maxX);
			maxY = Math.max(maxY, preview.box.maxY);
			maxZ = Math.max(maxZ, preview.box.maxZ);
		}
		
		return new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	public LittleTileVec getMinVec() {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		
		for (LittleTilePreview preview : allPreviews()) {
			minX = Math.min(minX, preview.box.minX);
			minY = Math.min(minY, preview.box.minY);
			minZ = Math.min(minZ, preview.box.minZ);
			maxX = Math.max(maxX, preview.box.maxX);
			maxY = Math.max(maxY, preview.box.maxY);
			maxZ = Math.max(maxZ, preview.box.maxZ);
		}
		
		return new LittleTileVec(minX, minY, minZ);
	}
	
	public LittleTileSize getSize() {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		
		for (LittleTilePreview preview : allPreviews()) {
			minX = Math.min(minX, preview.box.minX);
			minY = Math.min(minY, preview.box.minY);
			minZ = Math.min(minZ, preview.box.minZ);
			maxX = Math.max(maxX, preview.box.maxX);
			maxY = Math.max(maxY, preview.box.maxY);
			maxZ = Math.max(maxZ, preview.box.maxZ);
		}
		
		return new LittleTileSize(maxX - minX, maxY - minY, maxZ - minZ);
	}
	
	public static void advancedScale(LittlePreviews previews, int from, int to) {
		previews.advancedScale(from, to);
	}
}
