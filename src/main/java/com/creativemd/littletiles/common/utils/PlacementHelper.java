package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileSize;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;

/**This class does all caculate on where to place a block. Used for rendering preview and placing**/
public class PlacementHelper {
	
	public MovingObjectPosition moving;
	public EntityPlayer player;
	public ArrayList<LittleTile> tiles;
	public ArrayList<LittleTilePreview> preview;
	public LittleTileSize size;
	public Vec3 centeroffset;
	public ForgeDirection direction;
	public int posX;
	public int posY;
	public int posZ;
	public int side;
	
	public static boolean isLittleBlock(ItemStack stack)
	{
		if(stack == null)
			return false;
		if(stack.getItem() instanceof ILittleTile)
			return ((ILittleTile) stack.getItem()).getLittlePreview(stack) != null;
		if(Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile)
			return ((ILittleTile)Block.getBlockFromItem(stack.getItem())).getLittlePreview(stack) != null;
		return false;
	}
	
	public PlacementHelper(EntityPlayer player, int x, int y, int z)
	{
		this(null, player, x, y, z, false);
	}
	
	/*public static MovingObjectPosition rayTrace(EntityPlayer player)
	{
		double distance = getBlockReachDistance(player);
		Vec3 vec3 = player.getPosition(1);
        Vec3 vec31 = player.getLook(1);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * distance, vec31.yCoord * distance, vec31.zCoord * distance);
        return player.worldObj.func_147447_a(vec3, vec32, false, false, true);
	}*/
	
	public static float getBlockReachDistance(EntityPlayer player)
    {
        return player.capabilities.isCreativeMode ? 5.0F : 4.5F;
    }
	
	public PlacementHelper(MovingObjectPosition moving, EntityPlayer player)
	{
		this(moving, player, 0, 0, 0, true);
	}
	
	public PlacementHelper(MovingObjectPosition moving, EntityPlayer player, int x, int y, int z, boolean isPreview)
	{
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.moving = moving;
		this.player = player;
		ItemStack stack = player.getHeldItem();
		
		if(moving != null)
		{
			this.direction = ForgeDirection.getOrientation(moving.sideHit);
			this.posX = moving.blockX;
			this.posY = moving.blockY;
			this.posZ = moving.blockZ;
			TileEntity entity = player.worldObj.getTileEntity(posX, posY, posZ);
			if(entity instanceof TileEntityLittleTiles)
			{
				MovingObjectPosition newmoving = ((TileEntityLittleTiles) entity).getMoving(player);
				if(newmoving != null)
				{
					this.moving = newmoving;
					this.moving.blockX = posX;
					this.moving.blockY = posY;
					this.moving.blockZ = posZ;
					this.direction = ForgeDirection.getOrientation(this.moving.sideHit);
				}
			}
		}
		if(stack != null)
		{
			if(isPreview)
			{
				LittleTilePreview tempPreview = null;
				if(stack.getItem() instanceof ILittleTile)
				{
					tempPreview = ((ILittleTile)stack.getItem()).getLittlePreview(stack);
				}else if(Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile){
					tempPreview = ((ILittleTile)Block.getBlockFromItem(stack.getItem())).getLittlePreview(stack);
				}
				if(tempPreview != null)
				{
					this.size = tempPreview.size;
					this.preview = tempPreview.getAllTiles();
				}
			}else{
				this.tiles = new ArrayList<LittleTile>();
				if(stack.getItem() instanceof ILittleTile)
				{
					this.tiles.addAll(((ILittleTile)stack.getItem()).getLittleTile(stack, player.getEntityWorld(), posX, posY, posZ));
					this.size = tiles.get(0).size;
				}else if(Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile){
					this.tiles.addAll(((ILittleTile)Block.getBlockFromItem(stack.getItem())).getLittleTile(stack, player.getEntityWorld(), posX, posY, posZ));
					this.size = tiles.get(0).size;
				}
			}
		}
		if(moving != null)
		{
			side = this.moving.sideHit;
			if(!isSingle()) //Calculate box offset for multitiles
			{
				calculateOffset(size);
			}
		}
	}
	
