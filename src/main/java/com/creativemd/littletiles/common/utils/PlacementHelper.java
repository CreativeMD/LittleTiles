package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import scala.tools.nsc.backend.icode.Primitives.Shift;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.HashMapList;
import com.creativemd.littletiles.utils.InsideShiftHandler;
import com.creativemd.littletiles.utils.ShiftHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**This class does all caculate on where to place a block. Used for rendering preview and placing**/
public class PlacementHelper {
	
	private static PlacementHelper instance;
	
	public static PlacementHelper getInstance(EntityPlayer player)
	{
		if(instance == null)
			instance = new PlacementHelper(player);
		else
			instance.player = player;
		return instance;
	}
	
	public EntityPlayer player;
	public World world; 
	
	public PlacementHelper(EntityPlayer player)
	{
		this.player = player;
		this.world = player.worldObj;
	}
	
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
	
	public ArrayList<PreviewTile> getPreviewTiles(ItemStack stack, MovingObjectPosition moving, ForgeDirection rotation, ForgeDirection rotation2)
	{
		return getPreviewTiles(stack, moving.blockX, moving.blockY, moving.blockZ, player.getPosition(1.0F), moving.hitVec, ForgeDirection.getOrientation(moving.sideHit), rotation, rotation2);
	}
	
	public static LittleTileSize getSize(ArrayList<LittleTilePreview> tiles, ItemStack stack)
	{
		byte minX = LittleTile.maxPos;
		byte minY = LittleTile.maxPos;
		byte minZ = LittleTile.maxPos;
		byte maxX = LittleTile.minPos;
		byte maxY = LittleTile.minPos;
		byte maxZ = LittleTile.minPos;
		LittleTileSize size = new LittleTileSize(0, 0, 0);
		for (int i = 0; i < tiles.size(); i++) {
			LittleTilePreview tile = tiles.get(i);
			if(tile == null)
				return new LittleTileSize(0, 0, 0);
			if(tile.box != null)
			{
				minX = (byte) Math.min(minX, tile.box.minX);
				minY = (byte) Math.min(minY, tile.box.minY);
				minZ = (byte) Math.min(minZ, tile.box.minZ);
				maxX = (byte) Math.max(maxX, tile.box.maxX);
				maxY = (byte) Math.max(maxY, tile.box.maxY);
				maxZ = (byte) Math.max(maxZ, tile.box.maxZ);
			}else{
				size.max(tile.size);
			}
		}
		return new LittleTileSize(maxX-minX, maxY-minY, maxZ-minZ).max(size);
	}
	
	public ArrayList<PreviewTile> getPreviewTiles(ItemStack stack, int x, int y, int z, Vec3 playerPos, Vec3 hitVec, ForgeDirection side, ForgeDirection rotation, ForgeDirection rotation2)
	{
		ArrayList<ShiftHandler> shifthandlers = new ArrayList<ShiftHandler>();
		ArrayList<PreviewTile> preview = new ArrayList<PreviewTile>();
		ArrayList<LittleTilePreview> tiles = null;
		
		LittleTilePreview tempPreview = null;
		if(stack.getItem() instanceof ILittleTile)
		{
			tiles = ((ILittleTile)stack.getItem()).getLittlePreview(stack);
		}else if(Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile){
			tiles = ((ILittleTile)Block.getBlockFromItem(stack.getItem())).getLittlePreview(stack);
		}
		
		if(tiles != null)
		{
			LittleTileSize size = getSize(tiles, stack);
			
			size.rotateSize(rotation);
			size.rotateSize(rotation2);
			
			//size.rotateSize(side);
			
			
			if(tiles.size() == 1)
				shifthandlers.addAll(tiles.get(0).shifthandlers);
			
			shifthandlers.add(new InsideShiftHandler());
			
			LittleTileBox box = getTilesBox(size, hitVec, x, y, z, side);
						
			if(player.isSneaking())
			{
				if(!canBePlacedInside(x, y, z, hitVec, side))
				{
					switch(side)
					{
					case EAST:
						x++;
						break;
					case WEST:
						x--;
						break;
					case UP:
						y++;
						break;
					case DOWN:
						y--;
						break;
					case SOUTH:
						z++;
						break;
					case NORTH:
						z--;
						break;
					default:
						break;
					}
				}
				
				for (int i = 0; i < shifthandlers.size(); i++) {
					shifthandlers.get(i).init(world, x, y, z);
				}
				
				LittleTileVec hit = getHitVec(hitVec, x, y, z, side);
				ShiftHandler handler = null;
				double distance = 2;
				for (int i = 0; i < shifthandlers.size(); i++) {
					double tempDistance = shifthandlers.get(i).getDistance(hit);
					if(tempDistance < distance)
					{
						distance = tempDistance;
						handler = shifthandlers.get(i);
					}
				}
				
				if(handler != null)
				{
					box = handler.getNewPosition(world, x, y, z, box);
				}
			}
			
			for (int i = 0; i < tiles.size(); i++) {
				LittleTilePreview tile = tiles.get(i);
				if(tile.box == null)
				{
					preview.add(new PreviewTile(box.copy(), tile));
				}else{
					if(!player.isSneaking())
						tile.box.addOffset(box.getMinVec());
					tile.box.rotateBox(rotation);
					tile.box.rotateBox(rotation2);
					preview.add(new PreviewTile(tile.box, tile));
				}
			}
		}
		
		return preview;
	}
	
