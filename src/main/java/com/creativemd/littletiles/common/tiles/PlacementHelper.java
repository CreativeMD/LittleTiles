package com.creativemd.littletiles.common.tiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.cert.CRLReason;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.TickUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.tiles.place.FixedHandler;
import com.creativemd.littletiles.common.tiles.place.InsideFixedHandler;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**This class does all caculate on where to place a block. Used for rendering preview and placing**/
public class PlacementHelper {
	
	public static class PositionResult {
		
		public BlockPos pos;
		public LittleTileVec hit;
		public EnumFacing facing;
		
		public static PositionResult readFromBytes(ByteBuf buf)
		{
			PositionResult result = new PositionResult();
			result.pos = CreativeCorePacket.readPos(buf);
			result.facing = CreativeCorePacket.readFacing(buf);
			result.hit = new LittleTileVec(buf.readInt(), buf.readInt(), buf.readInt());
			return result;
		}
		
		public LittleTileVec getAbsoluteVec()
		{
			LittleTileVec absolute = new LittleTileVec(pos);
			absolute.addVec(hit);
			return absolute;
		}
		
		public void subVec(LittleTileVec vec)
		{
			hit.addVec(vec);
			updatePos();
		}
		
		public void addVec(LittleTileVec vec)
		{
			hit.subVec(vec);
			updatePos();
		}
		
		public void writeToBytes(ByteBuf buf)
		{
			CreativeCorePacket.writePos(buf, pos);
			CreativeCorePacket.writeFacing(buf, facing);
			buf.writeInt(hit.x);
			buf.writeInt(hit.y);
			buf.writeInt(hit.z);
		}
		
		private void updatePos()
		{
			//Larger
			if(hit.x >= LittleTile.gridSize)
			{
				int amount = hit.x / LittleTile.gridSize;
				hit.x -= amount * LittleTile.gridSize;
				pos = pos.add(amount, 0, 0);
			}
			if(hit.y >= LittleTile.gridSize)
			{
				int amount = hit.y / LittleTile.gridSize;
				hit.y -= amount * LittleTile.gridSize;
				pos = pos.add(0, amount, 0);
			}
			if(hit.z >= LittleTile.gridSize)
			{
				int amount = hit.z / LittleTile.gridSize;
				hit.z -= amount * LittleTile.gridSize;
				pos = pos.add(0, 0, amount);
			}
			
			//Smaller
			if(hit.x < 0)
			{
				int amount = (int) Math.ceil(Math.abs(hit.x / LittleTile.gridSize));
				hit.x += amount * LittleTile.gridSize;
				pos = pos.add(-amount, 0, 0);
			}
			if(hit.y < 0)
			{
				int amount = (int) Math.ceil(Math.abs(hit.y / LittleTile.gridSize));
				hit.y += amount * LittleTile.gridSize;
				pos = pos.add(-amount, 0, 0);
			}
			if(hit.z < 0)
			{
				int amount = (int) Math.ceil(Math.abs(hit.z / LittleTile.gridSize));
				hit.z += amount * LittleTile.gridSize;
				pos = pos.add(-amount, 0, 0);
			}
		}

		public PositionResult copy()
		{
			PositionResult result = new PositionResult();
			result.facing = facing;
			result.pos = pos;
			result.hit = hit.copy();
			return result;
		}
	}
	
	public static class PreviewResult {
		
		public List<PlacePreviewTile> placePreviews = new ArrayList<>();
		public List<LittleTilePreview> previews = null;
		public LittleTileBox box;
		public LittleTileSize size;
		public boolean usedSize = false;
		public boolean placedFixed = false;
		public LittleTileVec offset;
		
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
			return ((ILittleTile) stack.getItem()).hasLittlePreview(stack);
		if(Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile)
			return ((ILittleTile)Block.getBlockFromItem(stack.getItem())).hasLittlePreview(stack);
		return false;
	}
	
	public static LittleTileVec getInternalOffset(List<LittleTilePreview> tiles)
	{
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		for (int i = 0; i < tiles.size(); i++) {
			LittleTilePreview tile = tiles.get(i);
			if(tile == null)
				return new LittleTileVec(0, 0, 0);
			if(tile.box != null)
			{
				minX = Math.min(minX, tile.box.minX);
				minY = Math.min(minY, tile.box.minY);
				minZ = Math.min(minZ, tile.box.minZ);
			}
		}
		return new LittleTileVec(minX, minY, minZ);
	}
	