	public void calculateOffset(LittleTileSize size)
	{
		byte minX = LittleTile.maxPos;
		byte minY = LittleTile.maxPos;
		byte minZ = LittleTile.maxPos;
		byte maxX = LittleTile.minPos;
		byte maxY = LittleTile.minPos;
		byte maxZ = LittleTile.minPos;
		if(tiles == null)
		{
			for (int i = 0; i < preview.size(); i++) {
				LittleTilePreview tile = preview.get(i);
				minX = (byte) Math.min(minX, tile.min.posX);
				minY = (byte) Math.min(minY, tile.min.posY);
				minZ = (byte) Math.min(minZ, tile.min.posZ);
				maxX = (byte) Math.max(maxX, tile.max.posX);
				maxY = (byte) Math.max(maxY, tile.max.posY);
				maxZ = (byte) Math.max(maxZ, tile.max.posZ);
			}
		}else{
			for (int i = 0; i < tiles.size(); i++) {
				LittleTile tile = tiles.get(i);
				minX = (byte) Math.min(minX, tile.minX);
				minY = (byte) Math.min(minY, tile.minY);
				minZ = (byte) Math.min(minZ, tile.minZ);
				maxX = (byte) Math.max(maxX, tile.maxX);
				maxY = (byte) Math.max(maxY, tile.maxY);
				maxZ = (byte) Math.max(maxZ, tile.maxZ);
			}
		}
		double offsetX = (minX+maxX)/2D;
		double offsetY = (minY+maxY)/2D;
		double offsetZ = (minZ+maxZ)/2D;
		
		centeroffset = Vec3.createVectorHelper(offsetX/16D, offsetY/16D, offsetZ/16D);
	}
	
	public boolean canBePlacedInside()
	{
		return canBePlacedInside(posX, posY, posZ);
	}
	