	public LittleTileBox getTilesBox(LittleTileSize size, Vec3 hitVec, int x, int y, int z, ForgeDirection side)
	{
		LittleTileVec hit = getHitVec(hitVec, x, y, z, side);
		LittleTileVec center = size.calculateCenter();
		LittleTileVec centerInv = size.calculateInvertedCenter();
		//hit.addVec(center);
		//Make hit the center of the Box
		switch(side)
		{
		case EAST:
			hit.x += center.x;
			break;
		case WEST:
			hit.x -= centerInv.x;
			break;
		case UP:
			hit.y += center.y;
			break;
		case DOWN:
			hit.y -= centerInv.y;
			break;
		case SOUTH:
			hit.z += center.z;
			break;
		case NORTH:
			hit.z -= centerInv.z;
			break;
		default:
			break;
		}
		return new LittleTileBox(hit, size);
	}
	
	public boolean canBePlacedInsideBlock(int x, int y, int z)
	{
		TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles)
			return true;
		
		return false;
	}
	
	public boolean canBePlacedInside(int x, int y, int z, Vec3 hitVec, ForgeDirection side)
	{
		TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			switch(side)
			{
			case EAST:
			case WEST:
				return (int)hitVec.xCoord != hitVec.xCoord;
			case UP:
			case DOWN:
				return (int)hitVec.yCoord != hitVec.yCoord;
			case SOUTH:
			case NORTH:
				return (int)hitVec.zCoord != hitVec.zCoord;
			default:
				return false;
			}
		}
		/*
		 * if (block == Blocks.snow_layer && (world.getBlockMetadata(x, y, z) & 7) < 1)
        {
            p_77648_7_ = 1;
        }
        else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(p_77648_3_, p_77648_4_, p_77648_5_, p_77648_6_))
        {
        	
        } 
		 */
		return false;
	}
	
	public LittleTileVec getHitVec(Vec3 hitVec, int x, int y, int z, ForgeDirection side)
	{
		double posX = hitVec.xCoord - x;
		//if(hitVec.xCoord < 0)
			//posX = 1-posX;
		double posY = hitVec.yCoord - y;
		//if(hitVec.yCoord < 0)
			//posY = 1-posY;
		double posZ = hitVec.zCoord - z;
		//if(hitVec.zCoord < 0)
			//posZ = 1-posZ;
		LittleTileVec vec = new LittleTileVec((int)(posX*16), (int)(posY*16), (int)(posZ*16));
		if(!canBePlacedInside(x, y, z, hitVec, side))
		{
			switch(side)
			{
			case EAST:
				//vec.x = 15;
				vec.x = 0;
				break;
			case WEST:
				vec.x = 16;
				//if(x < 0)
					//vec.x = 0;
				break;
			case UP:
				//vec.y = 15;
				vec.y = 0;
				break;
			case DOWN:
				vec.y = 16;
				break;
			case SOUTH:
				//vec.z = 15;
				vec.z = 0;
				break;
			case NORTH:
				vec.z = 16;
				//if(z < 0)
					//vec.z = 0;
				break;
			default:
				break;
			
			}
		}
		return vec;
	}
	
	public static class PreviewTile {
		
		public LittleTileBox box;
		public LittleTilePreview preview;
		
		public PreviewTile(LittleTileBox box, LittleTilePreview preview)
		{
			this.box = box;
			this.preview = preview;
		}
		
		public PreviewTile copy()
		{
			return new PreviewTile(box.copy(), preview.copy());
		}
		
		public boolean split(HashMapList<ChunkCoordinates, PreviewTile> tiles, int x, int y, int z)
		{
			if(!preview.canSplit && box.needsMultipleBlocks())
				return false;
			
			LittleTileSize size = box.getSize();
			
			int offX = box.minX/16;
			if(box.minX < 0)
				offX = (int) Math.floor(box.minX/16D);
			int offY = box.minY/16;
			if(box.minY < 0)
				offY = (int) Math.floor(box.minY/16D);
			int offZ = box.minZ/16;
			if(box.minZ < 0)
				offZ = (int) Math.floor(box.minZ/16D);
			
			int posX = x+offX;
			int posY = y+offY;
			int posZ = z+offZ;
			
			byte spaceX = (byte) (box.minX-offX*16);
			byte spaceY = (byte) (box.minY-offY*16);
			byte spaceZ = (byte) (box.minZ-offZ*16);
			
			for (int i = 0; spaceX+size.sizeX > i*16; i++) {
				posY = y+offY;
				for (int j = 0; spaceY+size.sizeY > j*16; j++) {
					posZ = z+offZ;
					for (int h = 0; spaceZ+size.sizeZ > h*16; h++) {
						
						PreviewTile tile = this.copy();
						if(i > 0)
							tile.box.minX =	0;
						else
							tile.box.minX = (byte) spaceX;
						if(i*16+16 > spaceX+size.sizeX)
							tile.box.maxX = (byte) (box.maxX-box.maxX/16*16);
						else
							tile.box.maxX = 16;
						
						if(j > 0)
							tile.box.minY =	0;
						else
							tile.box.minY = (byte) spaceY;
						if(j*16+16 > spaceY+size.sizeY)
							tile.box.maxY = (byte) (box.maxY-box.maxY/16*16);
						else
							tile.box.maxY = 16;
						
						if(h > 0)
							tile.box.minZ =	0;
						else
							tile.box.minZ = (byte) spaceZ;
						if(h*16+16 > spaceZ+size.sizeZ)
							tile.box.maxZ = (byte) (box.maxZ-box.maxZ/16*16);
						else
							tile.box.maxZ = 16;
						
						if(tile.box.isValidBox())
						{
							tiles.add(new ChunkCoordinates(posX, posY, posZ), tile);
						}
						posZ++;
					}
					posY++;
				}
				posX++;
			}
			
			return true;
		}
		
	}
}
