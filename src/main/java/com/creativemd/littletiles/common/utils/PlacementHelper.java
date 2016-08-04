package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;

import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.InsideShiftHandler;
import com.creativemd.littletiles.utils.PreviewTile;
import com.creativemd.littletiles.utils.ShiftHandler;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**This class does all caculate on where to place a block. Used for rendering preview and placing**/
public class PlacementHelper {
	
	private static PlacementHelper instance;
	
	public static PlacementHelper getInstance(EntityPlayer player)
	{
		if(instance == null)
			instance = new PlacementHelper(player);
		else{
			instance.player = player;
			instance.world = player.worldObj;
		}
		return instance;
	}
	
	public EntityPlayer player;
	public World world; 
	
	public PlacementHelper(EntityPlayer player)
	{
		this.player = player;
		this.world = player.worldObj;
	}
	
	public static ILittleTile getLittleInterface(ItemStack stack)
	{
		if(stack == null)
			return null;
		if(stack.getItem() instanceof ILittleTile)
			return (ILittleTile) stack.getItem();
		if(Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile)
			return (ILittleTile)Block.getBlockFromItem(stack.getItem());
		return null;
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
	
	public ArrayList<PreviewTile> getPreviewTiles(ItemStack stack, RayTraceResult moving, boolean customPlacement) //, ForgeDirection rotation, ForgeDirection rotation2)
	{
		return getPreviewTiles(stack, moving.getBlockPos(), player.getPositionVector(), moving.hitVec, moving.sideHit, customPlacement); //, rotation, rotation2);
	}
	
	public static LittleTileVec getInternalOffset(ArrayList<LittleTilePreview> tiles)
	{
		byte minX = LittleTile.maxPos;
		byte minY = LittleTile.maxPos;
		byte minZ = LittleTile.maxPos;
		for (int i = 0; i < tiles.size(); i++) {
			LittleTilePreview tile = tiles.get(i);
			if(tile == null)
				return new LittleTileVec(0, 0, 0);
			if(tile.box != null)
			{
				minX = (byte) Math.min(minX, tile.box.minX);
				minY = (byte) Math.min(minY, tile.box.minY);
				minZ = (byte) Math.min(minZ, tile.box.minZ);
			}
		}
		return new LittleTileVec(minX, minY, minZ);
	}
	
	public static LittleTileSize getSize(ArrayList<LittleTilePreview> tiles)
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
	
	public ArrayList<PreviewTile> getPreviewTiles(ItemStack stack, BlockPos pos, Vec3d playerPos, Vec3d hitVec, EnumFacing side, boolean customPlacement) //, ForgeDirection rotation, ForgeDirection rotation2)
	{
		return getPreviewTiles(stack, pos, playerPos, hitVec, side, customPlacement, false);
	}
	
	public ArrayList<PreviewTile> getPreviewTiles(ItemStack stack, BlockPos pos, Vec3d playerPos, Vec3d hitVec, EnumFacing side, boolean customPlacement, boolean inside) //, ForgeDirection rotation, ForgeDirection rotation2)
	{
		ArrayList<ShiftHandler> shifthandlers = new ArrayList<ShiftHandler>();
		ArrayList<PreviewTile> preview = new ArrayList<PreviewTile>();
		ArrayList<LittleTilePreview> tiles = null;
		
		LittleTilePreview tempPreview = null;
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		
		if(iTile != null)
			tiles = iTile.getLittlePreview(stack);
		
		if(tiles != null)
		{
			LittleTileSize size = getSize(tiles);
			
			//size.rotateSize(rotation);
			//size.rotateSize(rotation2);
			//size.rotateSize(rotation2.getRotation(ForgeDirection.DOWN));
			
			//size.rotateSize(side);
			
			
 			if(tiles.size() == 1)
				shifthandlers.addAll(tiles.get(0).shifthandlers);
			
			shifthandlers.add(new InsideShiftHandler());
			
			LittleTileBox box = getTilesBox(size, hitVec, pos, side, customPlacement, inside);
			LittleTileVec internalOffset = getInternalOffset(tiles);
			internalOffset.invert();
			//box.addOffset(new LittleTileVec(-LittleTile.maxPos/2, -LittleTile.maxPos/2, -LittleTile.maxPos/2));
			
			boolean canPlaceNormal = false;
			
			if(!customPlacement && player.isSneaking())
			{			
				if(!inside && !canBePlacedInside(pos, hitVec, side))
				{
					pos = pos.offset(side);
				}
				
				if(tiles.size() > 0 && tiles.get(0).box != null)
				{
					Block block = world.getBlockState(pos).getBlock();
					if(block.isReplaceable(world, pos) || block instanceof BlockTile)
					{
						TileEntity te = world.getTileEntity(pos);
						canPlaceNormal = true;
						if(te instanceof TileEntityLittleTiles)
						{
							TileEntityLittleTiles teTiles = (TileEntityLittleTiles) te;
							for (int i = 0; i < tiles.size(); i++) {
								LittleTilePreview tile = tiles.get(i);
								if(!teTiles.isSpaceForLittleTile(tile.box))
								{
									canPlaceNormal = false;
									break;
								}
							}
						}
					}
				}
				
				if(!canPlaceNormal)
				{
					
					for (int i = 0; i < shifthandlers.size(); i++) {
						shifthandlers.get(i).init(world, pos);
					}
					
					LittleTileVec hit = getHitVec(hitVec, pos, side, customPlacement, inside);
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
						box = handler.getNewPosition(world, pos, box);
					}
				}
			}
			
			LittleTileVec offset = box.getMinVec();
			
			offset.addVec(internalOffset);
			
			
			for (int i = 0; i < tiles.size(); i++) {
				LittleTilePreview tile = tiles.get(i);
				if(tile != null)
				{
					if(tile.box == null)
					{
						preview.add(new PreviewTile(box.copy(), tile));
					}else{
						if(!canPlaceNormal)
							tile.box.addOffset(offset);
						//tile.box.rotateBox(rotation);
						//tile.box.rotateBox(rotation2);
						//tile.box.rotateBox(rotation2.getRotation(ForgeDirection.DOWN));
						preview.add(new PreviewTile(tile.box, tile));
					}
				}
			}
			
			LittleStructure structure = iTile.getLittleStructure(stack);
			if(structure != null)
			{
				//ArrayList<LittleTileBox> highlightedBoxes = structure.getSpecialTiles();
				ArrayList<PreviewTile> newBoxes = structure.getSpecialTiles();
				
				for (int i = 0; i < newBoxes.size(); i++) {
					if(!canPlaceNormal)
						newBoxes.get(i).box.addOffset(offset);
				}
				
				preview.addAll(newBoxes);
				
				/*for (int i = 0; i < highlightedBoxes.size(); i++) {
					if(!canPlaceNormal)
						highlightedBoxes.get(i).addOffset(offset);
					//tile.box.rotateBox(rotation);
					//tile.box.rotateBox(rotation2);
					//tile.box.rotateBox(rotation2.getRotation(ForgeDirection.DOWN));
					PreviewTile previewTile = new PreviewTile(highlightedBoxes.get(i), null); 
					previewTile.color = Vec3.createVectorHelper(1, 0, 0);
					preview.add(previewTile);
				}*/
			}
		}
		
		return preview;
	}
	
