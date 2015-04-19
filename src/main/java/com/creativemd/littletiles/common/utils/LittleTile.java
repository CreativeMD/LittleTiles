package com.creativemd.littletiles.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class LittleTile {
	
	private static HashMap<Class<? extends LittleTile>, String> tileIDs = new HashMap<Class<? extends LittleTile>, String>();
	
	public static final int minPos = -8;
	public static final int maxPos = 8;
	
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
	
	public LittleTile(Block block, int meta, LittleTileVec size)
	{
		this();
		this.block = block;
		this.meta = meta;
		this.size = size;
	}
	
	/**Every LittleTile class has to have this constructor implemented**/
	public LittleTile()
	{
		isPlaced = false;
	}
	
	/**All information the client needs*/
	public void sendToClient(NBTTagCompound nbt)
	{
		saveCore(nbt);
	}
	
	/**Should apply all information from sendToCLient**/
	@SideOnly(Side.CLIENT)
	public void recieveFromServer(NetworkManager net, NBTTagCompound nbt)
	{
		loadCore(nbt);
	}
	
	public static LittleTile CreateandLoadTile(NBTTagCompound nbt)
	{
		return CreateandLoadTile(nbt, false, null);
	}
	
	public static LittleTile CreateandLoadTile(NBTTagCompound nbt, boolean isPacket, NetworkManager net)
	{
		String id = nbt.getString("tileID");
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
				tile.recieveFromServer(net, nbt);
			else
				tile.load(nbt);
		return tile;		
	}
	
	private void loadCore(NBTTagCompound nbt)
	{
		block = Block.getBlockFromName(nbt.getString("block"));
		meta = nbt.getInteger("meta");
		isPlaced = nbt.getBoolean("placed");
		if(isPlaced)
		{
			minX = nbt.getByte("ix");
			minY = nbt.getByte("iy");
			minZ = nbt.getByte("iz");
			maxX = nbt.getByte("ax");
			maxY = nbt.getByte("ay");
			maxZ = nbt.getByte("az");
			nbt.removeTag("sizeX");
			nbt.removeTag("sizeY");
			nbt.removeTag("sizeZ");
		}else{
			size = new LittleTileVec(nbt.getByte("sizeX"), nbt.getByte("sizeY"), nbt.getByte("sizeZ"));
		}
		if(block == null || block instanceof BlockAir)
			setInValid();
	}
	
	private void saveCore(NBTTagCompound nbt)
	{
		nbt.setString("tileID", getIDByClass(this.getClass()));
		nbt.setString("block", Block.blockRegistry.getNameForObject(block));
		nbt.setInteger("meta", meta);
		if(isPlaced)
			nbt.setBoolean("placed", isPlaced);
		else
			nbt.removeTag("placed");
		if(isPlaced)
		{
			nbt.setByte("ix", minX);
			nbt.setByte("iy", minY);
			nbt.setByte("iz", minZ);
			nbt.setByte("ax", maxX);
			nbt.setByte("ay", maxY);
			nbt.setByte("az", maxZ);
			size = new LittleTileVec((byte)(maxX - minX), (byte)(maxY - minY), (byte)(maxZ - minZ));
		}else{
			if(size == null)
				size = new LittleTileVec((byte)(maxX - minX), (byte)(maxY - minY), (byte)(maxZ - minZ));
			nbt.setByte("sizeX", size.sizeX);
			nbt.setByte("sizeY", size.sizeY);
			nbt.setByte("sizeZ", size.sizeZ);
		}
	}
	
	/**Should load the LittleTile**/
	public void load(NBTTagCompound nbt)
	{
		loadCore(nbt);
	}
	
	/**Should save ALL data**/
	public void save(NBTTagCompound nbt)
	{
		saveCore(nbt);
	}
	
	private boolean isInValid = false;
	
	public boolean isValid()
	{
		return !isInValid;
	}
	
	public void setInValid()
	{
		isInValid = true;
	}
	
	public boolean isPlaced;
	
	public Block block;
	public int meta;
	
	/**All coordinates are going from -8 to 8**/
	public byte minX;
	/**All coordinates are going from -8 to 8**/
	public byte minY;
	/**All coordinates are going from -8 to 8**/
	public byte minZ;
	
	/**All coordinates are going from -8 to 8**/
	public byte maxX;
	/**All coordinates are going from -8 to 8**/
	public byte maxY;
	/**All coordinates are going from -8 to 8**/
	public byte maxZ;
	
	public LittleTileVec size;
	
	public AxisAlignedBB getCoordBox(int x, int y, int z)
	{
		AxisAlignedBB box = getBox();
		box.minX += x;
		box.minY += y;
		box.minZ += z;
		box.maxX += x;
		box.maxY += y;
		box.maxZ += z;
		return box;
	}
	
	public AxisAlignedBB getLittleBox()
	{
		return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	public AxisAlignedBB getBox()
	{
		double minX = (double)(this.minX+8)/16D;
		double minY = (double)(this.minY+8)/16D;
		double minZ = (double)(this.minZ+8)/16D;
		double maxX = (double)(this.maxX+8)/16D;
		double maxY = (double)(this.maxY+8)/16D;
		double maxZ = (double)(this.maxZ+8)/16D;
		return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	/**If this LittleTile can be split. Interesting for TileEntity blocks**/
	public boolean canSplit()
	{
		if(block instanceof ILittleTile)
			return ((ILittleTile) block).canSplit(this);
		return true;
	}
	
	/*public boolean hasFixedSize()
	{
		if(block instanceof ILittleTile)
			return ((ILittleTile) block).getSize();
		return true;
	}*/
	
	public void updateEntity() {}
	
	/**The LittleTile is not placed yet! No reference to position, block, meta etc. are valid.*/
	public boolean PlaceLittleTile(ItemStack stack, TileEntityLittleTiles tileEntity, byte centerX, byte centerY, byte centerZ, byte sizeX, byte sizeY, byte sizeZ, ArrayList<LittleTile> splittedTiles)
	{
		byte minX = (byte) (centerX - (int)(sizeX/2D));
		byte tempminX = (byte) Math.max(minX, minPos);
		
		byte minY = (byte) (centerY - (int)(sizeY/2D));
		byte tempminY = (byte) Math.max(minY, minPos);
		
		byte minZ = (byte) (centerZ - (int)(sizeZ/2D));
		byte tempminZ = (byte) Math.max(minZ, minPos);
		
		byte maxX = (byte) (centerX + (sizeX - (centerX - minX)));
		byte tempmaxX = (byte) Math.min(maxX, maxPos);
		
		byte maxY = (byte) (centerY + (sizeY - (centerY - minY)));
		byte tempmaxY = (byte) Math.min(maxY, maxPos);
		
		byte maxZ = (byte) (centerZ + (sizeZ - (centerZ - minZ)));
		byte tempmaxZ = (byte) Math.min(maxZ, maxPos);
		
		/*byte maxX = (byte) (centerX + (byte)(sizeX/2D));
		//byte tempmaxX = (byte) Math.min(maxX, maxPos);
		
		//byte maxY = (byte) (centerY + (byte)(sizeY/2D));
		//byte tempmaxY = (byte) Math.min(maxY, maxPos);
		
		//byte maxZ = (byte) (centerZ + (byte)(sizeZ/2D));
		//byte tempmaxZ = (byte) Math.min(maxZ, maxPos);
		
		//byte minX = (byte) (centerX - (sizeX - (maxX - centerX)));
		//byte tempminX = (byte) Math.max(minX, minPos);
		
		//byte minY = (byte) (centerY - (sizeY - (maxY - centerY)));
		//byte tempminY = (byte) Math.max(minY, minPos);
		
		//byte minZ = (byte) (centerZ - (sizeZ - (maxZ - centerZ)));
		//byte tempminZ = (byte) Math.max(minZ, minPos);*/
		
		AxisAlignedBB alignedBB = AxisAlignedBB.getBoundingBox(tempminX, tempminY, tempminZ, tempmaxX, tempmaxY, tempmaxZ);
		if(tileEntity.isSpaceForLittleTile(alignedBB))
		{
			for (int i = 0; i < 6; i++) {
				ForgeDirection partDirection = ForgeDirection.getOrientation(i);
				ChunkCoordinates coord = new ChunkCoordinates(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
				byte newSizeX = 0;
				byte newSizeY = 0;
				byte newSizeZ = 0;
				byte newCenterX = 0;
				byte newCenterY = 0;
				byte newCenterZ = 0;
				
				//Yes this is not the most aesthetic way to do this, but yes :D
				switch(partDirection)
				{
				case WEST:
					if(minX < tempminX)
					{
						coord.posX--;
						newSizeX = (byte) (tempminX - minX);
						newSizeY = sizeY;
						newSizeZ = sizeZ;
						newCenterX = (byte) (maxPos - (newSizeX - newSizeX/2));
						newCenterY = centerY;
						newCenterZ = centerZ;
					}
					break;
				case EAST:
					if(maxX > tempmaxX)
					{
						coord.posX++;
						newSizeX = (byte) (maxX - tempmaxX);
						newSizeY = sizeY;
						newSizeZ = sizeZ;
						newCenterX = (byte) (minPos + newSizeX/2);
						newCenterY = centerY;
						newCenterZ = centerZ;
					}
					break;
				case DOWN:
					if(minY < tempminY)
					{
						coord.posY--;
						newSizeX = sizeX;
						newSizeY = (byte) (tempminY - minY);
						newSizeZ = sizeZ;
						newCenterX = centerX;
						newCenterY = (byte) (maxPos - (newSizeY - newSizeY/2));
						newCenterZ = centerZ;
					}
					break;
				case UP:
					if(maxY > tempmaxY)
					{
						coord.posY++;
						newSizeX = sizeX;
						newSizeY = (byte) (maxY - tempmaxY);
						newSizeZ = sizeZ;
						newCenterX = centerX;
						newCenterY = (byte) (minPos + newSizeY/2);
						newCenterZ = centerZ;
					}
					break;
				case NORTH:
					if(minZ < tempminZ)
					{
						coord.posZ--;
						newSizeX = sizeX;
						newSizeY = sizeY;
						newSizeZ = (byte) (tempminZ - minZ);
						newCenterX = centerX;
						newCenterY = centerY;
						newCenterZ = (byte) (maxPos - (newSizeZ - newSizeZ/2));
					}
					break;
				case SOUTH:
					if(maxZ > tempmaxZ)
					{
						coord.posZ++;
						newSizeX = sizeX;
						newSizeY = sizeY;
						newSizeZ = (byte) (maxZ - tempmaxZ);
						newCenterX = centerX;
						newCenterY = centerY;
						newCenterZ = (byte) (minPos + newSizeZ/2);
					}
					break;
				default:
					break;
				}
				World world = tileEntity.getWorldObj();
				if(newSizeX > 0 && canSplit()) //A part needs to be placed
				{
					//Try to place it, if not add it to the splittedTiles
					Block block = world.getBlock(coord.posX, coord.posY, coord.posZ);
					if(block instanceof BlockTile)
					{
						TileEntity tileEntity2 = world.getTileEntity(coord.posX, coord.posY, coord.posZ);
						if(tileEntity2 instanceof TileEntityLittleTiles)
							if(!PlaceLittleTile(stack, (TileEntityLittleTiles) tileEntity2, newCenterX, newCenterY, newCenterZ, newSizeX, newSizeY, newSizeZ, splittedTiles))
								splittedTiles.add(new LittleTile(this.block, meta, new LittleTileVec(newSizeX, newSizeY, newSizeZ)));
							
					}else if(block.isReplaceable(world, coord.posX, coord.posY, coord.posZ))
					{
						world.setBlock(coord.posX, coord.posY, coord.posZ, LittleTiles.blockTile);
						TileEntityLittleTiles littleTilesEntity = new TileEntityLittleTiles();
						world.setTileEntity(coord.posX, coord.posY, coord.posZ, littleTilesEntity);
						if(!PlaceLittleTile(stack, (TileEntityLittleTiles) littleTilesEntity, newCenterX, newCenterY, newCenterZ, newSizeX, newSizeY, newSizeZ, splittedTiles))
						{
							world.setBlockToAir(coord.posX, coord.posY, coord.posZ);
							splittedTiles.add(new LittleTile(this.block, meta, new LittleTileVec(newSizeX, newSizeY, newSizeZ)));
						}
					}else{
						splittedTiles.add(new LittleTile(this.block, meta, new LittleTileVec(newSizeX, newSizeY, newSizeZ)));
					}
				}else if(newSizeX > 0 && !canSplit())
					return false;
			}
			LittleTile placed = this.copy();
			placed.size = new LittleTileVec(tempmaxX-tempminX, tempmaxY-tempminY, tempmaxZ-tempminZ);
			placed.minX = tempminX;
			placed.minY = tempminY;
			placed.minZ = tempminZ;
			placed.maxX = tempmaxX;
			placed.maxY = tempmaxY;
			placed.maxZ = tempmaxZ;
			placed.isPlaced = true;
			tileEntity.tiles.add(placed);
			tileEntity.update();
			return true;
		}
		return false;
	}
	
	/**This method has to be overriden by any class. Use copyCore as a refernece to load all core stuff**/
	public LittleTile copy()
	{
		LittleTile tile = new LittleTile(block, meta, size.copy());
		copyCore(tile);
		return tile;
	}
	
	protected void copyCore(LittleTile tile)
	{
		tile.isInValid = isInValid;
		tile.isPlaced = isPlaced;
		tile.minX = minX;
		tile.minY = minY;
		tile.minZ = minZ;
		tile.maxX = maxX;
		tile.maxY = maxY;
		tile.maxZ = maxZ;
	}
	
	/**Is used for drop and creating LittleTile ItemStacks**/
	public ArrayList<ItemStack> getDrops()
	{
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		stacks.add(getItemStack());
		return stacks;
	}
	
	public ItemStack getItemStack()
	{
		if(isInValid)
			return null;
		ItemStack stack = new ItemStack(LittleTiles.blockTile);
		stack.stackTagCompound = new NBTTagCompound();
		
		//if(isDrop)
			//isPlaced = false;
		
		boolean tempPlace = isPlaced;
		isPlaced = false;
		save(stack.stackTagCompound);
		isPlaced = tempPlace;
		return stack;
	}
	
	public void onPlaced(ItemStack stack, TileEntityLittleTiles tileEntity)
	{
		isPlaced = true;
		block.onBlockAdded(tileEntity.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
	}
	
	public void onRemoved(TileEntityLittleTiles tileEntity)
	{
		tileEntity.tiles.remove(this);
	}
	
	//public LittleTile 
	
	/**NOTE: Max size is 16x16x16 and min size is 1x1x1**/
	public static class LittleTileVec{
		
		public byte sizeX;
		public byte sizeY;
		public byte sizeZ;
		
		public float getVolume()
		{
			return sizeX * sizeY * sizeZ;
		}
		
		/**Returns how the volume in percent to a size of a normal block*/
		public float getPercentVolume()
		{
			return getVolume() / (16*16*16);
		}
		
		public LittleTileVec(int sizeX, int sizeY, int sizeZ)
		{
			this((byte)sizeX, (byte)sizeY, (byte)sizeZ);
		}
		
		public double getPosX()
		{
			return (double)sizeX/16D;
		}
		
		public double getPosY()
		{
			return (double)sizeY/16D;
		}
		
		public double getPosZ()
		{
			return (double)sizeZ/16D;
		}
		
		public void rotateVec(ForgeDirection direction)
		{
			switch(direction)
			{
			case UP:
			case DOWN:
				byte tempY = sizeY;
				sizeY = sizeX;
				sizeX = tempY;
				break;
			case SOUTH:
			case NORTH:
				byte tempZ = sizeZ;
				sizeZ = sizeX;
				sizeX = tempZ;
				break;
			default:
				break;
			}
		}
		
		public LittleTileVec(byte sizeX, byte sizeY, byte sizeZ)
		{
			if(sizeX < 1)
				sizeX = 1;
			if(sizeX > 16)
				sizeX = 16;
			this.sizeX = sizeX;
			if(sizeY < 1)
				sizeY = 1;
			if(sizeY > 16)
				sizeY = 16;
			this.sizeY = sizeY;
			if(sizeZ < 1)
				sizeZ = 1;
			if(sizeZ > 16)
				sizeZ = 16;
			this.sizeZ = sizeZ;
		}
		
		public LittleTileVec copy()
		{
			return new LittleTileVec(sizeX, sizeY, sizeZ);
		}
	}
}