	public static LittleTileSize getSize(List<LittleTilePreview> tiles)
	{
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		LittleTileSize size = new LittleTileSize(0, 0, 0);
		for (int i = 0; i < tiles.size(); i++) {
			LittleTilePreview tile = tiles.get(i);
			if(tile == null)
				return new LittleTileSize(0, 0, 0);
			if(tile.box != null)
			{
				minX = Math.min(minX, tile.box.minX);
				minY = Math.min(minY, tile.box.minY);
				minZ = Math.min(minZ, tile.box.minZ);
				maxX = Math.max(maxX, tile.box.maxX);
				maxY = Math.max(maxY, tile.box.maxY);
				maxZ = Math.max(maxZ, tile.box.maxZ);
			}else{
				size.max(tile.size);
			}
		}
		return new LittleTileSize(maxX-minX, maxY-minY, maxZ-minZ).max(size);
	}
	
	public static void removeCache()
	{
		lastCached = null;
		lastPreviews = null;
	}
	
	private static NBTTagCompound lastCached;
	private static ArrayList<LittleTilePreview> lastPreviews;
	
	public static PositionResult getPosition(World world, RayTraceResult moving)
	{
		PositionResult result = new PositionResult();
		
		int x = moving.getBlockPos().getX();
		int y = moving.getBlockPos().getY();
		int z = moving.getBlockPos().getZ();
		
		boolean canBePlacedInsideBlock = true;
		if(!canBePlacedInside(world, moving.getBlockPos(), moving.hitVec, moving.sideHit))
		{
			switch(moving.sideHit)
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
			
			canBePlacedInsideBlock = false;
		}
		
		result.facing = moving.sideHit;
		result.pos = new BlockPos(x, y, z);
		result.hit = getHitVec(moving, canBePlacedInsideBlock);
		
		return result;
	}
	
	/**
	 * @param centered if the previews should be centered
	 * @param facing if centered is true it will be used to apply the offset
	 * @param fixed if the previews should keep it's original boxes
	 */
	public static PreviewResult getPreviews(World world, ItemStack stack, PositionResult position, boolean centered, boolean fixed, boolean allowLowResolution)
	{
		return getPreviews(world, stack, position.pos, position.hit, centered, position.facing, fixed, allowLowResolution);
	}
	
	/**
	 * @param hit relative vector to pos
	 * @param centered if the previews should be centered
	 * @param facing if centered is true it will be used to apply the offset
	 * @param fixed if the previews should keep it's original boxes
	 */
	public static PreviewResult getPreviews(World world, ItemStack stack, BlockPos pos, LittleTileVec hit, boolean centered, @Nullable EnumFacing facing, boolean fixed, boolean allowLowResolution)
	{
		PreviewResult result = new PreviewResult();
		
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		
		List<LittleTilePreview> tiles = allowLowResolution && iTile.shouldCache() && lastCached != null && lastCached.equals(stack.getTagCompound()) ? new ArrayList<>(lastPreviews) : null;
		
		if(tiles == null && iTile != null)
			tiles = iTile.getLittlePreview(stack, allowLowResolution);
		
		if(tiles != null && tiles.size() > 0)
		{
			result.previews = tiles;
			
			result.size = getSize(tiles);
			
			ArrayList<FixedHandler> shifthandlers = new ArrayList<FixedHandler>();
			
			if(tiles.size() == 1 && tiles.get(0).box == null) //Will only be used if it's the only preview trying to be placed
			{
				shifthandlers.addAll(tiles.get(0).fixedhandlers);
				shifthandlers.add(new InsideFixedHandler());
				result.usedSize = true;
			}
			
			result.box = getTilesBox(hit, result.size, centered, facing);
			
			boolean canBePlaceFixed = false;
			
			if(fixed)
			{
				if(!result.usedSize)
				{
					Block block = world.getBlockState(pos).getBlock();
					if(block.isReplaceable(world, pos) || block instanceof BlockTile)
					{
						canBePlaceFixed = true;
						TileEntity te = world.getTileEntity(pos);
						if(te instanceof TileEntityLittleTiles)
						{
							TileEntityLittleTiles teTiles = (TileEntityLittleTiles) te;
							for (int i = 0; i < tiles.size(); i++) {
								LittleTilePreview tile = tiles.get(i);
								if(!teTiles.isSpaceForLittleTile(tile.box))
								{
									canBePlaceFixed = false;
									break;
								}
							}
						}
					}
				}
				
				if(!canBePlaceFixed)
				{
					for (int i = 0; i < shifthandlers.size(); i++) {
						shifthandlers.get(i).init(world, pos);
					}
					
					FixedHandler handler = null;
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
						result.box = handler.getNewPosition(world, pos, result.box);
				}
			}
			
			LittleTileVec offset = result.box.getMinVec();
			LittleTileVec internalOffset = getInternalOffset(tiles);
			internalOffset.invert();
			offset.addVec(internalOffset);
			
			result.offset = offset;
			
			result.placedFixed = canBePlaceFixed;
			
			//Generating placetiles
			for (int i = 0; i < tiles.size(); i++) {
				LittleTilePreview tile = tiles.get(i);
				if(tile != null)
				{
					PlacePreviewTile preview = tile.getPlaceableTile(result.box, canBePlaceFixed, offset);
					if(preview != null)
						result.placePreviews.add(preview);
				}
			}
			
			LittleStructure structure = iTile.getLittleStructure(stack);
			if(structure != null)
			{
				ArrayList<PlacePreviewTile> newBoxes = structure.getSpecialTiles();
				
				for (int i = 0; i < newBoxes.size(); i++) {
					if(!canBePlaceFixed)
						newBoxes.get(i).box.addOffset(offset);
				}
				
				result.placePreviews.addAll(newBoxes);
			}
			
			if(allowLowResolution)
			{
				if(stack.getTagCompound() == null)
				{
					lastCached = null;
					lastPreviews = null;
				}else{
					lastCached = stack.getTagCompound().copy();
					lastPreviews = new ArrayList<>(tiles);
				}
			}
			
			return result;
		}
		
		return null;
	}
	
