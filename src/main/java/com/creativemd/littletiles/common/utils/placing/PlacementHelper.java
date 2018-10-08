package com.creativemd.littletiles.common.utils.placing;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.mods.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.place.FixedHandler;
import com.creativemd.littletiles.common.tiles.place.InsideFixedHandler;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementMode.PreviewMode;

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

/** This class does all calculate on where to place a block. Used for rendering preview and placing **/
public class PlacementHelper {
	
	public static class PositionResult extends LittleTilePos {
		
		public EnumFacing facing;
		
		public PositionResult() {
			super((BlockPos) null, (LittleGridContext) null);
		}
		
		public PositionResult(BlockPos pos, LittleGridContext context, LittleTileVec vec, EnumFacing facing) {
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
		
		public void assign(LittleTilePos pos) {
			this.pos = pos.pos;
			this.contextVec = pos.contextVec;
		}
		
		public AxisAlignedBB getBox() {
			double x = getPosX();
			double y = getPosY();
			double z = getPosZ();
			return new AxisAlignedBB(x, y, z, x + getContext().gridMCLength, y + getContext().gridMCLength, z + getContext().gridMCLength);
		}
		
		public void subVec(LittleTileVec vec) {
			contextVec.vec.add(vec);
			removeInternalBlockOffset();
		}
		
		public void addVec(LittleTileVec vec) {
			contextVec.vec.sub(vec);
			removeInternalBlockOffset();
		}
		
		public void writeToBytes(ByteBuf buf) {
			LittleAction.writePos(buf, pos);
			LittleAction.writeLittleVecContext(contextVec, buf);
			CreativeCorePacket.writeFacing(buf, facing);
		}
		
		public PositionResult copy() {
			PositionResult result = new PositionResult();
			result.facing = facing;
			result.contextVec = contextVec.copy();
			result.pos = pos;
			return result;
		}
	}
	
	public static class PreviewResult {
		
		public List<PlacePreviewTile> placePreviews = new ArrayList<>();
		public LittlePreviews previews = null;
		public LittleGridContext context;
		public LittleTileBox box;
		public LittleTileSize size;
		public boolean singleMode = false;
		public boolean placedFixed = false;
		public LittleTilePos offset;
		
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
	
	public static LittleTileVec getInternalOffset(ILittleTile iTile, ItemStack stack, LittlePreviews tiles) {
		LittleTileVec offset = iTile.getCachedOffset(stack);
		if (offset != null)
			return offset;
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		for (LittleTilePreview preview : tiles.allPreviews()) {
			if (preview.box != null) {
				minX = Math.min(minX, preview.box.minX);
				minY = Math.min(minY, preview.box.minY);
				minZ = Math.min(minZ, preview.box.minZ);
			}
		}
		return new LittleTileVec(minX, minY, minZ);
	}
	
	public static LittleTileSize getSize(ILittleTile iTile, ItemStack stack, LittlePreviews tiles, boolean allowLowResolution) {
		LittleTileSize cached = iTile.getCachedSize(stack);
		if (cached != null)
			return cached;
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		LittleTileSize size = new LittleTileSize(0, 0, 0);
		for (LittleTilePreview preview : tiles.allPreviews()) {
			minX = Math.min(minX, preview.box.minX);
			minY = Math.min(minY, preview.box.minY);
			minZ = Math.min(minZ, preview.box.minZ);
			maxX = Math.max(maxX, preview.box.maxX);
			maxY = Math.max(maxY, preview.box.maxY);
			maxZ = Math.max(maxZ, preview.box.maxZ);
		}
		return new LittleTileSize(maxX - minX, maxY - minY, maxZ - minZ).max(size);
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
		result.pos = new BlockPos(x, y, z);
		
		return result;
	}
	
