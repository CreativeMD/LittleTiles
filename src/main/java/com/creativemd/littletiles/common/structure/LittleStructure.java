package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTile.LittleTilePosition;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileCoord;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.block.BlockHorizontal;
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
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleStructure {
	
	private static HashMap<String, LittleStructureEntry> structures = new HashMap<String, LittleStructureEntry>();
	
	public static ArrayList<String> getStructureNames()
	{
		ArrayList<String> result = new ArrayList<>();
		for (String id : structures.keySet()) {
			result.add(id);
		}
		return result;
	}
	
	public static void registerLittleStructure(String id, LittleStructure structure)
	{
		registerLittleStructure(id, new LittleStructureEntry(structure.getClass(), structure));
	}
	
	public static void registerLittleStructure(String id, LittleStructureEntry entry)
	{
		if(structures.containsKey(id))
			System.out.println("ID is already taken! id=" + id);
		else if(structures.containsValue(entry))
			System.out.println("Already registered class=" + entry);
		else{
			structures.put(id, entry);
		}
	}
	
	public static String getIDByClass(Class<? extends LittleStructure> classStructure)
	{
		for (String id : structures.keySet()) {
			if(classStructure.equals(structures.get(id).structureClass))
				return id;
		}
		return "";
	}
	
	public static Class<? extends LittleStructure> getClassByID(String id)
	{
		LittleStructureEntry entry = structures.get(id);
		if(entry != null)
			return entry.structureClass;
		return null;
	}
	
	public static LittleStructureEntry getEntryByID(String id)
	{
		return structures.get(id);
	}
	
	public static void initStructures()
	{
		registerLittleStructure("fixed", new LittleFixedStructure());
		registerLittleStructure("chair", new LittleChair());
		registerLittleStructure("door", new LittleDoor());
		registerLittleStructure("slidingDoor", new LittleSlidingDoor());
		registerLittleStructure("ladder", new LittleLadder());
		registerLittleStructure("bed", new LittleBed());
		registerLittleStructure("storage", new LittleStorage());
		registerLittleStructure("noclip", new LittleNoClipStructure());
	}
	
	public static LittleStructure createAndLoadStructure(NBTTagCompound nbt, LittleTile mainTile)
	{
		if(nbt == null)
			return null;
		String id = nbt.getString("id");
		LittleStructureEntry entry = getEntryByID(id);
		if(entry != null)
		{
			Class<? extends LittleStructure> classStructure = entry.structureClass;
			if(classStructure != null)
			{
				LittleStructure structure = null;
				try {
					structure = classStructure.getConstructor().newInstance();
				} catch (Exception e) {
					System.out.println("Found invalid structureID=" + id);
				}
				structure.mainTile = mainTile;
				structure.loadFromNBT(nbt);
				return structure;
			}
		}
		return null;
	}
	
	/**
	 * This will notify every client that the structure has changed
	 */
	public void updateStructure()
	{
		mainTile.te.updateBlock();
	}
	
	public void setMainTile(LittleTile tile)
	{		
		this.mainTile = tile;
		
		this.mainTile.isMainBlock = true;
		this.mainTile.coord = null;
		this.mainTile.te.updateBlock();
		
		if(!containsTile(tile))
			addTile(tile);
		
		for (Iterator<Entry<TileEntityLittleTiles, ArrayList<LittleTile>>> iterator = tiles.entrySet().iterator(); iterator.hasNext();) {
			Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry = iterator.next();
			entry.getKey().getWorld().markChunkDirty(entry.getKey().getPos(), entry.getKey());
			
			for (Iterator iterator2 = entry.getValue().iterator(); iterator2.hasNext();) {
				LittleTile stTile = (LittleTile) iterator2.next();
				
				if(stTile != mainTile)
				{
					stTile.isMainBlock = false;
					stTile.coord = getMainTileCoord(stTile);
				}
			}
		}
		
	}
	
	public LittleTileCoord getMainTileCoord(LittleTile tile)
	{
		return new LittleTileCoord(tile.te, mainTile.te.getPos(), mainTile.getCornerVec());
	}
	
	public boolean hasMainTile()
	{
		return mainTile != null;
	}
	
	public void moveStructure(EnumFacing facing)
	{
		
	}
	
	public void combineTiles()
	{
		if(!hasLoaded())
			return ;
		
		BlockPos pos = null;

		for (Iterator<TileEntityLittleTiles> iterator = tiles.getKeys().iterator(); iterator.hasNext();) {
			iterator.next().combineTiles(this);
		}
	}
	
	public void selectMainTile()
	{
		if(hasLoaded())
		{
			LittleTile first = tiles.getFirst();
			if(first != null)
				setMainTile(first);
		}
	}
	
	private LittleTile mainTile;
	
	/**The core of the structure. Handles saving & loading of the structures. All Tiles inside the structure are containing relative coordinates to this tile**/
	public LittleTile getMainTile()
	{
		return mainTile;
	}
	
	protected HashMapList<TileEntityLittleTiles, LittleTile> tiles = null;
	
	public void setTiles(HashMapList<TileEntityLittleTiles, LittleTile> tiles)
	{
		this.tiles = tiles;
	}
	
	public boolean LoadList()
	{
		if(tiles == null)
			return loadTiles();
		return true;
	}
	
	public boolean containsTile(LittleTile tile)
	{
		return tiles.contains(tile.te, tile);
	}

	public HashMapList<TileEntityLittleTiles, LittleTile> copyOfTiles()
	{
		if(tiles == null)
			if(!loadTiles())
				return new HashMapList<>();
		return new HashMapList<>(tiles);
	}
	
	public Iterator<LittleTile> getTiles()
	{
		if(tiles == null)
			if(!loadTiles())
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
	
	public void removeTile(LittleTile tile)
	{
		if(tiles != null)
			tiles.removeValue(tile.te, tile);
	}
	
	public void addTile(LittleTile tile)
	{
		tiles.add(tile.te, tile);
	}
	
	/*public ArrayList<LittleTile> getTiles()
	{
		if(tiles == null)
			if(!loadTiles())
			{
				ArrayList<LittleTile> tiles =  new ArrayList<>();
				return tiles;
			}
		return tiles;
	}*/
	
	public boolean hasLoaded()
	{
		loadTiles();
		return mainTile != null && tiles != null && (tilesToLoad == null || tilesToLoad.size() == 0);
	}
	
	public boolean loadTiles()
	{
		if(mainTile != null)
		{
			//System.out.println("loading Structure");
			if(tiles == null)
			{
				tiles = new HashMapList<>();
				addTile(mainTile);
			}
			
			if(tilesToLoad == null)
				return true;
			
			//long time = System.nanoTime();
			
			for (Iterator<Entry<BlockPos, Integer>> iterator = tilesToLoad.entrySet().iterator(); iterator.hasNext();) {
				Entry<BlockPos, Integer> entry = iterator.next();
				if(checkForTiles(mainTile.te.getWorld(), entry.getKey(), entry.getValue()))
					iterator.remove();
			}
			
			if(!tiles.contains(mainTile))
				addTile(mainTile);
			/*int i = 0;
			while (i < tilesToLoad.size()) {
				if(checkForTile(mainTile.te.getWorld(), tilesToLoad.get(i)))
					tilesToLoad.remove(i);
				else
					i++;
			}*/
			//System.out.println("LOADING Structure! time=" + (System.nanoTime()-time));
			
			if(tilesToLoad.size() == 0)
				tilesToLoad = null;
			return true;
		}
		return false;
	}
	
	public HashMap<BlockPos, Integer> tilesToLoad = null;
	
	public LittleStructure()
	{
		
	}
	
	public void loadFromNBT(NBTTagCompound nbt)
	{
		/*if(nbt.hasKey("stack"))
		{
			dropStack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
			if(dropStack == null)
			{
				nbt.getCompoundTag("stack").setString("id", LittleTiles.multiTiles.getRegistryName().toString());
				dropStack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
			}
		}*/
		if(tiles != null)
			tiles.clear();
		
		tilesToLoad = new HashMap<>();
		
		//LoadTiles
		if(nbt.hasKey("count")) //Old way
		{
			int count = nbt.getInteger("count");
			for (int i = 0; i < count; i++) {
				LittleTileCoord coord = null;
				if(nbt.hasKey("i" + i + "coX"))
				{
					LittleTilePosition pos = new LittleTilePosition("i" + i, nbt);
					coord = new LittleTileCoord(mainTile.te, pos.coord, pos.position);
				}else{
					coord = new LittleTileCoord("i" + i, nbt);
				}
				
				BlockPos pos = coord.getAbsolutePosition(mainTile.te);
				Integer insideBlock = tilesToLoad.get(pos);
				if(insideBlock == null)
					insideBlock = new Integer(1);
				else
					insideBlock = insideBlock + 1;
				tilesToLoad.put(pos, insideBlock);
			}
			
		}else if(nbt.hasKey("tiles")){ //new way
			NBTTagList list = nbt.getTagList("tiles", 11);
			for (int i = 0; i < list.tagCount(); i++) {
				int[] array = list.getIntArrayAt(i);
				if(array.length == 4)
				{
					LittleTilePos pos = new LittleTilePos(array);
					tilesToLoad.put(pos.getAbsolutePos(mainTile.te), array[3]);
				}
				else
					System.out.println("Found invalid array! " + nbt);
			}
		}
		
		
		
		loadFromNBTExtra(nbt);
	}
	
	protected abstract void loadFromNBTExtra(NBTTagCompound nbt);
	
	public void writeToNBTPreview(NBTTagCompound nbt, BlockPos newCenter)
	{
		nbt.setString("id", getIDOfStructure());
		writeToNBTExtra(nbt);
		//writeToNBT(nbt);
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		/*if(dropStack != null)
		{
			NBTTagCompound nbtStack = new NBTTagCompound();
			dropStack.writeToNBT(nbtStack);
			nbt.setTag("stack", nbtStack);
		}*/
		
		nbt.setString("id", getIDOfStructure());
		
		//SaveTiles
		HashMap<BlockPos, Integer> positions = new HashMap<>();
		if(tiles != null)
		{
			for (Iterator<Entry<TileEntityLittleTiles, ArrayList<LittleTile>>> iterator = tiles.entrySet().iterator(); iterator.hasNext();) {
				Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry = iterator.next();
				if(entry.getValue().size() > 0)
					positions.put(entry.getKey().getPos(), entry.getValue().size());
			}
		}
		
		if(tilesToLoad != null)
			positions.putAll(tilesToLoad);
		
		if(positions.size() > 0)
		{
			NBTTagList list = new NBTTagList();
			for (Iterator<Entry<BlockPos, Integer>> iterator = positions.entrySet().iterator(); iterator.hasNext();) {
				Entry<BlockPos, Integer> entry = iterator.next();
				LittleTilePos pos = new LittleTilePos(mainTile.te, entry.getKey());
				list.appendTag(new NBTTagIntArray(new int[]{pos.getRelativePos().getX(), pos.getRelativePos().getY(), pos.getRelativePos().getZ(), entry.getValue()}));
			}
			nbt.setTag("tiles", list);
		}
		
		/*if(tiles != null)
		{
			nbt.setInteger("count", tiles.size());
			for (int i = 0; i < tiles.size(); i++) {
				if(tiles.get(i).isStructureBlock)
				{
					tiles.get(i).updateCorner();
					new LittleTileCoord(mainTile.te, tiles.get(i).te.getPos(), tiles.get(i).cornerVec.copy()).writeToNBT("i" + i, nbt);
					//new LittleTilePosition().writeToNBT("i" + i, nbt);
				}
				//tiles.get(i).pos.writeToNBT("i" + i, nbt);
			}
		}
		
		if(tilesToLoad != null)
		{
			int start = nbt.getInteger("count");
			nbt.setInteger("count", start + tilesToLoad.size());
			for (int i = 0; i < tilesToLoad.size(); i++) {
				tilesToLoad.get(i).writeToNBT("i" + (i + start), nbt);
			}
		}*/
		
		//if(mainTile == null)
			//System.out.println("Couldn't save tiles!!!" + mainTile.te.getCoord());
		
		writeToNBTExtra(nbt);
	}
	
	protected abstract void writeToNBTExtra(NBTTagCompound nbt);
	
	public boolean doesLinkToMainTile(LittleTile tile)
	{
		try{
			return tile == getMainTile() || (!tile.isMainBlock && tile.coord.getAbsolutePosition(tile.te).equals(mainTile.te.getPos()) && mainTile.isCornerAt(tile.coord.position));
		}catch(Exception e){
			//e.printStackTrace();
		}
		return false;
	}
	
	public boolean checkForTiles(World world, BlockPos pos, Integer expectedCount)
	{
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		if(WorldUtils.checkIfChunkExists(chunk))
		{
			//chunk.isChunkLoaded
			TileEntity tileEntity = world.getTileEntity(pos);
			if(tileEntity instanceof TileEntityLittleTiles)
			{
				if(!((TileEntityLittleTiles) tileEntity).hasLoaded())
					return false;
				int found = 0;
				
				if(tiles.getKeys().contains(tileEntity))
					tiles.removeKey((TileEntityLittleTiles) tileEntity);
				
				for (Iterator iterator = ((TileEntityLittleTiles) tileEntity).getTiles().iterator(); iterator.hasNext();) {
					LittleTile tile = (LittleTile) iterator.next();
					if(tile.isStructureBlock && (tile.structure == this || doesLinkToMainTile(tile)))
					{
						tiles.add((TileEntityLittleTiles) tileEntity, tile);
						tile.structure = this;
						found++;
					}
				}
				
				if(found == expectedCount)
					return true;
			}
		}
		return false;
	}
	
	/*public boolean checkForTile(World world, LittleTileCoord pos)
	{
		BlockPos coord = pos.getAbsolutePosition(mainTile.te);
		Chunk chunk = world.getChunkFromBlockCoords(coord);
		if(WorldUtils.checkIfChunkExists(chunk))
		{
			//chunk.isChunkLoaded
			TileEntity tileEntity = world.getTileEntity(coord);
			if(tileEntity instanceof TileEntityLittleTiles)
			{
				LittleTile tile = ((TileEntityLittleTiles) tileEntity).getTile(pos.position);
				if(tile != null && tile.isStructureBlock)
				{
					if(!tiles.contains(tile))
						addTile(tile);
					tile.structure = this;
					return true;
				}
			}
		}
		return false;
	}*/
	
	/*@Deprecated
	public boolean checkForTile(World world, LittleTilePosition pos)
	{
		Chunk chunk = world.getChunkFromBlockCoords(pos.coord.posX, pos.coord.posZ);
		if(!(chunk instanceof EmptyChunk))
		{
			//chunk.isChunkLoaded
			TileEntity tileEntity = world.getTileEntity(pos.coord.posX, pos.coord.posY, pos.coord.posZ);
			if(tileEntity instanceof TileEntityLittleTiles)
			{
				LittleTile tile = ((TileEntityLittleTiles) tileEntity).getTile(pos.position);
				if(tile != null && tile.isStructureBlock)
				{
					if(!tiles.contains(tile))
						tiles.add(tile);
					tile.structure = this;
					return true;
				}
			}
		}
		return false;
	}*/
	
	//====================Placing====================
	
	public boolean shouldPlaceTile(LittleTile tile)
	{
		return true;
	}
	
	public ArrayList<PlacePreviewTile> getSpecialTiles()
	{
		return new ArrayList<>();
	}
	
	//====================LittleTile-Stuff====================
	
	public void onLittleTileDestroy()
	{
		if(hasLoaded())
		{
			for (Iterator iterator = getTiles(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				tile.te.removeTile(tile);
			}
		}
	}
	
	public ItemStack getStructureDrop()
	{
		if(hasLoaded())
		{
			BlockPos pos = getMainTile().te.getPos();
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			
			for (Iterator<TileEntityLittleTiles> iterator = tiles.getKeys().iterator(); iterator.hasNext();) {
				TileEntityLittleTiles te = iterator.next();
				x = Math.min(x, te.getPos().getX());
				y = Math.min(y, te.getPos().getY());
				z = Math.min(z, te.getPos().getZ());
			}
			
			pos = new BlockPos(x, y, z);
			
			ItemStack stack = new ItemStack(LittleTiles.multiTiles);
			
			List<LittleTilePreview> previews = new ArrayList<>();
			
			for (Iterator<LittleTile> iterator = getTiles(); iterator.hasNext();) {
				LittleTile tile = iterator.next();
				LittleTilePreview preview = tile.getPreviewTile();
				preview.box.addOffset(tile.te.getPos().subtract(pos));
				previews.add(preview);
			}
			
			LittleTilePreview.savePreviewTiles(previews, stack);
			
			NBTTagCompound structureNBT = new NBTTagCompound();
			
			this.writeToNBTPreview(structureNBT, pos);
			stack.getTagCompound().setTag("structure", structureNBT);
			return stack;
		}
		return ItemStack.EMPTY;
	}
	
	public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return false;
	}
	
	//====================SORTING====================
	
	public HashMapList<BlockPos, LittleTile> getTilesSortedPerBlock()
	{
		HashMapList<BlockPos, LittleTile> coords = new HashMapList<>();
		for (Iterator iterator = getTiles(); iterator.hasNext();) {
			LittleTile tile = (LittleTile) iterator.next();
			coords.add(tile.te.getPos(), tile);
		}
		return coords;
	}
	
	public void onFlip(World world, EntityPlayer player, ItemStack stack, Axis axis){}
	
	public void onRotate(World world, EntityPlayer player, ItemStack stack, Rotation rotation){}
	
	//====================GUI STUFF====================
	@SideOnly(Side.CLIENT)
	public abstract void createControls(SubGui gui, LittleStructure structure);
	
	@SideOnly(Side.CLIENT)
	public abstract LittleStructure parseStructure(SubGui gui);
	
	
	//====================Helpers====================
	public LittleTileSize getSize()
	{
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		
		HashMapList<BlockPos, LittleTile> coords = getTilesSortedPerBlock();
		if(coords.sizeOfValues() == 0)
			return null;
		for (BlockPos coord : coords.getKeys()) {
			ArrayList<LittleTile> values = coords.getValues(coord);
			for (int j = 0; j < values.size(); j++) {
				LittleTileBox box = values.get(j).getCompleteBox();
				minX = Math.min(minX, coord.getX()*LittleTile.gridSize+box.minX);
				minY = Math.min(minY, coord.getY()*LittleTile.gridSize+box.minY);
				minZ = Math.min(minZ, coord.getZ()*LittleTile.gridSize+box.minZ);
				
				maxX = Math.max(maxX, coord.getX()*LittleTile.gridSize+box.maxX);
				maxY = Math.max(maxY, coord.getY()*LittleTile.gridSize+box.maxY);
				maxZ = Math.max(maxZ, coord.getZ()*LittleTile.gridSize+box.maxZ);
			}
			/*
			minX = Math.min(minX, coord.posX);
			minY = Math.min(minY, coord.posY);
			minZ = Math.min(minZ, coord.posZ);
			
			maxX = Math.max(maxX, coord.posX);
			maxY = Math.max(maxY, coord.posY);
			maxZ = Math.max(maxZ, coord.posZ);*/
		}
		
		return new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ).getSize();
	}
	
	public LittleTileVec getHighestCenterPoint()
	{
		int minYPos = Integer.MAX_VALUE;
		
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		
		int maxYPos = Integer.MIN_VALUE;
		
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		
		HashMapList<BlockPos, LittleTile> coords = getTilesSortedPerBlock();
		if(coords.sizeOfValues() == 0)
			return null;
		for (BlockPos coord : coords.getKeys()) {
			ArrayList<LittleTile> values = coords.getValues(coord);
			for (int j = 0; j < values.size(); j++) {
				LittleTileBox box = values.get(j).getCompleteBox();
				minX = Math.min(minX, coord.getX()*LittleTile.gridSize+box.minX);
				minY = Math.min(minY, coord.getY()*LittleTile.gridSize+box.minY);
				minZ = Math.min(minZ, coord.getZ()*LittleTile.gridSize+box.minZ);
				
				maxX = Math.max(maxX, coord.getX()*LittleTile.gridSize+box.maxX);
				maxY = Math.max(maxY, coord.getY()*LittleTile.gridSize+box.maxY);
				maxZ = Math.max(maxZ, coord.getZ()*LittleTile.gridSize+box.maxZ);
				
				minYPos = Math.min(minYPos, coord.getY());
				maxYPos = Math.max(maxYPos, coord.getY());
			}
			/*
			minX = Math.min(minX, coord.posX);
			minY = Math.min(minY, coord.posY);
			minZ = Math.min(minZ, coord.posZ);
			
			maxX = Math.max(maxX, coord.posX);
			maxY = Math.max(maxY, coord.posY);
			maxZ = Math.max(maxZ, coord.posZ);*/
		}
		
		//double test = Math.floor(((minX+maxX)/LittleTile.gridSize/2D));
		int centerX = (int) Math.floor((minX+maxX)/(double)LittleTile.gridSize/2D);
		int centerY = (int) Math.floor((minY+maxY)/(double)LittleTile.gridSize/2D);
		int centerZ = (int) Math.floor((minZ+maxZ)/(double)LittleTile.gridSize/2D);
		
		int centerTileX = (int) (Math.floor(minX+maxX)/2D)-centerX*LittleTile.gridSize;
		int centerTileY = (int) (Math.floor(minY+maxY)/2D)-centerY*LittleTile.gridSize;
		int centerTileZ = (int) (Math.floor(minZ+maxZ)/2D)-centerZ*LittleTile.gridSize;
		
		LittleTileVec position = new LittleTileVec((minX+maxX)/2, minYPos*LittleTile.gridSize, (minZ+maxZ)/2);
		//position.y = ;
		for (int y = minYPos; y <= maxYPos; y++) {
			ArrayList<LittleTile> tilesInCenter = coords.getValues(new BlockPos(centerX, y, centerZ));
			if(tilesInCenter != null)
			{
				LittleTileBox box = new LittleTileBox(centerTileX, LittleTile.minPos, centerTileZ, centerTileX+1, LittleTile.maxPos, centerTileZ+1);
				//int highest = LittleTile.minPos;
				for (int i = 0; i < tilesInCenter.size(); i++) {
					List<LittleTileBox> collision = tilesInCenter.get(i).getCollisionBoxes();
					for (int j = 0; j < collision.size(); j++) {
						LittleTileBox littleBox = collision.get(j);
						if(LittleTileBox.intersectsWith(box, littleBox))
						{
							position.y = Math.max(y*LittleTile.gridSize+littleBox.maxY, position.y);
							//highest = Math.max(highest, littleBox.maxY);
						}
					}
				}
				
			}
		}
		
		
		return position;
	}
	
	//====================LittleStructure ID====================
	
	public String getIDOfStructure()
	{
		return getIDByClass(this.getClass());
	}
	
	public static class LittleStructureEntry {
		
		public Class<? extends LittleStructure> structureClass;
		public LittleStructure parser;
		
		public LittleStructureEntry(Class<? extends LittleStructure> structureClass, LittleStructure parser)
		{
			this.structureClass = structureClass;
			this.parser = parser;
		}
		
		@Override
		public boolean equals(Object object)
		{
			return object instanceof LittleStructureEntry && ((LittleStructureEntry) object).structureClass == this.structureClass;
		}
		
		@Override
		public String toString()
		{
			return structureClass.toString();
		}
	}
	
	public boolean isBed(IBlockAccess world, BlockPos pos, EntityLivingBase player)
	{
		return false;
	}

	public boolean isLadder()
	{
		return false;
	}
	
	public boolean noCollisionBoxes()
	{
		return false;
	}
	
	public boolean shouldCheckForCollision()
	{
		return false;
	}
	
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
		
    }

	public void onUpdatePacketReceived()
	{
		
	}

	public void removeWorldProperties()
	{
		mainTile = null;
		tiles = new HashMapList<>();
		tilesToLoad = null;
	}
	
}
