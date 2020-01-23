package com.creativemd.littletiles.common.utils.placing;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.mods.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.place.fixed.FixedHandler;
import com.creativemd.littletiles.common.tile.place.fixed.InsideFixedHandler;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** This class does all calculate on where to place a block. Used for rendering
 * preview and placing **/
public class PlacementHelper {
	
	public static class PositionResult extends LittleAbsoluteVec {
		
		public EnumFacing facing;
		
		public PositionResult() {
			super((BlockPos) null, (LittleGridContext) null);
		}
		
		public PositionResult(BlockPos pos, LittleGridContext context, LittleVec vec, EnumFacing facing) {
			super(pos, context, vec);
			this.facing = facing;
		}
		
		public static PositionResult readFromBytes(ByteBuf buf) {
			PositionResult result = new PositionResult();
			result.pos = LittleAction.readPos(buf);
			result.contextVec = LittleAction.readLittleVecContext(buf);
			result.facing = CreativeCorePacket.readFacing(buf);
			return result;
		}
		
		public void assign(LittleAbsoluteVec pos) {
			this.pos = pos.getPos();
			this.contextVec = pos.getVecContext();
		}
		
		public AxisAlignedBB getBox() {
			double x = getPosX();
			double y = getPosY();
			double z = getPosZ();
			return new AxisAlignedBB(x, y, z, x + getContext().pixelSize, y + getContext().pixelSize, z + getContext().pixelSize);
		}
		
		public void subVec(LittleVec vec) {
			contextVec.getVec().add(vec);
			removeInternalBlockOffset();
		}
		
		public void addVec(LittleVec vec) {
			contextVec.getVec().sub(vec);
			removeInternalBlockOffset();
		}
		
		public void writeToBytes(ByteBuf buf) {
			LittleAction.writePos(buf, pos);
			LittleAction.writeLittleVecContext(contextVec, buf);
			CreativeCorePacket.writeFacing(buf, facing);
		}
		
		@Override
		public PositionResult copy() {
			PositionResult result = new PositionResult();
			result.facing = facing;
			result.contextVec = contextVec.copy();
			result.pos = pos;
			return result;
		}
	}
	
	public static class PreviewResult {
		
		public List<PlacePreview> placePreviews = new ArrayList<>();
		public LittlePreviews previews = null;
		public LittleGridContext context;
		public LittleBox box;
		public LittleVec size;
		public boolean singleMode = false;
		public boolean placedFixed = false;
		public LittleAbsoluteVec offset;
		
		public boolean isAbsolute() {
			return previews.isAbsolute();
		}
		
	}
	
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
			if (tiles.context != original)
				offset.convertTo(original, tiles.context);
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
			if (tiles.context != original)
				cached.convertTo(original, tiles.context);
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
	
	public static PositionResult getPosition(World world, RayTraceResult moving, LittleGridContext context) {
		PositionResult result = new PositionResult();
		
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
		
		result.facing = moving.sideHit;
		result.assign(getHitVec(moving, context, canBePlacedInsideBlock));
		result.setPos(new BlockPos(x, y, z));
		
		return result;
	}
	
