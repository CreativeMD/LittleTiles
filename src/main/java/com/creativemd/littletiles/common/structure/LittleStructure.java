package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.connection.IStructureChildConnector;
import com.creativemd.littletiles.common.structure.connection.StructureLink;
import com.creativemd.littletiles.common.structure.connection.StructureLinkFromSubWorld;
import com.creativemd.littletiles.common.structure.connection.StructureLinkTile;
import com.creativemd.littletiles.common.structure.connection.StructureLinkToSubWorld;
import com.creativemd.littletiles.common.structure.connection.StructureMainTile;
import com.creativemd.littletiles.common.structure.directional.StructureDirectionalField;
import com.creativemd.littletiles.common.structure.exception.MissingTileEntity;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTile.LittleTilePosition;
import com.creativemd.littletiles.common.tile.math.identifier.LittleIdentifierRelative;
import com.creativemd.littletiles.common.tile.math.identifier.LittleIdentifierStructureAbsolute;
import com.creativemd.littletiles.common.tile.math.identifier.LittleIdentifierStructureRelative;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.tile.math.vec.RelativeBlockPos;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviewsStructureHolder;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.vec.LittleTransformation;
import com.creativemd.littletiles.common.util.vec.SurroundingBox;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleStructure {
	
	private static final Iterator<LittleTile> EMPTY_ITERATOR = new Iterator<LittleTile>() {
		
		@Override
		public boolean hasNext() {
			return false;
		}
		
		@Override
		public LittleTile next() {
			return null;
		}
		
	};
	
	private static final HashMapList<BlockPos, LittleTile> EMPTY_HASHMAPLIST = new HashMapList<>();
	
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
	
	public final LittleStructureType type;
	public String name;
	
	/** The core of the structure. Handles saving & loading of the structures.
	 * All tiles inside the structure connect to it in relative block positions and absolute identifier **/
	private LittleTile mainTile;
	protected LittleAbsoluteVec lastMainTileVec = null;
	
	protected HashMapList<BlockPos, LittleTile> tiles = null;
	public LinkedHashMap<BlockPos, Integer> tilesToLoad = null;
	
	public IStructureChildConnector parent;
	public List<IStructureChildConnector> children;
	
	public LittleStructure(LittleStructureType type) {
		this.type = type;
	}
	
	// ================Basics================
	
	public World getWorld() {
		return mainTile.te.getWorld();
	}
	
	public int getAttribute() {
		return type.attribute;
	}
	
	// ================MainTile================
	
	/** The core of the structure. Handles saving & loading of the structures.
	 * All tiles inside the structure connect to it in relative block positions and absolute identifier **/
	public LittleTile getMainTile() {
		return mainTile;
	}
	
	public LittleIdentifierStructureAbsolute getAbsoluteIdentifier() {
		return new LittleIdentifierStructureAbsolute(mainTile, getAttribute());
	}
	
	public boolean selectMainTile() {
		if (load()) {
			LittleTile first = tiles.getFirst();
			if (first != null) {
				setMainTile(first);
				return true;
			}
		}
		return false;
	}
	
	public void setMainTile(LittleTile tile) {
		if (mainTile != null)
			checkLoaded();
		
		this.mainTile = tile;
		this.mainTile.connection = new StructureMainTile(mainTile, this);
		
		World world = getWorld();
		
		if (parent != null) {
			LittleStructure parentStructure = parent.getStructure(world);
			parentStructure.updateChildConnection(parent.getChildID(), this);
			this.updateParentConnection(parent.getChildID(), parentStructure);
		}
		
		for (IStructureChildConnector child : children) {
			LittleStructure childStructure = child.getStructure(world);
			childStructure.updateParentConnection(child.getChildID(), this);
			this.updateChildConnection(child.getChildID(), childStructure);
		}
		
		if (tiles == null) {
			tiles = new HashMapList<>();
			tiles.add(mainTile.getBlockPos(), mainTile);
		} else if (!contains(tile))
			add(tile);
		
		for (Entry<BlockPos, ArrayList<LittleTile>> entry : tiles.entrySet()) {
			try {
				TileEntityLittleTiles te = loadTE(entry.getKey());
				world.markChunkDirty(entry.getKey(), te);
				
				for (LittleTile stTile : entry.getValue()) {
					if (stTile != mainTile) {
						stTile.connection = getStructureLink(stTile);
						stTile.connection.setLoadedStructure(this);
					}
				}
			} catch (MissingTileEntity e) {
				markToBeLoaded(entry.getKey());
				e.printStackTrace();
			}
			
		}
		
		LittleAbsoluteVec absolute = tile.getAbsolutePos();
		if (lastMainTileVec != null) {
			LittleVecContext vec = lastMainTileVec.getRelative(absolute);
			if (!lastMainTileVec.equals(absolute))
				type.move(this, vec.getContext(), vec.getVec());
		}
		lastMainTileVec = absolute;
		
		updateStructure();
		
	}
	
	public boolean doesLinkToMainTile(LittleTile tile) {
		try {
			return tile == getMainTile() || (tile.connection.isLink() && tile.connection.getStructurePosition().equals(mainTile.getBlockPos()) && tile.connection.is(mainTile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// ================Tiles================
	
	public boolean loaded() {
		return mainTile != null && tiles != null && (tilesToLoad == null || tilesToLoad.size() == 0) && !isRelationToParentBroken() && !isRelationToChildrenBroken();
	}
	
	protected void checkLoaded() {
		if (!loaded())
			throw new RuntimeException("Structure is not loaded cannot add tile!");
	}
	
	protected boolean checkForTiles(World world, BlockPos pos, Integer expectedCount) {
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		if (WorldUtils.checkIfChunkExists(chunk)) {
			// chunk.isChunkLoaded
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof TileEntityLittleTiles) {
				if (!((TileEntityLittleTiles) tileEntity).hasLoaded())
					return false;
				int found = 0;
				
				if (tiles.keySet().contains(pos))
					tiles.removeKey(pos);
				
				for (LittleTile tile : (TileEntityLittleTiles) tileEntity) {
					if (tile.isChildOfStructure() && (tile.connection.getStructureWithoutLoading() == this || doesLinkToMainTile(tile))) {
						tiles.add(pos, tile);
						if (tile.connection.isLink())
							tile.connection.setLoadedStructure(this);
						found++;
					}
				}
				
				if (found == expectedCount)
					return true;
			}
		}
		return false;
	}
	
	public boolean load() {
		if (mainTile != null) {
			
			if (tiles == null) {
				tiles = new HashMapList<>();
				add(mainTile);
			}
			
			if (tilesToLoad == null)
				return !isRelationToParentBroken() && !isRelationToChildrenBroken();
			
			for (Iterator<Entry<BlockPos, Integer>> iterator = tilesToLoad.entrySet().iterator(); iterator.hasNext();) {
				Entry<BlockPos, Integer> entry = iterator.next();
				if (checkForTiles(mainTile.te.getWorld(), entry.getKey(), entry.getValue()))
					iterator.remove();
			}
			
			if (!tiles.contains(mainTile))
				add(mainTile);
			
			if (tilesToLoad.size() == 0)
				tilesToLoad = null;
			return loaded();
		}
		
		return false;
	}
	
	public List<TileEntityLittleTiles> collectBlocks() {
		if (!load())
			return Collections.EMPTY_LIST;
		
		World world = getWorld();
		List<TileEntityLittleTiles> blocks = new ArrayList<>(tiles.size());
		for (BlockPos pos : tiles.keySet())
			blocks.add((TileEntityLittleTiles) world.getTileEntity(pos));
		return blocks;
	}
	
	public HashMapList<BlockPos, LittleTile> copyOfTiles() {
		if (!load())
			return EMPTY_HASHMAPLIST;
		return new HashMapList<>(tiles);
	}
	
	public Iterator<LittleTile> getTiles() {
		if (!load())
			return EMPTY_ITERATOR;
		
		return tiles.iterator();
	}
	
	public TileEntityLittleTiles loadTE(BlockPos pos) throws MissingTileEntity {
		TileEntity te = getWorld().getTileEntity(pos);
		if (te == null || !(te instanceof TileEntityLittleTiles))
			throw new MissingTileEntity(pos);
		return (TileEntityLittleTiles) te;
	}
	
	public HashMapList<BlockPos, LittleTile> blockTiles() {
		if (!load())
			return EMPTY_HASHMAPLIST;
		return tiles;
	}
	
	public HashMapList<BlockPos, LittleTile> collectBlockTilesChildren(HashMapList<BlockPos, LittleTile> tiles, boolean onlySameWorld) {
		if (!load() || !loadChildren())
			return tiles;
		
		tiles.addAll(this.tiles);
		for (IStructureChildConnector child : children)
			if (!onlySameWorld || !child.isLinkToAnotherWorld())
				child.getStructure(getWorld()).collectBlockTilesChildren(tiles, onlySameWorld);
		return tiles;
	}
	
	public boolean contains(LittleTile tile) {
		return tiles.contains(tile.getBlockPos(), tile);
	}
	
	public void replace(LittleTile oldTile, LittleTile newTile) {
		tiles.removeValue(oldTile.getBlockPos(), oldTile);
		tiles.add(newTile.getBlockPos(), newTile);
	}
	
	public void remove(LittleTile tile) {
		checkLoaded();
		
		tiles.removeValue(tile.getBlockPos(), tile);
	}
	
	public void add(LittleTile tile) {
		tiles.add(tile.getBlockPos(), tile);
	}
	
	public void combineTiles() {
		if (load()) {
			for (TileEntityLittleTiles te : collectBlocks())
				te.combineTiles(this);
		}
	}
	
	protected void markToBeLoaded(BlockPos pos) {
		List<LittleTile> tiles = this.tiles.getValues(pos);
		this.tiles.removeKey(pos);
		
		if (tiles != null && !tiles.isEmpty()) {
			if (tilesToLoad == null)
				tilesToLoad = new LinkedHashMap<>();
			tilesToLoad.put(pos, tiles.size());
		}
	}
	
	public int count() {
		int count = 0;
		if (tilesToLoad != null)
			for (Integer tiles : tilesToLoad.values())
				count += tiles;
			
		if (tiles != null) {
			for (Entry<BlockPos, ArrayList<LittleTile>> entry : tiles.entrySet())
				if (tilesToLoad == null || !tilesToLoad.containsKey(entry.getKey()))
					count += entry.getValue().size();
		}
		return count;
	}
	
	public boolean isChildOf(LittleStructure structure) {
		if (parent != null && parent.isConnected(getWorld()))
			return structure == parent.getStructureWithoutLoading() || parent.getStructureWithoutLoading().isChildOf(structure);
		return false;
	}
	
	// ================Connections================
	
	public LittleIdentifierStructureRelative getMainTileCoord(BlockPos pos) {
		return new LittleIdentifierStructureRelative(pos, mainTile.getBlockPos(), mainTile.getContext(), mainTile.getIdentifier(), getAttribute());
	}
	
	public StructureLinkTile getStructureLink(LittleTile tile) {
		return new StructureLinkTile(tile.te, mainTile.getBlockPos(), mainTile.getContext(), mainTile.getIdentifier(), getAttribute(), tile);
	}
	
	public boolean isRelationToParentBroken() {
		return parent != null && !parent.isConnected(getWorld());
	}
	
	public boolean isRelationToChildrenBroken() {
		for (IStructureChildConnector child : children) {
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
			connector = new StructureLink(this.mainTile.te, child.getMainTile().getBlockPos(), child.getMainTile().getContext(), child.getMainTile().getIdentifier(), child.getAttribute(), this, i, false);
		else if (childWorld instanceof SubWorld && ((SubWorld) childWorld).parent != null)
			connector = new StructureLinkToSubWorld(child.getMainTile(), child.getAttribute(), this, i, ((SubWorld) childWorld).parent.getUniqueID());
		else
			throw new RuntimeException("Invalid connection between to structures!");
		
		connector.setLoadedStructure(child);
		if (children.size() > i)
			children.set(i, connector);
		else if (children.size() == i)
			children.add(connector);
		else
			throw new RuntimeException("Invalid childId " + children.size() + ".");
	}
	
	public void updateParentConnection(int i, LittleStructure parent) {
		World world = getWorld();
		World parentWorld = parent.getWorld();
		
		IStructureChildConnector<LittleStructure> connector;
		if (parentWorld == world)
			connector = new StructureLink(this.mainTile.te, parent.getMainTile().getBlockPos(), parent.getMainTile().getContext(), parent.getMainTile().getIdentifier(), parent.getAttribute(), this, i, true);
		else if (world instanceof SubWorld && ((SubWorld) world).parent != null)
			connector = new StructureLinkFromSubWorld(parent.getMainTile(), parent.getAttribute(), this, i);
		else
			throw new RuntimeException("Invalid connection between to structures!");
		
		connector.setLoadedStructure(parent);
		this.parent = connector;
	}
	
	public boolean loadParent() {
		if (parent != null)
			return parent.isConnected(getWorld());
		return true;
	}
	
	public boolean loadChildren() {
		if (children == null)
			children = new ArrayList<>();
		
		if (children.isEmpty())
			return true;
		
		for (IStructureChildConnector child : children)
			if (!child.isConnected(mainTile.te.getWorld()) || !child.getStructureWithoutLoading().load() || !child.getStructureWithoutLoading().loadChildren())
				return false;
			
		return true;
	}
	
	// ================Placing================
	
	/** takes name of stack and connects the structure to its children (does so recursively)
	 * 
	 * @param stack
	 */
	public void placedStructure(@Nullable ItemStack stack) {
		NBTTagCompound nbt;
		if (name == null && stack != null && (nbt = stack.getSubCompound("display")) != null && nbt.hasKey("Name", 8))
			name = nbt.getString("Name");
	}
	
	// ================Synchronization================
	
	/** This will notify every client that the structure has changed */
	public void updateStructure() {
		mainTile.te.updateBlock();
	}
	
	// ================Save and loading================
	
	public void loadStructure(LittleTile mainTile) {
		this.mainTile = mainTile;
		this.mainTile.connection = new StructureMainTile(mainTile, this);
		
		if (tiles != null && !contains(mainTile))
			add(mainTile);
	}
	
	public void loadFromNBT(NBTTagCompound nbt) {
		if (tiles != null)
			tiles = null;
		
		tilesToLoad = new LinkedHashMap<>();
		
		// LoadTiles
		if (nbt.hasKey("count")) // Old way
		{
			int count = nbt.getInteger("count");
			for (int i = 0; i < count; i++) {
				LittleIdentifierRelative coord = null;
				if (nbt.hasKey("i" + i + "coX")) {
					LittleTilePosition pos = new LittleTilePosition("i" + i, nbt);
					coord = new LittleIdentifierRelative(mainTile.te, pos.coord, LittleGridContext.get(), new int[] { pos.position.x, pos.position.y, pos.position.z });
				} else {
					coord = LittleIdentifierRelative.loadIdentifierOld("i" + i, nbt);
				}
				
				BlockPos pos = coord.getAbsolutePosition(mainTile.te);
				Integer insideBlock = tilesToLoad.get(pos);
				if (insideBlock == null)
					insideBlock = new Integer(1);
				else
					insideBlock = insideBlock + 1;
				tilesToLoad.put(pos, insideBlock);
			}
			
			tiles = new HashMapList<>();
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
			
			tiles = new HashMapList<>();
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
			
			NBTTagList list = nbt.getTagList("children", 10);
			children = new ArrayList<>(list.tagCount());
			for (int i = 0; i < list.tagCount(); i++)
				children.add(StructureLink.loadFromNBT(this, list.getCompoundTagAt(i), false));
			
			if (this instanceof IAnimatedStructure && ((IAnimatedStructure) this).isAnimated()) {
				for (IStructureChildConnector child : children) {
					if (child instanceof StructureLinkToSubWorld && ((StructureLinkToSubWorld) child).entityUUID.equals(((IAnimatedStructure) this).getAnimation().getUniqueID()))
						throw new RuntimeException("Something went wrong during loading!");
					
				}
			}
		} else
			children = new ArrayList<>();
		
		for (StructureDirectionalField field : type.directional) {
			if (nbt.hasKey(field.saveKey))
				field.createAndSet(this, nbt);
			else
				field.set(this, failedLoadingRelative(nbt, field));
		}
		
		loadFromNBTExtra(nbt);
	}
	
	protected Object failedLoadingRelative(NBTTagCompound nbt, StructureDirectionalField field) {
		return field.getDefault();
	}
	
	protected abstract void loadFromNBTExtra(NBTTagCompound nbt);
	
	public NBTTagCompound writeToNBTPreview(NBTTagCompound nbt, BlockPos newCenter) {
		nbt.setString("id", type.id);
		if (name != null)
			nbt.setString("name", name);
		else
			nbt.removeTag("name");
		
		LittleVecContext vec = getMainTile().getAbsolutePos().getRelative(new LittleAbsoluteVec(newCenter, getMainTile().getContext()));
		
		LittleVec inverted = vec.getVec().copy();
		inverted.invert();
		
		for (StructureDirectionalField field : type.directional) {
			Object value = field.get(this);
			field.move(value, vec.getContext(), vec.getVec());
			field.save(nbt, value);
			field.move(value, vec.getContext(), inverted);
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
			for (IStructureChildConnector child : children) {
				list.appendTag(child.writeToNBT(new NBTTagCompound()));
			}
			nbt.setTag("children", list);
		}
		
		// SaveTiles
		HashMap<BlockPos, Integer> positions = new HashMap<>();
		if (tiles != null)
			for (Entry<BlockPos, ArrayList<LittleTile>> entry : tiles.entrySet())
				if (entry.getValue().size() > 0)
					positions.put(entry.getKey(), entry.getValue().size());
				
		if (tilesToLoad != null)
			positions.putAll(tilesToLoad);
		
		if (positions.size() > 0) {
			NBTTagList list = new NBTTagList();
			for (Iterator<Entry<BlockPos, Integer>> iterator = positions.entrySet().iterator(); iterator.hasNext();) {
				Entry<BlockPos, Integer> entry = iterator.next();
				RelativeBlockPos pos = new RelativeBlockPos(mainTile.te, entry.getKey());
				list.appendTag(new NBTTagIntArray(new int[] { pos.getRelativePos().getX(), pos.getRelativePos().getY(), pos.getRelativePos().getZ(), entry.getValue() }));
			}
			nbt.setTag("tiles", list);
		}
		
		for (StructureDirectionalField field : type.directional) {
			Object value = field.get(this);
			field.save(nbt, value);
		}
		
		writeToNBTExtra(nbt);
	}
	
	protected abstract void writeToNBTExtra(NBTTagCompound nbt);
	
	// ====================Destroy====================
	
	public void onLittleTileDestroy() {
		if (parent != null) {
			if (parent.isConnected(getWorld()))
				parent.getStructure(getWorld()).onLittleTileDestroy();
			return;
		}
		
		if (load() && loadChildren())
			removeStructure();
	}
	
	public void removeStructure() {
		checkLoaded();
		
		onStructureDestroyed();
		
		for (IStructureChildConnector child : children)
			child.destroyStructure();
		
		if (this instanceof IAnimatedStructure && ((IAnimatedStructure) this).isAnimated())
			((IAnimatedStructure) this).destroyAnimation();
		else if (mainTile.te.contains(mainTile))
			for (Entry<BlockPos, ArrayList<LittleTile>> entry : tiles.entrySet())
				try {
					loadTE(entry.getKey()).updateTiles((x) -> x.removeAll(entry.getValue()));
				} catch (MissingTileEntity e) {
					//e.printStackTrace();
				}
			
	}
	
	/** Is called before the structure is removed */
	public void onStructureDestroyed() {
		
	}
	
	// ====================Previews====================
	
	public LittleAbsolutePreviews getAbsolutePreviews(BlockPos pos) {
		NBTTagCompound structureNBT = new NBTTagCompound();
		this.writeToNBTPreview(structureNBT, pos);
		LittleAbsolutePreviews previews = new LittleAbsolutePreviews(structureNBT, pos, LittleGridContext.getMin());
		
		for (Iterator<LittleTile> iterator = getTiles(); iterator.hasNext();)
			previews.addTile(iterator.next());
		
		for (IStructureChildConnector child : children)
			previews.addChild(child.getStructure(getWorld()).getPreviews(pos));
		
		previews.convertToSmallest();
		return previews;
	}
	
	public LittlePreviews getPreviews(BlockPos pos) {
		NBTTagCompound structureNBT = new NBTTagCompound();
		this.writeToNBTPreview(structureNBT, pos);
		LittlePreviews previews = new LittlePreviews(structureNBT, LittleGridContext.getMin());
		
		for (Iterator<LittleTile> iterator = getTiles(); iterator.hasNext();) {
			LittleTile tile = iterator.next();
			LittlePreview preview = previews.addTile(tile);
			preview.box.add(new LittleVec(previews.getContext(), tile.getBlockPos().subtract(pos)));
		}
		
		for (IStructureChildConnector child : children)
			previews.addChild(child.getStructure(getWorld()).getPreviews(pos));
		
		previews.convertToSmallest();
		return previews;
	}
	
	public LittleAbsolutePreviews getAbsolutePreviewsSameWorldOnly(BlockPos pos) {
		NBTTagCompound structureNBT = new NBTTagCompound();
		this.writeToNBTPreview(structureNBT, pos);
		LittleAbsolutePreviews previews = new LittleAbsolutePreviews(structureNBT, pos, LittleGridContext.getMin());
		
		for (Iterator<LittleTile> iterator = getTiles(); iterator.hasNext();)
			previews.addTile(iterator.next());
		
		for (IStructureChildConnector child : children)
			if (!child.isLinkToAnotherWorld())
				previews.addChild(child.getStructure(getWorld()).getPreviewsSameWorldOnly(pos));
			else
				previews.addChild(new LittlePreviewsStructureHolder(child.getStructure(getWorld())));
			
		previews.convertToSmallest();
		return previews;
	}
	
	public LittlePreviews getPreviewsSameWorldOnly(BlockPos pos) {
		NBTTagCompound structureNBT = new NBTTagCompound();
		this.writeToNBTPreview(structureNBT, pos);
		LittlePreviews previews = new LittlePreviews(structureNBT, LittleGridContext.getMin());
		
		for (Iterator<LittleTile> iterator = getTiles(); iterator.hasNext();) {
			LittleTile tile = iterator.next();
			LittlePreview preview = previews.addTile(tile);
			preview.box.add(new LittleVec(previews.getContext(), tile.getBlockPos().subtract(pos)));
		}
		
		for (IStructureChildConnector child : children)
			if (!child.isLinkToAnotherWorld())
				previews.addChild(child.getStructure(getWorld()).getPreviews(pos));
			else
				previews.addChild(new LittlePreviewsStructureHolder(child.getStructure(getWorld())));
			
		previews.convertToSmallest();
		return previews;
	}
	
	public MutableBlockPos getMinPos(MutableBlockPos pos) {
		for (BlockPos tePos : tiles.keySet())
			pos.setPos(Math.min(pos.getX(), tePos.getX()), Math.min(pos.getY(), tePos.getY()), Math.min(pos.getZ(), tePos.getZ()));
		
		for (IStructureChildConnector child : children)
			child.getStructure(getWorld()).getMinPos(pos);
		
		return pos;
	}
	
	// ====================Transform & Transfer====================
	
	public void transferChildrenToAnimation(EntityAnimation animation) {
		for (IStructureChildConnector child : children) {
			LittleStructure childStructure = child.getStructure(getWorld());
			if (child.isLinkToAnotherWorld()) {
				EntityAnimation subAnimation = ((IAnimatedStructure) childStructure).getAnimation();
				int l1 = subAnimation.chunkCoordX;
				int i2 = subAnimation.chunkCoordZ;
				World world = getWorld();
				if (subAnimation.addedToChunk) {
					Chunk chunk = world.getChunkFromChunkCoords(l1, i2);
					if (chunk != null)
						chunk.removeEntity(subAnimation);
					subAnimation.addedToChunk = false;
				}
				world.loadedEntityList.remove(subAnimation);
				subAnimation.setParentWorld(animation.fakeWorld);
				animation.fakeWorld.spawnEntity(subAnimation);
				subAnimation.updateTickState();
			} else
				childStructure.transferChildrenToAnimation(animation);
		}
	}
	
	public void transferChildrenFromAnimation(EntityAnimation animation) {
		World parentWorld = animation.fakeWorld.getParent();
		for (IStructureChildConnector child : children) {
			LittleStructure childStructure = child.getStructure(getWorld());
			if (child.isLinkToAnotherWorld()) {
				EntityAnimation subAnimation = ((IAnimatedStructure) childStructure).getAnimation();
				int l1 = subAnimation.chunkCoordX;
				int i2 = subAnimation.chunkCoordZ;
				if (subAnimation.addedToChunk) {
					Chunk chunk = animation.fakeWorld.getChunkFromChunkCoords(l1, i2);
					if (chunk != null)
						chunk.removeEntity(subAnimation);
					subAnimation.addedToChunk = false;
				}
				animation.fakeWorld.loadedEntityList.remove(subAnimation);
				subAnimation.setParentWorld(parentWorld);
				parentWorld.spawnEntity(subAnimation);
				subAnimation.updateTickState();
			} else
				childStructure.transferChildrenFromAnimation(animation);
		}
	}
	
	public void transformAnimation(LittleTransformation transformation) {
		for (IStructureChildConnector child : children) {
			LittleStructure childStructure = child.getStructure(getWorld());
			if (child.isLinkToAnotherWorld())
				((IAnimatedStructure) childStructure).getAnimation().transformWorld(transformation);
			else
				childStructure.transformAnimation(transformation);
		}
	}
	
	// ====================Helpers====================
	
	public AxisAlignedBB getSurroundingBox() {
		if (load())
			return new SurroundingBox(true).add(tiles.entrySet()).getSurroundingBox();
		return null;
	}
	
	public Vec3d getHighestCenterVec() {
		if (load())
			return new SurroundingBox(true).add(tiles.entrySet()).getHighestCenterVec();
		return null;
	}
	
	public LittleAbsoluteVec getHighestCenterPoint() {
		if (load())
			return new SurroundingBox(true).add(tiles.entrySet()).getHighestCenterPoint();
		return null;
	}
	
	// ====================Extra====================
	
	public ItemStack getStructureDrop() {
		if (parent != null) {
			if (parent.isConnected(getWorld()))
				return parent.getStructure(getWorld()).getStructureDrop();
			return ItemStack.EMPTY;
		}
		
		if (load() && loadChildren()) {
			BlockPos pos = getMinPos(new MutableBlockPos(getMainTile().getBlockPos()));
			
			ItemStack stack = new ItemStack(LittleTiles.multiTiles);
			LittlePreviews previews = getPreviews(pos);
			
			LittlePreview.savePreview(previews, stack);
			
			if (name != null) {
				NBTTagCompound display = new NBTTagCompound();
				display.setString("Name", name);
				stack.getTagCompound().setTag("display", display);
			}
			return stack;
		}
		return ItemStack.EMPTY;
	}
	
	public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
		return false;
	}
	
	public boolean isBed(IBlockAccess world, BlockPos pos, EntityLivingBase player) {
		return false;
	}
	
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		
	}
	
	public void onUpdatePacketReceived() {
		
	}
	
	// ====================Active====================
	
	public void tick() {
		
	}
	
	@SideOnly(Side.CLIENT)
	public void renderTick(BlockPos pos, double x, double y, double z, float partialTickTime) {
		
	}
	
	@SideOnly(Side.CLIENT)
	public void getRenderingCubes(BlockPos pos, BlockRenderLayer layer, List<LittleRenderingCube> cubes) {
		
	}
	
	public void addCollisionBoxes(BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn) {
		
	}
	
	public void neighbourChanged() {
		
	}
	
}
