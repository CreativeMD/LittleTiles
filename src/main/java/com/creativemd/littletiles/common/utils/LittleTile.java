package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import java.util.Random;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileCoord;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
	
	public static void setGridSize(int size)
	{
		gridSize = size;
		halfGridSize = gridSize/2;
		gridMCLength = 1D/gridSize;
		minPos = 0;
		maxPos = gridSize;
		maxTilesPerBlock = gridSize*gridSize*gridSize;
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
	
	public static LittleTile CreateandLoadTile(TileEntityLittleTiles te, World world, NBTTagCompound nbt)
	{
		return CreateandLoadTile(te, world, nbt, false, null);
	}
	
	public static LittleTile CreateandLoadTile(TileEntityLittleTiles te, World world, NBTTagCompound nbt, boolean isPacket, NetworkManager net)
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
			String id = nbt.getString("tID");
			Class<? extends LittleTile> TileClass = getClassByID(id);
			LittleTile tile = null;
			if(TileClass != null)
			{
				try {
					tile = TileClass.getConstructor().newInstance();
				} catch (Exception e) {
					System.out.println("Found invalid tileID=" + id);
				}
			}
			if(tile != null)
				if(isPacket)
					tile.receivePacket(nbt, net);
				else{
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
	
	public boolean needsFullUpdate = false;
	
	public boolean invisible = false;
	
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
	
	public boolean canBeCombined(LittleTile tile) {
		//if(isStructureBlock && isMainBlock)
			//return false;
		if(isStructureBlock != tile.isStructureBlock)
			return false;
		if(isStructureBlock && structure != tile.structure)
			return false;
		
		if(invisible != tile.invisible)
			return false;
		
		return true;
	}
	
	public boolean canBeSplitted()
	{
		return true;
	}
	//public abstract boolean canBeCombined(LittleTile tile);
	
	public void combineTiles(LittleTile tile)
	{
		if(isLoaded())
		{
			structure.getTiles().remove(tile);
		}
	}
	
	public boolean doesProvideSolidFace(EnumFacing facing)
	{
		return !invisible;
	}
	
	public boolean canBeRenderCombined(LittleTile tile)
	{
		return this.invisible == tile.invisible;
	}
	
	//================Packets================
	
	public void updatePacket(NBTTagCompound nbt)
	{
		nbt.setInteger("bSize", boundingBoxes.size());
		for (int i = 0; i < boundingBoxes.size(); i++) {
			boundingBoxes.get(i).writeToNBT("bBox" + i, nbt);
		}
	}
	
	public void receivePacket(NBTTagCompound nbt, NetworkManager net)
	{
		int count = nbt.getInteger("bSize");
		
		if(te.isClientSide())
			te.removeTile(this);
		boundingBoxes.clear();
		for (int i = 0; i < count; i++) {
			boundingBoxes.add(new LittleTileBox("bBox" + i, nbt));
		}
		if(te.isClientSide())
			te.addTile(this);
		updateCorner();
	}
	
	//================Save & Loading================
	
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
	}
	
	public void saveTileCore(NBTTagCompound nbt)
	{
		nbt.setString("tID", getID());
		if(cornerVec != null)
			cornerVec.writeToNBT("cVec", nbt);
		nbt.setInteger("bSize", boundingBoxes.size());
		for (int i = 0; i < boundingBoxes.size(); i++) {
			boundingBoxes.get(i).writeToNBT("bBox" + i, nbt);
		}
		
		if(isStructureBlock)
		{
			nbt.setBoolean("isStructure", true);
			if(isMainBlock)
			{
				nbt.setBoolean("main", true);
				structure.writeToNBT(nbt);
			}else{
				coord.writeToNBT(nbt);
				//pos.writeToNBT(nbt);
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
	}
	
	public void loadTileCore(NBTTagCompound nbt)
	{
		cornerVec = new LittleTileVec("cVec", nbt);
		int count = nbt.getInteger("bSize");
		boundingBoxes.clear();
		for (int i = 0; i < count; i++) {
			boundingBoxes.add(new LittleTileBox("bBox" + i, nbt));
		}
		updateCorner();
		
		isStructureBlock = nbt.getBoolean("isStructure");
		
		if(isStructureBlock)
		{
			if(nbt.getBoolean("main"))
			{
				isMainBlock = true;
				structure = LittleStructure.createAndLoadStructure(nbt, this);
				//structure.mainTile = this;
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
	
	/**return null for any rotation**/
	//public abstract ForgeDirection[] getValidRotation();
	
	/**stack may be null**/
	public void onPlaced(EntityPlayer player , ItemStack stack, EnumFacing facing)
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
		//LittleTileBox box = new LittleTileBox(getSelectedBox());
		updateCorner();
		te.addTile(this);
		//te.getWorldObj().playSoundEffect((double)((float)te.xCoord + 0.5F), (double)((float)te.yCoord + 0.5F), (double)((float)te.zCoord + 0.5F), getSound().func_150496_b(), (getSound().getVolume() + 1.0F) / 2.0F, getSound().getPitch() * 0.8F);
	}
	
	//================Destroying================
	
	public void onDestoryed(){}
	
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
	
	/*@SideOnly(Side.CLIENT)
	public boolean isRendering;
	
	@SideOnly(Side.CLIENT)
	public ArrayList<LittleBlockVertex> lastRendered;*/
	
	public boolean needCustomRendering()
	{
		return false;
	}
	
	/*@SideOnly(Side.CLIENT)
	public abstract boolean canBlockBeThreaded();*/
	
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
	
	/*public static final int ticksBetweenRefresh = 1200;
	
	public int ticks = 0;
	
	public void updateEntity()
	{
		ticks++;
		if(ticks > ticksBetweenRefresh)
		{
			ticks = 0;
			if(isStructureBlock && isMainBlock && structure.tilesToLoad != null)
			{
				//System.out.println("Loading structure x=" + te.xCoord + " y=" + te.yCoord + " z=" + te.zCoord + "");
				structure.loadTiles();
			}
		}
	}
	
	public boolean shouldTick()
	{
		return isStructureBlock && isMainBlock;
	}*/
	
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
		return 0;
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
	
	public boolean isBed(IBlockAccess world, BlockPos pos, EntityLivingBase player)
	{
		if(isLoaded())
			return structure.isBed(world, pos, player);
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
	
	/*
	 * Removed positions are now saved relative to the current position
	 * @Deprecated
	 * public LittleTilePosition pos;*/
	
	public LittleTileCoord coord;
	
	public boolean isMainBlock = false;
	
	public boolean checkForStructure()
	{
		if(structure != null)
			return true;
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
							if(this.structure != null && this.structure.getTiles() != null && !this.structure.getTiles().contains(this))
								this.structure.getTiles().add(this);
						}
					}
				}
				
				if(structure == null)
				{
					te.removeTile(this);
					te.updateBlock();
				}
				
				//pos = null;
				
				
				return structure != null;
			}
			
		}
		return false;
	}
	
	public boolean isLoaded()
	{
		return isStructureBlock && checkForStructure();
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

	public LittleTilePreview getPreviewTile() {
		NBTTagCompound nbt = new NBTTagCompound();
		saveTileExtra(nbt);
		nbt.setString("tID", getID());		
		return new LittleTilePreview(boundingBoxes.get(0).copy(), nbt);
	}
	
}