	/**
	 * @param centered
	 *            if the previews should be centered
	 * @param facing
	 *            if centered is true it will be used to apply the offset
	 * @param fixed
	 *            if the previews should keep it's original boxes
	 */
	public static PreviewResult getPreviews(World world, ItemStack stack, PositionResult position, boolean centered, boolean fixed, boolean allowLowResolution, boolean marked, PlacementMode mode) {
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		
		LittlePreviews tiles = allowLowResolution == lastLowResolution && iTile.shouldCache() && lastCached != null && lastCached.equals(stack.getTagCompound()) ? lastPreviews.copy() : null;
		if (tiles == null && iTile != null)
			tiles = iTile.getLittlePreview(stack, allowLowResolution, marked);
		
		PreviewResult result = getPreviews(world, tiles, stack, position, centered, fixed, allowLowResolution, mode);
		
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
	
	/**
	 * @param hit
	 *            relative vector to pos
	 * @param centered
	 *            if the previews should be centered
	 * @param facing
	 *            if centered is true it will be used to apply the offset
	 * @param fixed
	 *            if the previews should keep it's original boxes
	 */
	public static PreviewResult getPreviews(World world, @Nullable LittlePreviews tiles, ItemStack stack, PositionResult position, boolean centered, boolean fixed, boolean allowLowResolution, PlacementMode mode) {
		PreviewResult result = new PreviewResult();
		
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		
		if (tiles != null && (!tiles.isEmpty() || tiles.hasChildren())) {
			if (tiles.isAbsolute()) {
				result.context = tiles.context;
				result.previews = tiles;
				result.singleMode = false;
				result.placedFixed = false;
				result.offset = new LittleTilePos(tiles.getBlockPos(), result.context, LittleTileVec.ZERO);
				position.assign(result.offset);
				result.placePreviews = new ArrayList<>();
				tiles.getPlacePreviews(result.placePreviews, null, true, null);
				
				return result;
			}
			
			if (tiles.hasStructure()) {
				LittleGridContext structureContext = tiles.getMinContext();
				if (structureContext.size > position.contextVec.context.size)
					position.convertTo(structureContext);
			}
			
			tiles.ensureContext(position.getContext());
			if (position.getContext() != tiles.context)
				position.convertTo(tiles.context);
			
			LittleGridContext context = tiles.context;
			
			result.context = context;
			result.previews = tiles;
			
			result.size = getSize(iTile, stack, tiles, allowLowResolution);
			
			ArrayList<FixedHandler> shifthandlers = new ArrayList<FixedHandler>();
			
			if (tiles.size() == 1) {
				shifthandlers.addAll(tiles.get(0).fixedhandlers);
				shifthandlers.add(new InsideFixedHandler());
				result.singleMode = true;
				centered = true;
			}
			
			result.box = getTilesBox(position, result.size, centered, position.facing, mode);
			
			boolean canBePlaceFixed = false;
			
			if (fixed) {
				if (!result.singleMode) {
					Block block = world.getBlockState(position.pos).getBlock();
					if (block.isReplaceable(world, position.pos) || block instanceof BlockTile) {
						canBePlaceFixed = true;
						if (mode.mode == PreviewMode.PREVIEWS) {
							TileEntity te = world.getTileEntity(position.pos);
							if (te instanceof TileEntityLittleTiles) {
								TileEntityLittleTiles teTiles = (TileEntityLittleTiles) te;
								for (LittleTilePreview preview : tiles.allPreviews()) {
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
						shifthandlers.get(i).init(world, position.pos);
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
						result.box = handler.getNewPosition(world, position.pos, context, result.box);
				}
			}
			
			LittleTilePos offset = new LittleTilePos(position.pos, context, result.box.getMinVec());
			LittleTileVec internalOffset = getInternalOffset(iTile, stack, tiles);
			internalOffset.invert();
			offset.contextVec.vec.add(internalOffset);
			
			result.offset = offset;
			
			result.placedFixed = canBePlaceFixed;
			
			if ((canBePlaceFixed || (fixed && result.singleMode)) && mode.mode == PreviewMode.LINES)
				if (position.contextVec.vec.get(position.facing.getAxis()) % context.size == 0)
					offset.contextVec.vec.add(position.facing.getOpposite());
				
			// Generating placetiles
			tiles.getPlacePreviews(result.placePreviews, result.box, canBePlaceFixed, offset.contextVec.vec);
			return result;
		}
		
		return null;
	}
	
	public static LittleTileBox getTilesBox(LittleTilePos pos, LittleTileSize size, boolean centered, @Nullable EnumFacing facing, PlacementMode mode) {
		LittleTileVec temp = pos.contextVec.vec.copy();
		if (centered) {
			LittleTileVec center = size.calculateCenter();
			LittleTileVec centerInv = size.calculateInvertedCenter();
			
			if (mode.mode == PreviewMode.LINES)
				facing = facing.getOpposite();
			
			//Make hit the center of the Box
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
		return new LittleTileBox(temp, size);
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
				return (int) hitVec.xCoord != hitVec.xCoord;
			case UP:
			case DOWN:
				return (int) hitVec.yCoord != hitVec.yCoord;
			case SOUTH:
			case NORTH:
				return (int) hitVec.zCoord != hitVec.zCoord;
			default:
				return false;
			}
		}
		return false;
	}
	
	public static LittleTilePos getHitVec(RayTraceResult result, LittleGridContext context, boolean isInsideOfBlock) {
		LittleTilePos pos = new LittleTilePos(result, context);
		
		if (!isInsideOfBlock)
			pos.contextVec.vec.set(result.sideHit.getAxis(), result.sideHit.getAxisDirection() == AxisDirection.POSITIVE ? 0 : context.size);
		
		return pos;
	}
}
