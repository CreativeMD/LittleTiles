package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.connection.IStructureChildConnector;
import com.creativemd.littletiles.common.structure.connection.StructureLink;
import com.creativemd.littletiles.common.structure.connection.StructureLinkFromSubWorld;
import com.creativemd.littletiles.common.structure.connection.StructureLinkTile;
import com.creativemd.littletiles.common.structure.connection.StructureLinkToSubWorld;
import com.creativemd.littletiles.common.structure.connection.StructureMainTile;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.StructureTypeRelative;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTile.LittleTilePosition;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviewsStructure;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviewsStructure;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierRelative;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierStructureAbsolute;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierStructureRelative;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVecContext;
import com.creativemd.littletiles.common.tiles.vec.RelativeBlockPos;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.ingredients.Ingredients;
import com.creativemd.littletiles.common.utils.vec.SurroundingBox;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public abstract class LittleStructure {
	
	public static LittleStructure createAndLoadStructure(NBTTagCompound nbt, @Nullable LittleTile mainTile) {
		if (nbt == null)
			return null;
		
		String id = nbt.getString("id");
		LittleStructureType type = LittleStructureRegistry.getStructureType(id);
		if (type != null) {
			LittleStructure structure = type.createStructure();
			structure.mainTile = mainTile;
			structure.loadFromNBT(nbt);
			
			return structure;
			
		} else
			System.out.println("Could not find structureID=" + id);
		return null;
	}
	
	public final LittleStructureAttribute attribute;
	public final LittleStructureType type;
	
	public String name;
	
	public IStructureChildConnector parent;
	public LinkedHashMap<Integer, IStructureChildConnector> children;
	public List<LittleStructure> tempChildren;
	
	public LittleTilePos lastMainTileVec = null;
	
	public LittleStructure(LittleStructureType type) {
		this.attribute = type.attribute;
		this.type = type;
	}
	
	/** takes name of stack and connects the structure to its children (does so recursively)
	 * 
	 * @param stack
	 */
	public void placedStructure(@Nullable ItemStack stack) {
		NBTTagCompound nbt;
		if (name == null && stack != null && (nbt = stack.getSubCompound("display")) != null && nbt.hasKey("Name", 8))
			name = nbt.getString("Name");
		
		combineTiles();
		
		if (tempChildren != null) {
			children = new LinkedHashMap<>();
			for (int i = 0; i < tempChildren.size(); i++) {
				LittleStructure child = tempChildren.get(i);
				child.updateParentConnection(i, this);
				this.updateChildConnection(i, child);
				child.placedStructure(null);
			}
			tempChildren = null;
		}
	}
	
	/** This will notify every client that the structure has changed */
	public void updateStructure() {
		mainTile.te.updateBlock();
	}
	
	public void setMainTile(LittleTile tile) {
		this.mainTile = tile;
		
		if (parent != null) {
			LittleStructure parentStructure = parent.getStructure(getWorld());
			parentStructure.updateChildConnection(parent.getChildID(), this);
			this.updateParentConnection(parent.getChildID(), parentStructure);
		}
		
		for (IStructureChildConnector child : children.values()) {
			LittleStructure childStructure = child.getStructure(getWorld());
			childStructure.updateParentConnection(child.getChildID(), this);
			this.updateChildConnection(child.getChildID(), childStructure);
		}
		
		this.mainTile.connection = new StructureMainTile(mainTile, this);
		updateStructure();
		
		if (tiles == null) {
			tiles = new HashMapList<>();
			tiles.add(mainTile.te, mainTile);
		} else if (!containsTile(tile))
			addTile(tile);
		
		for (Iterator<Entry<TileEntityLittleTiles, ArrayList<LittleTile>>> iterator = tiles.entrySet().iterator(); iterator.hasNext();) {
			Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry = iterator.next();
			entry.getKey().getWorld().markChunkDirty(entry.getKey().getPos(), entry.getKey());
			
			for (Iterator iterator2 = entry.getValue().iterator(); iterator2.hasNext();) {
				LittleTile stTile = (LittleTile) iterator2.next();
				
				if (stTile != mainTile) {
					stTile.connection = getStructureLink(stTile);
					stTile.connection.setLoadedStructure(this, attribute);
				}
			}
		}
		
		LittleTilePos absolute = tile.getAbsolutePos();
		if (lastMainTileVec != null) {
			LittleTileVecContext vec = lastMainTileVec.getRelative(absolute);
			if (!lastMainTileVec.equals(absolute)) {
				for (StructureTypeRelative relative : type.relatives) {
					StructureRelative relativeST = (StructureRelative) relative.getRelative(this);
					if (relativeST != null)
						relativeST.onMove(this, vec.context, vec.vec);
				}
			}
		}
		lastMainTileVec = absolute;
		
	}
	
	public LittleTileIdentifierStructureRelative getMainTileCoord(BlockPos pos) {
		return new LittleTileIdentifierStructureRelative(pos, mainTile.te.getPos(), mainTile.getContext(), mainTile.getIdentifier(), attribute);
	}
	
	public StructureLinkTile getStructureLink(LittleTile tile) {
		return new StructureLinkTile(tile.te, mainTile.te.getPos(), mainTile.getContext(), mainTile.getIdentifier(), attribute, tile);
	}
	
	public boolean hasMainTile() {
		return mainTile != null;
	}
	
	public boolean isPlaced() {
		return hasMainTile();
	}
	
	public void combineTiles() {
		if (!hasLoaded())
			return;
		
		BlockPos pos = null;
		
		for (Iterator<TileEntityLittleTiles> iterator = tiles.keySet().iterator(); iterator.hasNext();) {
			iterator.next().combineTiles(this);
		}
	}
	
	public boolean selectMainTile() {
		if (hasLoaded()) {
			LittleTile first = tiles.getFirst();
			if (first != null) {
				setMainTile(first);
				return true;
			}
		}
		return false;
	}
	
	private LittleTile mainTile;
	
	/** The core of the structure. Handles saving & loading of the structures. All
	 * Tiles inside the structure are containing relative coordinates to this tile **/
	public LittleTile getMainTile() {
		return mainTile;
	}
	
	protected HashMapList<TileEntityLittleTiles, LittleTile> tiles = null;
	
	public void setTiles(HashMapList<TileEntityLittleTiles, LittleTile> tiles) {
		this.tiles = tiles;
	}
	
	public boolean LoadList() {
		if (tiles == null)
			return loadTiles();
		return true;
	}
	
	public boolean containsTile(LittleTile tile) {
		return tiles.contains(tile.te, tile);
	}
	
	public Set<TileEntityLittleTiles> blocks() {
		if (tiles == null)
			if (!loadTiles())
				return Collections.EMPTY_SET;
		return tiles.keySet();
	}
	
	public HashMapList<TileEntityLittleTiles, LittleTile> copyOfTiles() {
		if (tiles == null)
			if (!loadTiles())
				return new HashMapList<>();
		return new HashMapList<>(tiles);
	}
	
	public Set<Entry<TileEntityLittleTiles, ArrayList<LittleTile>>> getEntrySet() {
		return tiles.entrySet();
	}
	
	public Iterator<LittleTile> getTiles() {
		if (tiles == null)
			if (!loadTiles())
				return new Iterator<LittleTile>() {
					
					@Override
					public boolean hasNext() {
						return false;
					}
					
					@Override
					public LittleTile next() {
						return null;
					}
					
				};
			
		return tiles.iterator();
	}
	
	public Set<Entry<TileEntityLittleTiles, ArrayList<LittleTile>>> entrySet() {
		if (tiles == null)
			if (!loadTiles())
				return Collections.EMPTY_SET;
		return tiles.entrySet();
	}
	
	public HashMapList<TileEntityLittleTiles, LittleTile> getAllTiles(HashMapList<TileEntityLittleTiles, LittleTile> tiles) {
		if (!hasLoaded() || !loadChildren())
			return tiles;
		
		for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : this.tiles.entrySet()) {
			tiles.add(entry.getKey(), entry.getValue());
		}
		for (IStructureChildConnector child : children.values()) {
			child.getStructure(getWorld()).getAllTiles(tiles);
		}
		return tiles;
	}
	
	public void removeTile(LittleTile tile) {
		if (tiles != null)
			tiles.removeValue(tile.te, tile);
	}
	
	public void addTile(LittleTile tile) {
		tiles.add(tile.te, tile);
	}
	
	public boolean hasLoaded() {
		loadTiles();
		return mainTile != null && tiles != null && (tilesToLoad == null || tilesToLoad.size() == 0) && !isRelationToParentBroken() && !isRelationToChildrenBroken();
	}
	
	public boolean isRelationToParentBroken() {
		return parent != null && !parent.isConnected(getWorld());
	}
	
	public boolean isRelationToChildrenBroken() {
		for (IStructureChildConnector child : children.values()) {
			if (!child.isConnected(getWorld()))
				return true;
		}
		return false;
	}
	
	public void updateChildConnection(int i, LittleStructure child) {
		World world = getWorld();
		World childWorld = child.getWorld();
		
		IStructureChildConnector<LittleStructure> connector;
		if (childWorld == world)
			connector = new StructureLink(this.mainTile.te, child.getMainTile().te.getPos(), child.getMainTile().getContext(), child.getMainTile().getIdentifier(), child.attribute, this, i, false);
		else if (childWorld instanceof SubWorld && ((SubWorld) childWorld).parent != null)
			connector = new StructureLinkToSubWorld(child.getMainTile(), child.attribute, this, i, ((SubWorld) childWorld).parent.getUniqueID().toString());
		else
			throw new RuntimeException("Invalid connection between to structures!");
		
		connector.setLoadedStructure(child, child.attribute);
		children.put(i, connector);
	}
	
	public void updateParentConnection(int i, LittleStructure parent) {
		World world = getWorld();
		World parentWorld = parent.getWorld();
		
		IStructureChildConnector<LittleStructure> connector;
		if (parentWorld == world)
			connector = new StructureLink(this.mainTile.te, parent.getMainTile().te.getPos(), parent.getMainTile().getContext(), parent.getMainTile().getIdentifier(), parent.attribute, this, i, true);
		else if (world instanceof SubWorld && ((SubWorld) world).parent != null)
			connector = new StructureLinkFromSubWorld(parent.getMainTile(), parent.attribute, this, i);
		else
			throw new RuntimeException("Invalid connection between to structures!");
		
		connector.setLoadedStructure(parent, parent.attribute);
		this.parent = connector;
	}
	
	public boolean loadTiles() {
		if (mainTile != null) {
			if (tiles == null) {
				tiles = new HashMapList<>();
				addTile(mainTile);
			}
			
			if (tilesToLoad == null)
				return true;
			
			for (Iterator<Entry<BlockPos, Integer>> iterator = tilesToLoad.entrySet().iterator(); iterator.hasNext();) {
				Entry<BlockPos, Integer> entry = iterator.next();
				if (checkForTiles(mainTile.te.getWorld(), entry.getKey(), entry.getValue()))
					iterator.remove();
			}
			
			if (!tiles.contains(mainTile))
				addTile(mainTile);
			
			if (tilesToLoad.size() == 0)
				tilesToLoad = null;
			return true;
		}
		return false;
	}
	
	public boolean loadParent() {
		if (parent != null)
			return parent.isConnected(getWorld());
		return true;
	}
	
	public boolean loadChildren() {
		if (children == null)
			children = new LinkedHashMap<>();
		
		if (children.isEmpty())
			return true;
		
		for (IStructureChildConnector child : children.values()) {
			if (!child.isConnected(mainTile.te.getWorld()) || !child.getStructureWithoutLoading().hasLoaded() || !child.getStructureWithoutLoading().loadChildren())
				return false;
		}
		
		return true;
	}
	
	public boolean isChildMoving() {
		for (IStructureChildConnector child : children.values()) {
			if (child.isLinkToAnotherWorld())
				return true;
			if (child.getStructure(getWorld()).isChildMoving())
				return true;
		}
		return false;
	}
	
	public HashMap<BlockPos, Integer> tilesToLoad = null;
	
	public void loadStructure(LittleTile mainTile) {
		this.mainTile = mainTile;
		this.mainTile.connection = new StructureMainTile(mainTile, this);
		
		if (tiles != null && !containsTile(mainTile))
			addTile(mainTile);
	}
	
	public void loadFromNBT(NBTTagCompound nbt) {
		if (tiles != null)
			tiles = null;
		
		tilesToLoad = new HashMap<>();
		
		// LoadTiles
		if (nbt.hasKey("count")) // Old way
		{
			int count = nbt.getInteger("count");
			for (int i = 0; i < count; i++) {
				LittleTileIdentifierRelative coord = null;
				if (nbt.hasKey("i" + i + "coX")) {
					LittleTilePosition pos = new LittleTilePosition("i" + i, nbt);
					coord = new LittleTileIdentifierRelative(mainTile.te, pos.coord, LittleGridContext.get(), new int[] {
					        pos.position.x, pos.position.y, pos.position.z });
				} else {
					coord = LittleTileIdentifierRelative.loadIdentifierOld("i" + i, nbt);
				}
				
				BlockPos pos = coord.getAbsolutePosition(mainTile.te);
				Integer insideBlock = tilesToLoad.get(pos);
				if (insideBlock == null)
					insideBlock = new Integer(1);
				else
					insideBlock = insideBlock + 1;
				tilesToLoad.put(pos, insideBlock);
			}
			
		} else if (nbt.hasKey("tiles")) { // new way
			NBTTagList list = nbt.getTagList("tiles", 11);
			for (int i = 0; i < list.tagCount(); i++) {
				int[] array = list.getIntArrayAt(i);
				if (array.length == 4) {
					RelativeBlockPos pos = new RelativeBlockPos(array);
					tilesToLoad.put(pos.getAbsolutePos(mainTile.te), array[3]);
				} else
					System.out.println("Found invalid array! " + nbt);
			}
		}
		
		if (nbt.hasKey("name"))
			name = nbt.getString("name");
		else
			name = null;
		
		// Load family (parent and children)		
		if (nbt.hasKey("parent"))
			parent = StructureLink.loadFromNBT(this, nbt.getCompoundTag("parent"), true);
		else
			parent = null;
		
		if (nbt.hasKey("children")) {
			children = new LinkedHashMap<>();
			NBTTagList list = nbt.getTagList("children", 10);
			for (int i = 0; i < list.tagCount(); i++) {
				IStructureChildConnector child = StructureLink.loadFromNBT(this, list.getCompoundTagAt(i), false);
				children.put(child.getChildID(), child);
			}
		} else
			children = new LinkedHashMap<>();
		
		for (StructureTypeRelative relative : type.relatives) {
			if (nbt.hasKey(relative.saveKey))
				relative.createAndSetRelative(this, nbt);
			else
				failedLoadingRelative(nbt, relative);
		}
		
		loadFromNBTExtra(nbt);
	}
	
	protected void failedLoadingRelative(NBTTagCompound nbt, StructureTypeRelative relative) {
		relative.setRelative(this, null);
	}
	
	protected abstract void loadFromNBTExtra(NBTTagCompound nbt);
	
	public NBTTagCompound writeToNBTPreview(NBTTagCompound nbt, BlockPos newCenter) {
		nbt.setString("id", type.id);
		if (name != null)
			nbt.setString("name", name);
		else
			nbt.removeTag("name");
		
		LittleTileVecContext vec = getMainTile().getAbsolutePos().getRelative(new LittleTilePos(newCenter, getMainTile().getContext()));
		
		LittleTileVec inverted = vec.vec.copy();
		inverted.invert();
		
		for (StructureTypeRelative relative : type.relatives) {
			StructureRelative relativeST = (StructureRelative) relative.getRelative(this);
			if (relativeST == null)
				continue;
			relativeST.onMove(this, vec.context, vec.vec);
			relativeST.writeToNBT(relative.saveKey, nbt);
			relativeST.onMove(this, vec.context, inverted);
		}
		
		writeToNBTExtra(nbt);
		return nbt;
	}
	
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setString("id", type.id);
		if (name != null)
			nbt.setString("name", name);
		else
			nbt.removeTag("name");
		
		// Save family (parent and children)
		if (parent != null)
			nbt.setTag("parent", parent.writeToNBT(new NBTTagCompound()));
		
		if (children != null && !children.isEmpty()) {
			NBTTagList list = new NBTTagList();
			for (IStructureChildConnector child : children.values()) {
				list.appendTag(child.writeToNBT(new NBTTagCompound()));
			}
			nbt.setTag("children", list);
		}
		
		// SaveTiles
		HashMap<BlockPos, Integer> positions = new HashMap<>();
		if (tiles != null) {
			for (Iterator<Entry<TileEntityLittleTiles, ArrayList<LittleTile>>> iterator = tiles.entrySet().iterator(); iterator.hasNext();) {
				Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry = iterator.next();
				if (entry.getValue().size() > 0)
					positions.put(entry.getKey().getPos(), entry.getValue().size());
			}
		}
		
		if (tilesToLoad != null)
			positions.putAll(tilesToLoad);
		
		if (positions.size() > 0) {
			NBTTagList list = new NBTTagList();
			for (Iterator<Entry<BlockPos, Integer>> iterator = positions.entrySet().iterator(); iterator.hasNext();) {
				Entry<BlockPos, Integer> entry = iterator.next();
				RelativeBlockPos pos = new RelativeBlockPos(mainTile.te, entry.getKey());
				list.appendTag(new NBTTagIntArray(new int[] { pos.getRelativePos().getX(), pos.getRelativePos().getY(),
				        pos.getRelativePos().getZ(), entry.getValue() }));
			}
			nbt.setTag("tiles", list);
		}
		
		for (StructureTypeRelative relative : type.relatives) {
			StructureRelative relativeST = (StructureRelative) relative.getRelative(this);
			if (relativeST == null)
				continue;
			relativeST.writeToNBT(relative.saveKey, nbt);
		}
		
		writeToNBTExtra(nbt);
	}
	
	protected abstract void writeToNBTExtra(NBTTagCompound nbt);
	
	public boolean doesLinkToMainTile(LittleTile tile) {
		try {
			return tile == getMainTile() || (tile.connection.isLink() && tile.connection.getStructurePosition().equals(mainTile.te.getPos()) && tile.connection.is(mainTile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean checkForTiles(World world, BlockPos pos, Integer expectedCount) {
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		if (WorldUtils.checkIfChunkExists(chunk)) {
			// chunk.isChunkLoaded
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof TileEntityLittleTiles) {
				if (!((TileEntityLittleTiles) tileEntity).hasLoaded())
					return false;
				int found = 0;
				
				if (tiles.keySet().contains(tileEntity))
					tiles.removeKey((TileEntityLittleTiles) tileEntity);
				
				for (Iterator iterator = ((TileEntityLittleTiles) tileEntity).getTiles().iterator(); iterator.hasNext();) {
					LittleTile tile = (LittleTile) iterator.next();
					if (tile.isChildOfStructure() && (tile.connection.getStructureWithoutLoading() == this || doesLinkToMainTile(tile))) {
						tiles.add((TileEntityLittleTiles) tileEntity, tile);
						if (tile.connection.isLink())
							tile.connection.setLoadedStructure(this, attribute);
						found++;
					}
				}
				
				if (found == expectedCount)
					return true;
			}
		}
		return false;
	}
	
	public int countTiles() {
		int count = 0;
		if (tilesToLoad != null) {
			for (Integer tiles : tilesToLoad.values()) {
				count += tiles;
			}
		}
		if (tiles != null) {
			for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : tiles.entrySet()) {
				if (tilesToLoad == null || !tilesToLoad.containsKey(entry.getKey().getPos()))
					count += entry.getValue().size();
			}
		}
		return count;
	}
	
	// ====================LittleTile-Stuff====================
	
	public World getWorld() {
		return mainTile.te.getWorld();
	}
	
	public void onLittleTileDestroy() {
		if (parent != null) {
			if (parent.isConnected(getWorld()))
				parent.getStructure(getWorld()).onLittleTileDestroy();
			return;
		}
		
		if (hasLoaded() && loadChildren()) {
			for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : tiles.entrySet()) {
				entry.getKey().removeTiles(entry.getValue());
			}
			
			for (IStructureChildConnector child : children.values()) {
				child.destroyStructure();
			}
		}
	}
	
	public void addIngredients(Ingredients ingredients) {
		
	}
	
	public LittleGridContext getMinContext() {
		LittleGridContext context = LittleGridContext.getMin();
		for (IStructureChildConnector child : children.values()) {
			context = LittleGridContext.max(context, child.getStructure(getWorld()).getMinContext());
		}
		for (StructureTypeRelative relative : type.relatives) {
			StructureRelative relativeST = relative.getRelative(this);
			if (relativeST == null)
				continue;
			relativeST.convertToSmallest();
			context = LittleGridContext.max(context, relativeST.getContext());
		}
		return context;
	}
	
	public LittlePreviewsStructure getPreviews(BlockPos pos) {
		NBTTagCompound structureNBT = new NBTTagCompound();
		this.writeToNBTPreview(structureNBT, pos);
		LittlePreviewsStructure previews = new LittlePreviewsStructure(structureNBT, getMinContext());
		
		for (Iterator<LittleTile> iterator = getTiles(); iterator.hasNext();) {
			LittleTile tile = iterator.next();
			LittleTilePreview preview = previews.addTile(tile);
			preview.box.add(new LittleTileVec(previews.context, tile.te.getPos().subtract(pos)));
		}
		
		for (IStructureChildConnector child : children.values()) {
			previews.addChild(child.getStructure(getWorld()).getPreviews(pos));
		}
		
		previews.convertToSmallest();
		previews.ensureContext(getMinContext());
		return previews;
	}
	
	public LittleAbsolutePreviewsStructure getAbsolutePreviews(BlockPos pos) {
		NBTTagCompound structureNBT = new NBTTagCompound();
		this.writeToNBTPreview(structureNBT, pos);
		LittleAbsolutePreviewsStructure previews = new LittleAbsolutePreviewsStructure(structureNBT, pos, getMinContext());
		
		for (Iterator<LittleTile> iterator = getTiles(); iterator.hasNext();) {
			previews.addTile(iterator.next());
		}
		
		for (IStructureChildConnector child : children.values()) {
			previews.addChild(child.getStructure(getWorld()).getPreviews(pos));
		}
		
		previews.convertToSmallest();
		previews.ensureContext(getMinContext());
		return previews;
	}
	
	public MutableBlockPos getMinPos(MutableBlockPos pos) {
		for (TileEntityLittleTiles te : tiles.keySet()) {
			pos.setPos(Math.min(pos.getX(), te.getPos().getX()), Math.min(pos.getY(), te.getPos().getY()), Math.min(pos.getZ(), te.getPos().getZ()));
		}
		for (IStructureChildConnector child : children.values()) {
			child.getStructure(getWorld()).getMinPos(pos);
		}
		return pos;
	}
	
	public List<PlacePreviewTile> getSpecialTiles(LittlePreviews previews) {
		if (type.relatives.isEmpty())
			return Collections.EMPTY_LIST;
		List<PlacePreviewTile> placePreviews = new ArrayList<>();
		for (StructureTypeRelative relative : type.relatives) {
			StructureRelative relativeST = relative.getRelative(this);
			if (relativeST == null)
				continue;
			PlacePreviewTile tile = getPlacePreview(relativeST, relative, previews);
			if (relativeST.getContext().size < previews.context.size)
				tile.convertTo(relativeST.getContext(), previews.context);
			placePreviews.add(tile);
		}
		return placePreviews;
	}
	
	protected PlacePreviewTile getPlacePreview(StructureRelative relative, StructureTypeRelative type, LittlePreviews previews) {
		return relative.getPlacePreview(previews, type);
	}
	
	public ItemStack getStructureDrop() {
		if (parent != null) {
			if (parent.isConnected(getWorld()))
				return parent.getStructure(getWorld()).getStructureDrop();
			return ItemStack.EMPTY;
		}
		
		if (hasLoaded() && loadChildren()) {
			BlockPos pos = getMinPos(new MutableBlockPos(getMainTile().te.getPos()));
			
			ItemStack stack = new ItemStack(LittleTiles.multiTiles);
			LittlePreviews previews = getPreviews(pos);
			
			LittleTilePreview.savePreview(previews, stack);
			
			if (name != null) {
				NBTTagCompound display = new NBTTagCompound();
				display.setString("Name", name);
				stack.getTagCompound().setTag("display", display);
			}
			return stack;
		}
		return ItemStack.EMPTY;
	}
	
	public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
		return false;
	}
	
	// ====================SORTING====================
	
	public void onMove(LittleGridContext context, LittleTileVec offset) {
		for (StructureTypeRelative relative : type.relatives) {
			StructureRelative relativeST = (StructureRelative) relative.getRelative(this);
			if (relativeST == null)
				continue;
			relativeST.onMove(this, context, offset);
		}
	}
	
	public void onFlip(LittleGridContext context, Axis axis, LittleTileVec doubledCenter) {
		for (StructureTypeRelative relative : type.relatives) {
			StructureRelative relativeST = (StructureRelative) relative.getRelative(this);
			if (relativeST == null)
				continue;
			relativeST.onFlip(this, context, axis, doubledCenter);
		}
	}
	
	public void onRotate(LittleGridContext context, Rotation rotation, LittleTileVec doubledCenter) {
		for (StructureTypeRelative relative : type.relatives) {
			StructureRelative relativeST = (StructureRelative) relative.getRelative(this);
			if (relativeST == null)
				continue;
			relativeST.onRotate(this, context, rotation, doubledCenter);
		}
	}
	
	// ====================Helpers====================
	
	public AxisAlignedBB getSurroundingBox() {
		return new SurroundingBox(true).add(tiles.entrySet()).getSurroundingBox();
	}
	
	public Vec3d getHighestCenterVec() {
		return new SurroundingBox(true).add(tiles.entrySet()).getHighestCenterVec();
	}
	
	public LittleTilePos getHighestCenterPoint() {
		return new SurroundingBox(true).add(tiles.entrySet()).getHighestCenterPoint();
	}
	
	// ====================Extra====================
	
	public boolean shouldPlaceTile(LittleTile tile) {
		return true;
	}
	
	public boolean isBed(IBlockAccess world, BlockPos pos, EntityLivingBase player) {
		return false;
	}
	
	public boolean shouldCheckForCollision() {
		return false;
	}
	
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		
	}
	
	public void onUpdatePacketReceived() {
		
	}
	
	public void removeWorldProperties() {
		mainTile = null;
		tiles = new HashMapList<>();
		tilesToLoad = null;
	}
	
	public boolean canOnlyBePlacedByItemStack() {
		return false;
	}
	
	/** Only important for structures which require to be placed by the given
	 * itemstack
	 * 
	 * @return */
	public String getStructureDropIdentifier() {
		return null;
	}
	
	public LittleTileIdentifierStructureAbsolute getAbsoluteIdentifier() {
		return new LittleTileIdentifierStructureAbsolute(mainTile, attribute);
	}
	
	public boolean isChildOf(LittleStructure structure) {
		if (parent != null && parent.isConnected(getWorld()))
			return structure == parent.getStructureWithoutLoading() || parent.getStructureWithoutLoading().isChildOf(structure);
		return false;
	}
	
}
