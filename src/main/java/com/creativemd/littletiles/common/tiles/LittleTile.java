package com.creativemd.littletiles.common.tiles;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import java.util.Random;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.common.items.ItemTileContainer.BlockEntry;
import com.creativemd.littletiles.common.packet.LittleTileUpdatePacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileCoord;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleTile {
	
	private static HashMap<Class<? extends LittleTile>, String> tileIDs = new HashMap<Class<? extends LittleTile>, String>();
	
	public static int gridSize = 16;
	public static int halfGridSize = gridSize/2;
	public static double gridMCLength = 1D/gridSize;
	public static int minPos = 0;
	public static int maxPos = gridSize;
	public static int maxTilesPerBlock = gridSize*gridSize*gridSize;
	public static double minimumTileSize = 1D/maxTilesPerBlock;
	
	public static void setGridSize(int size)
	{
		gridSize = size;
		halfGridSize = gridSize/2;
		gridMCLength = 1D/gridSize;
		minPos = 0;
		maxPos = gridSize;
		maxTilesPerBlock = gridSize*gridSize*gridSize;
		minimumTileSize = 1D/maxTilesPerBlock;
	}
	
	public static Class<? extends LittleTile> getClassByID(String id)
	{
		 for (Entry<Class<? extends LittleTile>, String> entry : tileIDs.entrySet()) {
		        if (id.equals(entry.getValue())) {
		            return entry.getKey();
		        }
		    }
		    return null;
	}
	
	public static String getIDByClass(Class<? extends LittleTile> LittleClass)
	{
		return tileIDs.get(LittleClass);
	}
	
	/**The id has to be unique and cannot be changed!**/
	public static void registerLittleTile(Class<? extends LittleTile> LittleClass, String id)
	{
		tileIDs.put(LittleClass, id);
	}
	
	public static LittleTile CreateEmptyTile(String id)
	{
		Class<? extends LittleTile> TileClass = getClassByID(id);
		if(TileClass != null)
		{
			try {
				return TileClass.getConstructor().newInstance();
			} catch (Exception e) {
				System.out.println("Found invalid tileID=" + id);
			}
		}
		return null;
	}
	
	public static LittleTile CreateandLoadTile(TileEntityLittleTiles te, World world, NBTTagCompound nbt)
	{
		if(nbt.hasKey("tileID")) //If it's the old tileentity
		{
			if(nbt.hasKey("block"))
			{
				Block block = Block.getBlockFromName(nbt.getString("block"));
				int meta = nbt.getInteger("meta");
				LittleTileBox box = new LittleTileBox(new LittleTileVec("i", nbt), new LittleTileVec("a", nbt));
				box.addOffset(new LittleTileVec(halfGridSize, halfGridSize, halfGridSize));
				LittleTileBlock tile = new LittleTileBlock(block, meta);
				tile.boundingBoxes.add(box);
				tile.cornerVec = box.getMinVec();
				return tile;
			}
		}else{
			LittleTile tile = CreateEmptyTile(nbt.getString("tID"));
			if(tile != null)
			{
				try {
					tile.loadTile(te, nbt);
				}catch(Exception e){
					e.printStackTrace();
					return null;
				}
			}
			return tile;		
		}
		return null;
	}
	
	public boolean invisible = false;
	
	public boolean glowing = false;
	
	public TileEntityLittleTiles te;
	
	/**Every LittleTile class has to have this constructor implemented**/
	public LittleTile()
	{
		boundingBoxes = new ArrayList<LittleTileBox>();
	}
	
	public String getID()
	{
		return getIDByClass(this.getClass());
	}
	
	//================Position & Size================
	
	public LittleTileVec cornerVec;
	
	/**Might cause issues in regions x, y or z above 134,217,728**/
	public LittleTileVec getAbsoluteCoordinates()
	{
		LittleTileVec coord = new LittleTileVec(te.getPos());
		coord.addVec(cornerVec);
		return coord;
	}
	
	public ArrayList<LittleTileBox> boundingBoxes;
	
	public AxisAlignedBB getSelectedBox()
	{
		if(boundingBoxes.size() > 0)
		{
			LittleTileBox box = boundingBoxes.get(0).copy();
			for (int i = 1; i < boundingBoxes.size(); i++) {
				box.minX = Math.min(box.minX, boundingBoxes.get(i).minX);
				box.minY = Math.min(box.minY, boundingBoxes.get(i).minY);
				box.minZ = Math.min(box.minZ, boundingBoxes.get(i).minZ);
				box.maxX = Math.max(box.maxX, boundingBoxes.get(i).maxX);
				box.maxY = Math.max(box.maxY, boundingBoxes.get(i).maxY);
				box.maxZ = Math.max(box.maxZ, boundingBoxes.get(i).maxZ);
			}
			return box.getBox();
		}else
			return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
	}
	
	public int getVolume()
	{
		int percent = 0;
		for (int i = 0; i < boundingBoxes.size(); i++) {
			percent += boundingBoxes.get(i).getSize().getVolume();
		}
		return percent;
	}
	
	public double getPercentVolume()
	{
		double percent = 0;
		for (int i = 0; i < boundingBoxes.size(); i++) {
			percent += boundingBoxes.get(i).getSize().getPercentVolume();
		}
		return percent;
	}
	
	public LittleTileSize getSize()
	{
		LittleTileSize size = new LittleTileSize(0, 0, 0);
		for (int i = 0; i < boundingBoxes.size(); i++) {
			LittleTileSize tempSize = boundingBoxes.get(i).getSize();
			size.sizeX = Math.max(size.sizeX, tempSize.sizeX);
			size.sizeY = Math.max(size.sizeY, tempSize.sizeY);
			size.sizeZ = Math.max(size.sizeZ, tempSize.sizeZ);
		}
		return size;
	}
	
	public boolean canBeCombined(LittleTile tile)
	{	
		if(isStructureBlock != tile.isStructureBlock)
			return false;
		
		if(isStructureBlock && structure != tile.structure)
			return false;
		
		if(invisible != tile.invisible)
			return false;
		
		if(glowing != tile.glowing)
			return false;
		
		return true;
	}
	
	/**
	 * Will become more important with further update, right now it should not be touched
	 */
	public boolean canHaveMultipleBoundingBoxes()
	{
		return false;
	}
	
	public boolean canBeSplitted()
	{
		return true;
	}
	
	public void combineTiles(LittleTile tile)
	{
		if(isLoaded())
		{
			structure.removeTile(tile);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public boolean doesProvideSolidFace(EnumFacing facing)
	{
		return !invisible;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean canBeRenderCombined(LittleTile tile)
	{
		return this.invisible == tile.invisible;
	}
	
	//================Packets================
	
	protected static Field playerInChunkMapEntry = ReflectionHelper.findField(PlayerChunkMapEntry.class, "players", "field_187283_c");
	
	/**
	 * Only works for tiles which support update packets. Example: LittleTileTE
	 * @return
	 */
	public boolean sendUpdatePacketToClient()
	{
		if(supportsUpdatePacket())
		{
			if(!te.getWorld().isRemote && te.getWorld() instanceof WorldServer)
			{
				PlayerChunkMap map = ((WorldServer) te.getWorld()).getPlayerChunkMap();
				ChunkPos pos = new ChunkPos(te.getPos());
				PlayerChunkMapEntry entry = map.getEntry(pos.chunkXPos, pos.chunkZPos);
				try {
					List<EntityPlayerMP> players = (List<EntityPlayerMP>) playerInChunkMapEntry.get(entry);
					PacketHandler.sendPacketToPlayers(new LittleTileUpdatePacket(this, getUpdateNBT()), players);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				
			}
			
			return true;
		}
		return false;
	}
	
	/**
	 * Can be used to force a complete update on client for tiles which support update packet (example: LittleTileTE)
	 */
	public boolean needsFullUpdate = false;
	
	public boolean supportsUpdatePacket()
	{
		return false;
	}
	
	public NBTTagCompound getUpdateNBT()
	{
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public void receivePacket(NBTTagCompound nbt, NetworkManager net)
	{
		
	}
	
	public boolean isIdenticalToNBT(NBTTagCompound nbt)
	{
		return getID().equals(nbt.getString("tID")) && glowing == nbt.getBoolean("glowing") && invisible == nbt.getBoolean("invisible");
	}
	
	//================Save & Loading================
	
	public NBTTagCompound startNBTGrouping()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		saveTile(nbt);
		int bSize = nbt.getInteger("bSize");
		
		nbt.setInteger("bSize", boundingBoxes.size());
		for (int i = 0; i < bSize; i++) {
			nbt.removeTag("bBox" + i);
		}
		nbt.removeTag("bSize");
		
		NBTTagList list = new NBTTagList();
		
		for (int i = 0; i < boundingBoxes.size(); i++) {
			list.appendTag(boundingBoxes.get(i).getNBTIntArray());
		}
		nbt.setTag("boxes", list);
		
		return nbt;
	}
	
	public boolean canBeNBTGrouped(LittleTile tile)
	{
		return tile.canBeCombined(this) && this.canBeCombined(tile) && !tile.isMainBlock && !this.isMainBlock;
	}
	
	public void groupNBTTile(NBTTagCompound nbt, LittleTile tile)
	{
		NBTTagList list = nbt.getTagList("boxes", 11);
		
		for (int i = 0; i < tile.boundingBoxes.size(); i++) {
			list.appendTag(tile.boundingBoxes.get(i).getNBTIntArray());
		}
	}
	
	public List<NBTTagCompound> extractNBTFromGroup(NBTTagCompound nbt)
	{
		List<NBTTagCompound> tags = new ArrayList<>();
		NBTTagList list = nbt.getTagList("boxes", 11);
		
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound copy = nbt.copy();
			NBTTagList small = new NBTTagList();
			small.appendTag(list.get(i));
			copy.setTag("boxes", small);
			tags.add(copy);
		}		
		return tags;
	}
	
	public void saveTile(NBTTagCompound nbt)
	{
		saveTileCore(nbt);
		saveTileExtra(nbt);
	}
	
	/**Used to save extra data like block-name, meta, color etc. everything necessary for a preview**/
	public void saveTileExtra(NBTTagCompound nbt)
	{
		if(invisible)
			nbt.setBoolean("invisible", invisible);
		if(glowing)
			nbt.setBoolean("glowing", glowing);
	}
	
	public void saveTileCore(NBTTagCompound nbt)
	{
		nbt.setString("tID", getID());
		
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < boundingBoxes.size(); i++) {
			list.appendTag(boundingBoxes.get(i).getNBTIntArray());
		}
		nbt.setTag("boxes", list);
		
		if(isStructureBlock)
		{
			nbt.setBoolean("isStructure", true);
			if(isMainBlock)
			{
				nbt.setBoolean("main", true);
				structure.writeToNBT(nbt);
			}else{
				coord.writeToNBT(nbt);
			}
		}
	}
	
	public void loadTile(TileEntityLittleTiles te, NBTTagCompound nbt)
	{
		this.te = te;
		loadTileCore(nbt);
		loadTileExtra(nbt);
	}
	
	public void loadTileExtra(NBTTagCompound nbt)
	{
		invisible = nbt.getBoolean("invisible");
		glowing = nbt.getBoolean("glowing");
	}
	
	public void loadTileCore(NBTTagCompound nbt)
	{
		if(nbt.hasKey("bSize"))
		{
			int count = nbt.getInteger("bSize");
			boundingBoxes.clear();
			for (int i = 0; i < count; i++) {
				boundingBoxes.add(new LittleTileBox("bBox" + i, nbt));
			}
		}else{
			NBTTagList list = nbt.getTagList("boxes", 11);
			boundingBoxes.clear();
			for (int i = 0; i < list.tagCount(); i++) {
				boundingBoxes.add(new LittleTileBox(list.getIntArrayAt(i)));
			}
		}
		
		updateCorner();
		
		isStructureBlock = nbt.getBoolean("isStructure");
		
		if(isStructureBlock)
		{
			if(nbt.getBoolean("main"))
			{
				isMainBlock = true;
				if(structure == null)
					structure = LittleStructure.createAndLoadStructure(nbt, this);
				else{
					structure.setMainTile(this);
					for (Iterator<LittleTile> iterator = structure.getTiles(); iterator.hasNext();) {
						LittleTile tile = iterator.next();
						if(tile != this && tile.isLoaded())
							tile.structure = null;
					}
					structure.loadFromNBT(nbt);
				}
			}else{
				if(nbt.hasKey("coX"))
				{
					LittleTilePosition pos = new LittleTilePosition(nbt);
					
					coord = new LittleTileCoord(te, pos.coord, pos.position);
					
					System.out.println("Converting old positioning to new relative coordinates " + pos + " to " + coord);
				}else
					coord = new LittleTileCoord(nbt);
			}
		}
	}
	
	public void markForUpdate()
	{
		if(!te.getWorld().isRemote)
			te.updateBlock();
		else
			te.updateRender();
	}
	
	//================Placing================
	
	/**stack may be null**/
	public void onPlaced(@Nullable EntityPlayer player , ItemStack stack, EnumFacing facing)
	{
		onNeighborChangeInside();
	}
	
	public void updateCorner()
	{
		if(boundingBoxes.size() > 0)
		{
			LittleTileBox box = boundingBoxes.get(0);
			if(cornerVec != null){
				cornerVec.x = box.minX;
				cornerVec.y = box.minY;
				cornerVec.z = box.minZ;
			}else
				cornerVec = new LittleTileVec(box.minX, box.minY, box.minZ);
		}else
			cornerVec = new LittleTileVec(0, 0, 0);
	}
	
	public void place()
	{
		updateCorner();
		te.addTile(this);
	}
	
	//================Destroying================
	
	public void destroy()
	{
		if(isStructureBlock)
		{
			if(!te.getWorld().isRemote && isLoaded())
				structure.onLittleTileDestory();
		}else
			te.removeTile(this);
	}
	
	//================Copy================
	
	public LittleTile copy()
	{
		LittleTile tile = null;
		try {
			tile = this.getClass().getConstructor().newInstance();
		} catch (Exception e) {
			System.out.println("Invalid LittleTile class=" + this.getClass().getName());
			tile = null;
		}
		if(tile != null)
		{
			copyCore(tile);
			copyExtra(tile);
		}
		return tile;
	}
	
	public void assignTo(LittleTile target)
	{
		copyCore(target);
		copyExtra(target);
	}
	
	public void copyExtra(LittleTile tile)
	{
		tile.invisible = this.invisible;
		tile.glowing = this.glowing;
	}
	
	public void copyCore(LittleTile tile)
	{
		for (int i = 0; i < this.boundingBoxes.size(); i++) {
			tile.boundingBoxes.add(this.boundingBoxes.get(i).copy());
		}
		tile.cornerVec = this.cornerVec.copy();
		tile.te = this.te;
		
		tile.isStructureBlock = this.isStructureBlock;
		tile.structure = this.structure;
		if(this.coord != null)
			tile.coord = this.coord.copy();
	}
	
	//================Drop================
	
	public ArrayList<ItemStack> getDrops()
	{
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		ItemStack stack = null;
		if(isStructureBlock)
		{
			if(isLoaded())
				stack = structure.getStructureDrop();
		}else
			stack = getDrop();
		if(stack != null)
			drops.add(stack);
		
		return drops;
	}
	
	public abstract ItemStack getDrop();
	
	/**Used for LittleTileContainer. Can return null.**/
	public abstract BlockEntry getBlockEntry();
	
	public LittleTilePreview getPreviewTile()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		saveTileExtra(nbt);
		nbt.setString("tID", getID());		
		return new LittleTilePreview(boundingBoxes.get(0).copy(), nbt);
	}
	
	//================Notifcations/Events================
	
	public void onNeighborChangeOutside()
	{
		onNeighborChange();
	}
	
	public void onNeighborChangeInside()
	{
		onNeighborChange();
	}
	
	public void onNeighborChange() {}
	
	//================Rendering================
	
	public boolean needCustomRendering()
	{
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean shouldBeRenderedInLayer(BlockRenderLayer layer)
	{
		return layer == BlockRenderLayer.SOLID;
	}
	
	@SideOnly(Side.CLIENT)
	public final ArrayList<RenderCubeObject> getRenderingCubes()
	{
		if(invisible)
			return new ArrayList<>();
		return getInternalRenderingCubes();
	}
	
	@SideOnly(Side.CLIENT)
	protected abstract ArrayList<RenderCubeObject> getInternalRenderingCubes();
	
	@SideOnly(Side.CLIENT)
	public void renderTick(double x, double y, double z, float partialTickTime) {}
	
	@SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
        return 4096;
    }
	
	@SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
    }
	
	//================Sound================
	
	public abstract SoundType getSound();
	
	//================Tick================
	
	public void updateEntity()
	{
		
	}
	
	public boolean shouldTick()
	{
		return false;
	}
	
	//================Interaction================
	
	protected abstract boolean canSawResize(EnumFacing facing, EntityPlayer player);
	
	public boolean canSawResizeTile(EnumFacing facing, EntityPlayer player)
	{
		return boundingBoxes.size() == 1 && !isStructureBlock && canSawResize(facing, player);
	}
	
	public boolean canBeMoved(EnumFacing facing)
	{
		return boundingBoxes.size() == 1;
	}
	
	//================Block Event================
	
	public abstract float getExplosionResistance();
	
	public void onTileExplodes(Explosion explosion) {}
	
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {}

	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(isLoaded())
			return structure.onBlockActivated(worldIn, this, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
		return false;
	}

	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return glowing ? 14 : 0;
	}

	public float getEnchantPowerBonus(World world, BlockPos pos) {
		return 0;
	}
	
	public boolean isLadder()
	{
		if(isLoaded())
			return structure.isLadder();
		return false;
	}
	
	//================Collision================
	
	public ArrayList<LittleTileBox> getCollisionBoxes()
	{
		if(shouldCheckForCollision())
			return new ArrayList<>();
		if(isLoaded() && structure.noCollisionBoxes())
			return new ArrayList<>();
		return this.boundingBoxes;
	}
	
	public boolean shouldCheckForCollision()
	{
		if(isLoaded())
			return structure.shouldCheckForCollision();
		return false;
	}
	
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
		if(isLoaded())
			structure.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
    }
	
	//================Structure================
	
	public boolean isStructureBlock = false;
	
	public LittleStructure structure;
	
	public LittleTileCoord coord;
	
	public boolean isMainBlock = false;
	
	protected boolean loadingStructure = false;
	
	public boolean checkForStructure()
	{
		if(loadingStructure)
			return false;
		
		if(structure != null)
			return true;
		
		loadingStructure = true;
		
		World world = te.getWorld();
		if(world != null)
		{
			BlockPos absoluteCoord = coord.getAbsolutePosition(te);
			Chunk chunk = world.getChunkFromBlockCoords(absoluteCoord);
			if(WorldUtils.checkIfChunkExists(chunk))
			{
				TileEntity tileEntity = world.getTileEntity(absoluteCoord);
				if(tileEntity instanceof TileEntityLittleTiles)
				{
					LittleTile tile = ((TileEntityLittleTiles) tileEntity).getTile(coord.position);
					if(tile != null && tile.isStructureBlock)
					{
						if(tile.isMainBlock)
						{
							this.structure = tile.structure;
							if(this.structure != null && this.structure.LoadList() && !this.structure.containsTile(this))
								this.structure.addTile(this);
						}
					}
				}
				
				if(structure == null && !world.isRemote)
				{
					te.removeTile(this);
					te.updateBlock();
				}
				
				//pos = null;
				
				loadingStructure = false;
				
				return structure != null;
			}
			
		}
		
		loadingStructure = false;
		
		return false;
	}
	
	public boolean isAllowedToSearchForStructure = true;
	
	public boolean isLoaded()
	{
		return isAllowedToSearchForStructure && isStructureBlock && checkForStructure();
	}
	
	@Deprecated
	public static class LittleTilePosition {
		
		public BlockPos coord;
		public LittleTileVec position;
		
		public LittleTilePosition(BlockPos coord, LittleTileVec position)
		{
			this.coord = coord;
			this.position = position;
		}
		
		public LittleTilePosition(String id, NBTTagCompound nbt)
		{
			coord = new BlockPos(nbt.getInteger(id + "coX"), nbt.getInteger(id + "coY"), nbt.getInteger(id + "coZ"));
			position = new LittleTileVec(id + "po", nbt);
		}
		
		public LittleTilePosition(NBTTagCompound nbt)
		{
			this("", nbt);
		}
		
		public void writeToNBT(String id, NBTTagCompound nbt)
		{
			nbt.setInteger(id + "coX", coord.getX());
			nbt.setInteger(id + "coY", coord.getY());
			nbt.setInteger(id + "coZ", coord.getZ());
			position.writeToNBT(id + "po", nbt);
		}
		
		public void writeToNBT(NBTTagCompound nbt)
		{
			writeToNBT("", nbt);
		}
		
		@Override
		public String toString()
		{
			return "coord:" + coord + "|position:" + position;
		}
		
		public LittleTilePosition copy()
		{
			return new LittleTilePosition(new BlockPos(coord), position.copy());
		}
		
	}
	
}