	public static LittleTileBox getTilesBox(LittleTileVec hit, LittleTileSize size, boolean centered, @Nullable EnumFacing facing)
	{
		LittleTileVec temp = hit.copy();
		if(centered)
		{
			LittleTileVec center = size.calculateCenter();
			LittleTileVec centerInv = size.calculateInvertedCenter();
			
			//Make hit the center of the Box
			switch(facing)
			{
			case EAST:
				temp.x += center.x;
				break;
			case WEST:
				temp.x -= centerInv.x;
				break;
			case UP:
				temp.y += center.y;
				break;
			case DOWN:
				temp.y -= centerInv.y;
				break;
			case SOUTH:
				temp.z += center.z;
				break;
			case NORTH:
				temp.z -= centerInv.z;
				break;
			default:
				break;
			}
		}
		return new LittleTileBox(temp, size);
	}
	
	public static boolean canBePlacedInsideBlock(World world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityLittleTiles)
			return true;
		
		return false;
	}
	
	public static boolean canBePlacedInside(World world, BlockPos pos, Vec3d hitVec, EnumFacing side)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			switch(side)
			{
			case EAST:
			case WEST:
				return (int)hitVec.x != hitVec.x;
			case UP:
			case DOWN:
				return (int)hitVec.y != hitVec.y;
			case SOUTH:
			case NORTH:
				return (int)hitVec.z != hitVec.z;
			default:
				return false;
			}
		}
		return false;
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    //BigDecimal bd = new BigDecimal(value);
	    //bd = bd.setScale(places, RoundingMode.HALF_UP);
	    //return bd.doubleValue();
	   double precision = Math.pow(10, places);
	   return Math.round(value * precision) / precision;
	}
	
	public static double round(double value)
	{
		return round(value, 6);
	}
	
	public static LittleTileVec getHitVec(RayTraceResult result, boolean isInsideOfBlock)
	{
		double posX = result.hitVec.x - result.getBlockPos().getX();
		double posY = result.hitVec.y - result.getBlockPos().getY();
		double posZ = result.hitVec.z - result.getBlockPos().getZ();
		
		LittleTileVec vec = new LittleTileVec((int)round(posX*LittleTile.gridSize), (int)round(posY*LittleTile.gridSize), (int)round(posZ*LittleTile.gridSize));
		
		/*if(!fixed)
		{
			if(!isInsideOfBlock)
			{
				switch(side)
				{
				case EAST:
					vec.x -= LittleTile.gridSize;
					break;
				case WEST:
					vec.x += LittleTile.gridSize;
					break;
				case UP:
					vec.y -= LittleTile.gridSize;
					break;
				case DOWN:
					vec.y += LittleTile.gridSize;
					break;
				case SOUTH:
					vec.z -= LittleTile.gridSize;
					break;
				case NORTH:
					vec.z += LittleTile.gridSize;
					break;
				default:
					break;
				
				}
			}
			return vec;
		}*/
		
		if(!isInsideOfBlock)
		{
			switch(result.sideHit)
			{
			case EAST:
				vec.x = 0;
				break;
			case WEST:
				vec.x = LittleTile.gridSize;
				break;
			case UP:
				vec.y = 0;
				break;
			case DOWN:
				vec.y = LittleTile.gridSize;
				break;
			case SOUTH:
				vec.z = 0;
				break;
			case NORTH:
				vec.z = LittleTile.gridSize;
				break;
			default:
				break;
			
			}
		}
		
		return vec;
	}
}