	public LittleTileBox getTilesBox(LittleTileSize size, Vec3d hitVec, BlockPos pos, EnumFacing side, boolean customPlacement, boolean inside)
	{
		LittleTileVec hit = getHitVec(hitVec, pos, side, customPlacement, inside);
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
	
	public boolean canBePlacedInsideBlock(BlockPos pos)
	{
		TileEntity tileEntity = player.worldObj.getTileEntity(pos);
		if(tileEntity instanceof TileEntityLittleTiles)
			return true;
		
		return false;
	}
	
	public boolean canBePlacedInside(BlockPos pos, Vec3d hitVec, EnumFacing side)
	{
		TileEntity tileEntity = player.worldObj.getTileEntity(pos);
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
	
	public LittleTileVec getHitVec(Vec3d hitVec, BlockPos pos, EnumFacing side, boolean customPlacement, boolean isInside)
	{
		if(customPlacement && !isInside)
		{
			double posX = hitVec.xCoord - pos.getX();
			double posY = hitVec.yCoord - pos.getY();
			double posZ = hitVec.zCoord - pos.getZ();
			
			LittleTileVec vec = new LittleTileVec((int)(posX*16), (int)(posY*16), (int)(posZ*16));
			if(!canBePlacedInside(pos, hitVec, side))
			{
				switch(side)
				{
				case EAST:
					vec.x -= 16;
					break;
				case WEST:
					vec.x += 16;
					break;
				case UP:
					vec.y -= 16;
					break;
				case DOWN:
					vec.y += 16;
					break;
				case SOUTH:
					vec.z -= 16;
					break;
				case NORTH:
					vec.z += 16;
					break;
				default:
					break;
				
				}
			}
			return vec;
		}
		double posX = hitVec.xCoord - pos.getX();
		//if(hitVec.xCoord < 0)
			//posX = 1-posX;
		double posY = hitVec.yCoord - pos.getY();
		//if(hitVec.yCoord < 0)
			//posY = 1-posY;
		double posZ = hitVec.zCoord - pos.getZ();
		//if(hitVec.zCoord < 0)
			//posZ = 1-posZ;
		LittleTileVec vec = new LittleTileVec((int)(posX*16), (int)(posY*16), (int)(posZ*16));
		if(!customPlacement && !canBePlacedInside(pos, hitVec, side))
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
}
