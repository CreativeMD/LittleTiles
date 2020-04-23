package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.mc.TickUtils;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.fixed.InsideFixedHandler;
import com.creativemd.littletiles.common.tile.place.fixed.SecondModeHandler;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** This class does all calculate on where to place a block. Used for rendering
 * preview and placing **/
public class PlacementHelper {
	
	public static ILittleTile getLittleInterface(ItemStack stack) {
		if (stack == null)
			return null;
		if (stack.getItem() instanceof ILittleTile)
			return (ILittleTile) stack.getItem();
		if (Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile)
			return (ILittleTile) Block.getBlockFromItem(stack.getItem());
		return null;
	}
	
	public static boolean isLittleBlock(ItemStack stack) {
		if (stack == null)
			return false;
		if (stack.getItem() instanceof ILittleTile)
			return ((ILittleTile) stack.getItem()).hasLittlePreview(stack);
		if (Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile)
			return ((ILittleTile) Block.getBlockFromItem(stack.getItem())).hasLittlePreview(stack);
		return false;
	}
	
	public static LittleVec getInternalOffset(ILittleTile iTile, ItemStack stack, LittlePreviews tiles, LittleGridContext original) {
		LittleVec offset = iTile.getCachedOffset(stack);
		if (offset != null) {
			if (tiles.getContext() != original)
				offset.convertTo(original, tiles.getContext());
			return offset;
		}
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		for (LittlePreview preview : tiles.allPreviews()) {
			if (preview.box != null) {
				minX = Math.min(minX, preview.box.minX);
				minY = Math.min(minY, preview.box.minY);
				minZ = Math.min(minZ, preview.box.minZ);
			}
		}
		return new LittleVec(minX, minY, minZ);
	}
	
	public static LittleVec getSize(ILittleTile iTile, ItemStack stack, LittlePreviews tiles, boolean allowLowResolution, LittleGridContext original) {
		LittleVec cached = iTile.getCachedSize(stack);
		if (cached != null) {
			if (tiles.getContext() != original)
				cached.convertTo(original, tiles.getContext());
			return cached;
		}
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		LittleVec size = new LittleVec(0, 0, 0);
		for (LittlePreview preview : tiles.allPreviews()) {
			minX = Math.min(minX, preview.box.minX);
			minY = Math.min(minY, preview.box.minY);
			minZ = Math.min(minZ, preview.box.minZ);
			maxX = Math.max(maxX, preview.box.maxX);
			maxY = Math.max(maxY, preview.box.maxY);
			maxZ = Math.max(maxZ, preview.box.maxZ);
		}
		return new LittleVec(maxX - minX, maxY - minY, maxZ - minZ).max(size);
	}
	
	public static void removeCache() {
		lastCached = null;
		lastPreviews = null;
		lastLowResolution = false;
	}
	
	private static boolean lastLowResolution;
	private static NBTTagCompound lastCached;
	private static LittlePreviews lastPreviews;
	
	@SideOnly(Side.CLIENT)
	public static PlacementPosition getPosition(World world, RayTraceResult moving, LittleGridContext context, ILittleTile tile, ItemStack stack) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		
		int x = moving.getBlockPos().getX();
		int y = moving.getBlockPos().getY();
		int z = moving.getBlockPos().getZ();
		