	public boolean canBePlacedInside(int x, int y, int z)
	{
		TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles)
			return true;
		//TODO Add function
		/*
		 * if (block == Blocks.snow_layer && (world.getBlockMetadata(x, y, z) & 7) < 1)
        {
            p_77648_7_ = 1;
        }
        else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(p_77648_3_, p_77648_4_, p_77648_5_, p_77648_6_))
        {
        	
        } TODO Add also this vanilla method
		 */
		return false;
	}
	
	public boolean isSingle()
	{
		if(tiles == null)
			return preview.size() == 1;
		return tiles.size() == 1;
	}
	
	public LittleTileSize getRelativeSize()
	{
		LittleTileSize vec = this.size.copy();
		vec.rotateSize(direction);
		return vec;
	}
	
	public void rotateTiles(ForgeDirection direction)
	{
		if(!isSingle())
		{
			if(tiles == null)
			{
				for (int i = 0; i < preview.size(); i++) {
					LittleTilePreview tile = preview.get(i);
					CubeObject box = new CubeObject(tile.getLittleBox());
					box = CubeObject.rotateCube(box, direction);
					tile.min.posX = (byte) box.minX;
					tile.min.posY = (byte) box.minY;
					tile.min.posZ = (byte) box.minZ;
					tile.max.posX = (byte) box.maxX;
					tile.max.posY = (byte) box.maxY;
					tile.max.posZ = (byte) box.maxZ;
					tile.updateSize();
				}
			}else{
				for (int i = 0; i < tiles.size(); i++) {
					LittleTile tile = tiles.get(i);
					CubeObject box = new CubeObject(tile.getLittleBox());
					box = CubeObject.rotateCube(box, direction);
					tile.minX = (byte) box.minX;
					tile.minY = (byte) box.minY;
					tile.minZ = (byte) box.minZ;
					tile.maxX = (byte) box.maxX;
					tile.maxY = (byte) box.maxY;
					tile.maxZ = (byte) box.maxZ;
					tile.updateSize();
				}
			}
		}
	}
	
	public Vec3 getOffset(int i, LittleTileSize size)
	{
		if(!isSingle())
		{
			calculateOffset(size);
			
			if(tiles == null)
			{
				LittleTilePreview tile = preview.get(i);
				double x = ((tile.min.posX+tile.max.posX)/2D)/16D-centeroffset.xCoord;
				double y = ((tile.min.posY+tile.max.posY)/2D)/16D-centeroffset.yCoord;
				double z = ((tile.min.posZ+tile.max.posZ)/2D)/16D-centeroffset.zCoord;
				return Vec3.createVectorHelper(x, y, z);
			}else{
				LittleTile tile = tiles.get(i);
				double x = ((tile.minX+tile.maxX)/2D)/16D-centeroffset.xCoord;
				double y = ((tile.minY+tile.maxY)/2D)/16D-centeroffset.yCoord;
				double z = ((tile.minZ+tile.maxZ)/2D)/16D-centeroffset.zCoord;
				return Vec3.createVectorHelper(x, y, z);
			}
		}
		return Vec3.createVectorHelper(0, 0, 0);
	}
	
	public Vec3 getCenterPos(LittleTileSize size)
	{
		double x = posX;
		double y = posY;
		double z = posZ;
		
		switch(direction)
        {
		case EAST:
			x += size.getPosX()/2;
		case WEST:
			if(direction == ForgeDirection.WEST)
				x -= size.getPosX()/2;
			y += size.getPosY()/(size.sizeY*2D);
			if(size.sizeY%2 == 0)
				y += 1D/32D;
			z += size.getPosZ()/(size.sizeZ*2D);
			if(size.sizeZ%2 == 0)
				z += 1D/32D;
			break;
		case UP:
			y += size.getPosY()/2;
		case DOWN:
			if(direction == ForgeDirection.DOWN)
				y -= size.getPosY()/2;
			x += size.getPosX()/(size.sizeX*2D);
			if(size.sizeX%2 == 0)
				x += 1D/32D;
			z += size.getPosZ()/(size.sizeZ*2D);
			if(size.sizeZ%2 == 0)
				z += 1D/32D;
			break;
		case SOUTH:
			z += size.getPosZ()/2;
		case NORTH:
			if(direction == ForgeDirection.NORTH)
				z -= size.getPosZ()/2;
			x += size.getPosX()/(size.sizeX*2D);
			if(size.sizeX%2 == 0)
				x += 1D/32D;
			y += size.getPosY()/(size.sizeY*2D);
			if(size.sizeY%2 == 0)
				y += 1D/32D;
			break;
		default:
			break;
        }
		
		Vec3 center = Vec3.createVectorHelper(x, y, z);
		Vec3 look = getLookingPos();
		center = center.addVector(look.xCoord, look.yCoord, look.zCoord);
		return center;
	}
	
	public Vec3 getLookingPos()
	{
		double posX = moving.hitVec.xCoord - moving.blockX;
		double posY = moving.hitVec.yCoord - moving.blockY;
		double posZ = moving.hitVec.zCoord - moving.blockZ;
		//int temp = (int)(posX*16);
		posX = ((int)(posX*16))/16D;
		posY = ((int)(posY*16))/16D;
		posZ = ((int)(posZ*16))/16D;
		//posX = posX*16-8;
		//posY = posY*16-8;
		//posZ = posZ*16-8;
		if(!canBePlacedInside())
		{
			switch(ForgeDirection.getOrientation(moving.sideHit))
			{
			case EAST:
				posX = 1;
				break;
			case WEST:
				posX = 0;
				break;
			case UP:
				posY = 1;
				break;
			case DOWN:
				posY = 0;
				break;
			case SOUTH:
				posZ = 1;
				break;
			case NORTH:
				posZ = 0;
				break;
			default:
				break;
			
			}
		}
		return Vec3.createVectorHelper(posX, posY, posZ);
	}
	
}
