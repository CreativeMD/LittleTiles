package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class LittleTile {
	
	private static HashMap<Class<? extends LittleTile>, String> tileIDs = new HashMap<Class<? extends LittleTile>, String>();
	
	public static final int minPos = -7;
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
	
	public LittleTile(Block block, int meta, LittleTileSize size)
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
	
	/**Should load the LittleTile**/
	public void load(NBTTagCompound nbt)
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
			nbt.setByte("sizeX", size.sizeX);
			nbt.setByte("sizeY", size.sizeY);
			nbt.setByte("sizeZ", size.sizeZ);
		}
		if(block == null || block instanceof BlockAir)
			setInValid();
	}
	
	/**Should save ALL data**/
	public void save(NBTTagCompound nbt)
	{
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
			size = new LittleTileSize((byte)(maxX - minX), (byte)(maxY - minY), (byte)(maxZ - minZ));
		}else{
			//For ItemStacks
			size = new LittleTileSize(nbt.getByte("sizeX"), nbt.getByte("sizeY"), nbt.getByte("sizeZ"));
		}
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
	
	/**All coordinates are going from -7 to 8**/
	public byte minX;
	/**All coordinates are going from -7 to 8**/
	public byte minY;
	/**All coordinates are going from -7 to 8**/
	public byte minZ;
	
	/**All coordinates are going from -7 to 8**/
	public byte maxX;
	/**All coordinates are going from -7 to 8**/
	public byte maxY;
	/**All coordinates are going from -7 to 8**/
	public byte maxZ;
	
	public LittleTileSize size;
	
	
	public AxisAlignedBB getBox()
	{
		return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	/**If this LittleTile can be split. Interesting for TileEntity blocks**/
	public boolean canSplit()
	{
		if(block instanceof ILittleTile)
			return ((ILittleTile) block).canSplit(this);
		return true;
	}
	
	public void updateEntity() {}
	
	/**The LittleTile is not placed yet! No reference to position, block, meta etc. are valid.*/
	public boolean PlaceLittleTile(ItemStack stack, TileEntityLittleTiles tileEntity, byte centerX, byte centerY, byte centerZ, byte sizeX, byte sizeY, byte sizeZ, ArrayList<LittleTile> splittedTiles)
	{
		byte minX = (byte) (centerX - (byte)(sizeX/2));
		byte tempminX = (byte) Math.max(minX, minPos);
		
		byte minY = (byte) (centerY - (byte)(sizeY/2));
		byte tempminY = (byte) Math.max(minY, minPos);
		
		byte minZ = (byte) (centerZ - (byte)(sizeZ/2));
		byte tempminZ = (byte) Math.max(minZ, minPos);
		
		byte maxX = (byte) (centerX + (sizeX - (centerX - minX)));
		byte tempmaxX = (byte) Math.min(maxX, maxPos);
		
		byte maxY = (byte) (centerY + (sizeY - (centerY - minY)));
		byte tempmaxY = (byte) Math.min(maxY, maxPos);
		
		byte maxZ = (byte) (centerZ + (sizeZ - (centerZ - minZ)));
		byte tempmaxZ = (byte) Math.min(maxZ, maxPos);
		
		AxisAlignedBB alignedBB = AxisAlignedBB.getBoundingBox(tempminZ, tempminY, tempminZ, tempmaxX, tempmaxY, tempmaxZ);
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
						newCenterX = (byte) (maxPos - (newSizeX - newSizeX/2));//TODO Check if this works out. Could create a new tile with an invalid pos
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
						newCenterX = (byte) (minPos + newSizeX/2);//TODO Check if this works out. Could create a new tile with an invalid pos
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
						newCenterY = (byte) (maxPos - (newSizeY - newSizeY/2));//TODO Check if this works out. Could create a new tile with an invalid pos
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
						newCenterY = (byte) (minPos + newSizeY/2);//TODO Check if this works out. Could create a new tile with an invalid pos
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
						newCenterZ = (byte) (maxPos - (newSizeZ - newSizeZ/2));//TODO Check if this works out. Could create a new tile with an invalid pos
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
						newCenterZ = (byte) (minPos + newSizeZ/2);//TODO Check if this works out. Could create a new tile with an invalid pos
					}
					break;
				default:
					break;
				}
				World world = tileEntity.getWorldObj();
				if(sizeX > 0 && canSplit()) //A part needs to be placed
				{
					//Try to place it, if not add it to the splittedTiles
					Block block = world.getBlock(coord.posX, coord.posY, coord.posZ);
					if(block instanceof BlockTile)
					{
						TileEntity tileEntity2 = world.getTileEntity(coord.posX, coord.posY, coord.posZ);
						if(tileEntity2 instanceof TileEntityLittleTiles)
							PlaceLittleTile(stack, (TileEntityLittleTiles) tileEntity2, newCenterX, newCenterY, newCenterZ, newSizeX, newSizeY, newSizeZ, splittedTiles);
					}else if(block.isReplaceable(world, coord.posX, coord.posY, coord.posZ))
					{
						world.setBlock(coord.posX, coord.posY, coord.posZ, LittleTiles.blockTile);
						TileEntityLittleTiles littleTilesEntity = new TileEntityLittleTiles();
						world.setTileEntity(coord.posX, coord.posY, coord.posZ, littleTilesEntity);
						if(PlaceLittleTile(stack, (TileEntityLittleTiles) littleTilesEntity, newCenterX, newCenterY, newCenterZ, newSizeX, newSizeY, newSizeZ, splittedTiles))
							world.setBlockToAir(coord.posX, coord.posY, coord.posZ);
					}else{
						splittedTiles.add(new LittleTile(block, meta, new LittleTileSize(newSizeX, newSizeY, newSizeZ)));
					}
				}else if(canSplit())
					return false;
			}
			return true;
		}
		return false;
	}
	
	/**Is used for drop and creating LittleTile ItemStacks**/
	public ItemStack getItemStack(boolean isDrop)
	{
		if(isInValid)
			return null;
		ItemStack stack = new ItemStack(block, 1, meta);
		stack.stackTagCompound = new NBTTagCompound();
		if(isDrop)
			isPlaced = true;
		save(stack.stackTagCompound);
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
	
	/**NOTE: Max size is 16x16x16 and min size is 1x1x1**/
	public static class LittleTileSize{
		
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
		
		public LittleTileSize(int sizeX, int sizeY, int sizeZ)
		{
			this((byte)sizeX, (byte)sizeY, (byte)sizeZ);
		}
		
		public LittleTileSize(byte sizeX, byte sizeY, byte sizeZ)
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
	}
}
