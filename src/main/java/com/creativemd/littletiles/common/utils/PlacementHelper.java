package com.creativemd.littletiles.common.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;

/**This class does all caculate on where to place a block. Used for rendering preview and placing**/
public class PlacementHelper {
	
	public MovingObjectPosition moving;
	public EntityPlayer player;
	public LittleTile tile;
	public ForgeDirection direction;
	public int posX;
	public int posY;
	public int posZ;
	
	
	public PlacementHelper(EntityPlayer player)
	{
		this(player.rayTrace(getBlockReachDistance(player), 1.0F), player);
	}
	
	public static float getBlockReachDistance(EntityPlayer player)
    {
        return player.capabilities.isCreativeMode ? 5.0F : 4.5F;
    }
	
	public PlacementHelper(MovingObjectPosition moving, EntityPlayer player)
	{
		this.moving = moving;
		this.player = player;
		this.tile = ItemBlockTiles.getLittleTile(player.getHeldItem());
		this.direction = ForgeDirection.getOrientation(moving.sideHit);
		this.posX = moving.blockX;
		this.posY = moving.blockY;
		this.posZ = moving.blockZ;
	}
	
	public boolean canBePlacedInside()
	{
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
	
	public LittleTileVec getRelativeSize()
	{
		LittleTileVec vec = new LittleTileVec(tile.size.sizeX, tile.size.sizeY, tile.size.sizeZ);
		vec.rotateVec(direction);
		return vec;
	}
	
	public Vec3 getCenterPos()
	{
		double x = posX;
		double y = posY;
		double z = posZ;
		
		LittleTileVec size = getRelativeSize();
		
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
			break;
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
		//posX = posX*16-7;
		//posY = posY*16-7;
		//posZ = posZ*16-7;
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