		boolean canBePlacedInsideBlock = true;
		if (!canBePlacedInside(world, moving.getBlockPos(), moving.hitVec, moving.sideHit)) {
			switch (moving.sideHit) {
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
		
		BlockPos pos = new BlockPos(x, y, z);
		
		PlacementPosition result = new PlacementPosition(pos, getHitVec(moving, context, canBePlacedInsideBlock).getVecContext(), moving.sideHit);
		
		if (tile != null && stack != null && (LittleAction.isUsingSecondMode(player) != tile.snapToGridByDefault())) {
			Vec3d position = player.getPositionEyes(TickUtils.getPartialTickTime());
			double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
			Vec3d temp = player.getLook(TickUtils.getPartialTickTime());
			Vec3d look = position.addVector(temp.x * d0, temp.y * d0, temp.z * d0);
			position = position.subtract(pos.getX(), pos.getY(), pos.getZ());
			look = look.subtract(pos.getX(), pos.getY(), pos.getZ());
			List<LittleRenderingCube> cubes = tile.getPositingCubes(world, pos, stack);
			if (cubes != null)
				result.positingCubes = cubes;
		}
		
		return result;
	}
	
	/** @param centered
	 *            if the previews should be centered
	 * @param facing
	 *            if centered is true it will be used to apply the offset
	 * @param fixed
	 *            if the previews should keep it's original boxes */
	public static PlacementPreview getPreviews(World world, ItemStack stack, PlacementPosition position, boolean centered, boolean fixed, boolean allowLowResolution, boolean marked, PlacementMode mode) {
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		
		LittlePreviews tiles = allowLowResolution == lastLowResolution && iTile.shouldCache() && lastCached != null && lastCached.equals(stack.getTagCompound()) ? lastPreviews.copy() : null;
		if (tiles == null && iTile != null)
			tiles = iTile.getLittlePreview(stack, allowLowResolution, marked);
		
		PlacementPreview result = getPreviews(world, tiles, iTile.getPreviewsContext(stack), stack, position, centered, fixed, allowLowResolution, mode);
		
		if (result != null) {
			if (stack.getTagCompound() == null) {
				lastCached = null;
				lastPreviews = null;
			} else {
				lastLowResolution = allowLowResolution;
				lastCached = stack.getTagCompound().copy();
				lastPreviews = tiles.copy();
			}
		}
		return result;
	}
	
	public static PlacementPreview getAbsolutePreviews(World world, LittlePreviews previews, BlockPos pos, PlacementMode mode) {
		return new PlacementPreview(world, previews, mode, previews.getSurroundingBox(), true, pos, null, null);
	}
	
	/** @param hit
	 *            relative vector to pos
	 * @param centered
	 *            if the previews should be centered
	 * @param facing
	 *            if centered is true it will be used to apply the offset
	 * @param fixed
	 *            if the previews should keep it's original boxes */
	public static PlacementPreview getPreviews(World world, @Nullable LittlePreviews tiles, LittleGridContext original, ItemStack stack, PlacementPosition position, boolean centered, boolean fixed, boolean allowLowResolution, PlacementMode mode) {
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		
		if (tiles != null && (!tiles.isEmpty() || tiles.hasChildren())) {
			
			if (tiles.isAbsolute())
				return new PlacementPreview(world, tiles, mode, tiles.getSurroundingBox(), true, tiles.getBlockPos(), null, position.facing);
			
			tiles.forceContext(position);
			LittleGridContext context = tiles.getContext();
			
			LittleVec size = getSize(iTile, stack, tiles, allowLowResolution, original);
			
			List<SecondModeHandler> shifthandlers = new ArrayList<SecondModeHandler>();
			
			boolean singleMode = tiles.size() == 1;
			
			if (singleMode) {
				shifthandlers.add(new InsideFixedHandler());
				centered = true;
			}
			
			LittleBox box = getTilesBox(position, size, centered, position.facing, mode);
			
			boolean canBePlaceFixed = false;
			
			if (fixed && !singleMode) {
				canBePlaceFixed = LittleAction.canPlaceInside(tiles, world, position.getPos(), mode.placeInside);
				
				if (!canBePlaceFixed)
					for (int i = 0; i < shifthandlers.size(); i++)
						box = shifthandlers.get(i).getBox(world, position.getPos(), context, box);
					
			}
			
			LittleAbsoluteVec offset = new LittleAbsoluteVec(position.getPos(), context, box.getMinVec());
			LittleVec internalOffset = getInternalOffset(iTile, stack, tiles, original);
			internalOffset.invert();
			offset.getVec().add(internalOffset);
			
			if ((canBePlaceFixed || (fixed && singleMode)) && mode.placeInside)
				if (position.getVec().get(position.facing.getAxis()) % context.size == 0)
					offset.getVec().add(position.facing.getOpposite());
				
			return new PlacementPreview(world, tiles, mode, box, canBePlaceFixed, offset.getPos(), offset.getVec(), position.facing);
		}
		
		return null;
	}
	
	public static LittleBox getTilesBox(LittleAbsoluteVec pos, LittleVec size, boolean centered, @Nullable EnumFacing facing, PlacementMode mode) {
		LittleVec temp = pos.getVec().copy();
		if (centered) {
			LittleVec center = size.calculateCenter();
			LittleVec centerInv = size.calculateInvertedCenter();
			
			if (mode.placeInside)
				facing = facing.getOpposite();
			
			// Make hit the center of the Box
			switch (facing) {
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
		return new LittleBox(temp, size.x, size.y, size.z);
	}
	
	public static boolean canBlockBeUsed(World world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityLittleTiles)
			return true;
		return ChiselsAndBitsManager.isChiselsAndBitsStructure(tileEntity);
	}
	
	public static boolean canBePlacedInside(World world, BlockPos pos, Vec3d hitVec, EnumFacing side) {
		if (canBlockBeUsed(world, pos)) {
			switch (side) {
			case EAST:
			case WEST:
				return (int) hitVec.x != hitVec.x;
			case UP:
			case DOWN:
				return (int) hitVec.y != hitVec.y;
			case SOUTH:
			case NORTH:
				return (int) hitVec.z != hitVec.z;
			default:
				return false;
			}
		}
		return false;
	}
	
	public static LittleAbsoluteVec getHitVec(RayTraceResult result, LittleGridContext context, boolean isInsideOfBlock) {
		LittleAbsoluteVec pos = new LittleAbsoluteVec(result, context);
		
		if (!isInsideOfBlock)
			pos.getVec().set(result.sideHit.getAxis(), result.sideHit.getAxisDirection() == AxisDirection.POSITIVE ? 0 : context.size);
		
		return pos;
	}
}
