package com.creativemd.littletiles.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Logger;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class LittleTile {
	
	private static HashMap<Class<? extends LittleTile>, String> tileIDs = new HashMap<Class<? extends LittleTile>, String>();
	
	public static final int minPos = 0;
	public static final int maxPos = 16;
	
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
				box.addOffset(new LittleTileVec(8, 8, 8));
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
	
	public TileEntityLittleTiles te;
	
	/**Every LittleTile class has to have this constructor implemented**/
	public LittleTile()
	{
		boundingBoxes = new ArrayList<LittleTileBox>();
	}
	
	//================Position & Size================
	
	public LittleTileVec cornerVec;
	
	public ArrayList<LittleTileBox> boundingBoxes;
	
	public AxisAlignedBB getSelectedBox()
	{
		if(boundingBoxes.size() > 0)
		{
			LittleTileBox box = boundingBoxes.get(0).copy();
			for (int i = 1; i < boundingBoxes.size(); i++) {
				box.minX = (byte) Math.min(box.minX, boundingBoxes.get(i).minX);
				box.minY = (byte) Math.min(box.minY, boundingBoxes.get(i).minY);
				box.minZ = (byte) Math.min(box.minZ, boundingBoxes.get(i).minZ);
				box.maxX = (byte) Math.max(box.maxX, boundingBoxes.get(i).maxX);
				box.maxY = (byte) Math.max(box.maxY, boundingBoxes.get(i).maxY);
				box.maxZ = (byte) Math.max(box.maxZ, boundingBoxes.get(i).maxZ);
			}
			return box.getBox();
		}else
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
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
			size.sizeX = (byte) Math.max(size.sizeX, tempSize.sizeX);
			size.sizeY = (byte) Math.max(size.sizeY, tempSize.sizeY);
			size.sizeZ = (byte) Math.max(size.sizeZ, tempSize.sizeZ);
		}
		return size;
	}
	
	public abstract boolean canBeCombined(LittleTile tile);
	
	public void combineTiles(LittleTile tile)
	{
		if(isLoaded())
		{
			structure.tiles.remove(tile);
		}
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
		boundingBoxes.clear();
		for (int i = 0; i < count; i++) {
			boundingBoxes.add(new LittleTileBox("bBox" + i, nbt));
		}
		updateCorner();
	}
	
	//================Save & Loading================
	
	public void saveTile(NBTTagCompound nbt)
	{
		saveTileCore(nbt);
		saveTileExtra(nbt);
	}
	
	public abstract void saveTileExtra(NBTTagCompound nbt);
	
	public void saveTileCore(NBTTagCompound nbt)
	{
		nbt.setString("tID", getIDByClass(this.getClass()));
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
				pos.writeToNBT(nbt);
			}
		}
	}
	
	public void loadTile(TileEntityLittleTiles te, NBTTagCompound nbt)
	{
		this.te = te;
		loadTileCore(nbt);
		loadTileExtra(nbt);
	}
	
	public abstract void loadTileExtra(NBTTagCompound nbt);
	
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
				structure = LittleStructure.createAndLoadStructure(nbt);
			}else{
				pos = new LittleTilePosition(nbt);
			}
		}
	}
	
	public void markForUpdate()
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
			te.update();
		else
			te.updateRender();
	}
	
	//================Placing================
	
	/**return null for any rotation**/
	public abstract ForgeDirection[] getValidRotation();
	
	/**stack may be null**/
	public void onPlaced(EntityPlayer player , ItemStack stack)
	{
		onNeighborChangeInside();
	}
	
	public void updateCorner()
	{
		if(boundingBoxes.size() > 0)
		{
			LittleTileBox box = boundingBoxes.get(0);
			cornerVec = new LittleTileVec(box.minX, box.minY, box.minZ);
		}else
			cornerVec = new LittleTileVec(0, 0, 0);
	}
	
	public void place()
	{
		LittleTileBox box = new LittleTileBox(getSelectedBox());
		updateCorner();
		te.addTile(this);
		te.getWorldObj().playSoundEffect((double)((float)te.xCoord + 0.5F), (double)((float)te.yCoord + 0.5F), (double)((float)te.zCoord + 0.5F), getSound().func_150496_b(), (getSound().getVolume() + 1.0F) / 2.0F, getSound().getPitch() * 0.8F);
	}
	
	//================Destroying================
	
	public void onDestoryed(){}
	
	public void destroy()
	{
		if(isStructureBlock)
		{
			if(!te.getWorldObj().isRemote && isLoaded())
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
	
	public abstract void copyExtra(LittleTile tile);
	
	public void copyCore(LittleTile tile)
	{
		for (int i = 0; i < this.boundingBoxes.size(); i++) {
			tile.boundingBoxes.add(this.boundingBoxes.get(i).copy());
		}
		tile.cornerVec = this.cornerVec.copy();
		tile.te = this.te;
		
		tile.structure = this.structure;
		if(this.pos != null)
			tile.pos = this.pos.copy();
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
	
	@SideOnly(Side.CLIENT)
	public abstract ArrayList<CubeObject> getRenderingCubes();
	
	@SideOnly(Side.CLIENT)
	public void renderTick(double x, double y, double z) {}
	
	//================Sound================
	
	public abstract Block.SoundType getSound();
	
	//================Tick================
	
	public void updateEntity()
	{
		if(isStructureBlock && isMainBlock && structure.tilesToLoad != null)
		{
			structure.tiles = new ArrayList<LittleTile>();
			for (int i = 0; i < structure.tilesToLoad.size(); i++) {
				structure.checkForTile(te.getWorldObj(), structure.tilesToLoad.get(i));
			}
			int missingTiles = structure.tilesToLoad.size() - structure.tiles.size();
			if(missingTiles > 0)
				System.out.println("Couldn't load " + missingTiles + " tiles");
			structure.tilesToLoad = null;
		}
	}
	
	//================Block Event================
	
	public abstract IIcon getIcon(int side);
	
	public void randomDisplayTick(World world, int x, int y, int z, Random random) {}

	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float moveX, float moveY, float moveZ) {
		if(isLoaded())
			return structure.onBlockActivated(world, this, x, y, z, player, side, moveX, moveY, moveZ);
		return false;
	}

	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		return 0;
	}

	public double getEnchantPowerBonus(World world, int x, int y, int z) {
		return 0;
	}
	
	public boolean isLadder()
	{
		if(isLoaded())
			return structure.isLadder();
		return false;
	}
	
	//================Structure================
	
	public boolean isStructureBlock = false;
	
	public LittleStructure structure;
	public LittleTilePosition pos;
	public boolean isMainBlock = false;
	
	public boolean checkForStructure()
	{
		if(structure != null)
			return true;
		World world = te.getWorldObj();
		//if(!world.isRemote)
		//{
			Chunk chunk = world.getChunkFromBlockCoords(pos.coord.posX, pos.coord.posZ);
			if(!(chunk instanceof EmptyChunk))
			{
				TileEntity tileEntity = world.getTileEntity(pos.coord.posX, pos.coord.posY, pos.coord.posZ);
				if(tileEntity instanceof TileEntityLittleTiles)
				{
					LittleTile tile = ((TileEntityLittleTiles) tileEntity).getTile(pos.position);
					if(tile != null && tile.isStructureBlock)
					{
						if(tile.isMainBlock)
						{
							this.structure = tile.structure;
							if(this.structure != null && this.structure.tiles != null && !this.structure.tiles.contains(this))
								this.structure.tiles.add(this);
						}
					}
				}
				
				if(structure == null)
					te.removeTile(this);
				
				//pos = null;
				te.update();
				
				return structure != null;
			}
			
		//}
		return false;
	}
	
	public boolean isLoaded()
	{
		return isStructureBlock && checkForStructure();
	}
	
	public static class LittleTilePosition {
		
		public ChunkCoordinates coord;
		public LittleTileVec position;
		
		public LittleTilePosition(ChunkCoordinates coord, LittleTileVec position)
		{
			this.coord = coord;
			this.position = position;
		}
		
		public LittleTilePosition(String id, NBTTagCompound nbt)
		{
			coord = new ChunkCoordinates(nbt.getInteger(id + "coX"), nbt.getInteger(id + "coY"), nbt.getInteger(id + "coZ"));
			position = new LittleTileVec(id + "po", nbt);
		}
		
		public LittleTilePosition(NBTTagCompound nbt)
		{
			this("", nbt);
		}
		
		public void writeToNBT(String id, NBTTagCompound nbt)
		{
			nbt.setInteger(id + "coX", coord.posX);
			nbt.setInteger(id + "coY", coord.posY);
			nbt.setInteger(id + "coZ", coord.posZ);
			position.writeToNBT(id + "po", nbt);
		}
		
		public void writeToNBT(NBTTagCompound nbt)
		{
			writeToNBT("", nbt);
		}
		
		public LittleTilePosition copy()
		{
			return new LittleTilePosition(new ChunkCoordinates(coord), position.copy());
		}
		
	}
	
}