	/** @param centered
	 *            if the previews should be centered
	 * @param facing
	 *            if centered is true it will be used to apply the offset
	 * @param fixed
	 *            if the previews should keep it's original boxes */
	public static PreviewResult getPreviews(World world, ItemStack stack, PositionResult position, boolean centered, boolean fixed, boolean allowLowResolution, boolean marked, PlacementMode mode) {
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		
		LittlePreviews tiles = allowLowResolution == lastLowResolution && iTile.shouldCache() && lastCached != null && lastCached.equals(stack.getTagCompound()) ? lastPreviews.copy() : null;
		if (tiles == null && iTile != null)
			tiles = iTile.getLittlePreview(stack, allowLowResolution, marked);
		
		PreviewResult result = getPreviews(world, tiles, iTile.getPreviewsContext(stack), stack, position, centered, fixed, allowLowResolution, mode);
		
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
	
	/** @param hit
	 *            relative vector to pos
	 * @param centered
	 *            if the previews should be centered
	 * @param facing
	 *            if centered is true it will be used to apply the offset
	 * @param fixed
	 *            if the previews should keep it's original boxes */
	public static PreviewResult getPreviews(World world, @Nullable LittlePreviews tiles, LittleGridContext original, ItemStack stack, PositionResult position, boolean centered, boolean fixed, boolean allowLowResolution, PlacementMode mode) {
		PreviewResult result = new PreviewResult();
		
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		
		if (tiles != null && (!tiles.isEmpty() || tiles.hasChildren())) {
			if (tiles.isAbsolute()) {
				result.context = tiles.context;
				result.previews = tiles;
				result.singleMode = false;
				result.placedFixed = false;
				result.offset = new LittleAbsoluteVec(tiles.getBlockPos(), result.context, LittleVec.ZERO);
				position.assign(result.offset);
				result.placePreviews = new ArrayList<>();
				tiles.getPlacePreviews(result.placePreviews, null, true, null);
				
				return result;
			}
			
			if (tiles.hasStructure()) {
				LittleGridContext structureContext = tiles.getMinContext();
				if (structureContext.size > position.getContext().size)
					position.convertTo(structureContext);
			}
			
			tiles.ensureContext(position.getContext());
			if (position.getContext() != tiles.context)
				position.convertTo(tiles.context);
			
			LittleGridContext context = tiles.context;
			
			result.context = context;
			result.previews = tiles;
			
			result.size = getSize(iTile, stack, tiles, allowLowResolution, original);
			
			ArrayList<FixedHandler> shifthandlers = new ArrayList<FixedHandler>();
			
			if (tiles.size() == 1) {
				shifthandlers.add(new InsideFixedHandler());
				result.singleMode = true;
				centered = true;
			}
			
			result.box = getTilesBox(position, result.size, centered, position.facing, mode);
			
			boolean canBePlaceFixed = false;
			
			if (fixed) {
				if (!result.singleMode) {
					Block block = world.getBlockState(position.getPos()).getBlock();
					if (block.isReplaceable(world, position.getPos()) || block instanceof BlockTile) {
						canBePlaceFixed = true;
						if (!mode.placeInside) {
							TileEntity te = world.getTileEntity(position.getPos());
							if (te instanceof TileEntityLittleTiles) {
								TileEntityLittleTiles teTiles = (TileEntityLittleTiles) te;
								for (LittlePreview preview : tiles.allPreviews()) {
									if (!teTiles.isSpaceForLittleTile(preview.box)) {
										canBePlaceFixed = false;
										break;
									}
								}
							}
						}
					}
				}
				
				if (!canBePlaceFixed) {
					for (int i = 0; i < shifthandlers.size(); i++) {
						shifthandlers.get(i).init(world, position.getPos());
					}
					
					FixedHandler handler = null;
					double distance = 2;
					for (int i = 0; i < shifthandlers.size(); i++) {
						double tempDistance = shifthandlers.get(i).getDistance(position);
						if (tempDistance < distance) {
							distance = tempDistance;
							handler = shifthandlers.get(i);
						}
					}
					
					if (handler != null)
						result.box = handler.getNewPosition(world, position.getPos(), context, result.box);
				}
			}
			
			LittleAbsoluteVec offset = new LittleAbsoluteVec(position.getPos(), context, result.box.getMinVec());
			LittleVec internalOffset = getInternalOffset(iTile, stack, tiles, original);
			internalOffset.invert();
			offset.getVec().add(internalOffset);
			
			result.offset = offset;
			
			result.placedFixed = canBePlaceFixed;
			
			if ((canBePlaceFixed || (fixed && result.singleMode)) && mode.placeInside)
				if (position.getVec().get(position.facing.getAxis()) % context.size == 0)
					offset.getVec().add(position.facing.getOpposite());
				
			// Generating placetiles
			tiles.getPlacePreviews(result.placePreviews, result.box, canBePlaceFixed, offset.getVec());
			return result;
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
		return new LittleBox(temp, size);
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
