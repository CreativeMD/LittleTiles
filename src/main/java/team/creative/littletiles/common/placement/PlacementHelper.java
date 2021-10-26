package team.creative.littletiles.common.placement;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.api.tool.ILittleTool;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.second.InsideFixedHandler;
import team.creative.littletiles.common.placement.second.SecondModeHandler;

/** This class does all calculate on where to place a block. Used for rendering
 * preview and placing **/
public class PlacementHelper {
    
    public static final int LOW_RESOLUTION_COUNT = 2000;
    
    public static ILittlePlacer getLittleInterface(ItemStack stack) {
        if (stack.getItem() instanceof ILittlePlacer)
            return (ILittlePlacer) stack.getItem();
        return null;
    }
    
    public static boolean isLittleBlock(ItemStack stack) {
        if (stack == null)
            return false;
        if (stack.getItem() instanceof ILittlePlacer)
            return ((ILittlePlacer) stack.getItem()).hasTiles(stack);
        return false;
    }
    
    public static LittleVec getInternalOffset(ILittlePlacer iTile, ItemStack stack, LittleGroup tiles, LittleGrid original) {
        LittleVec offset = iTile.getCachedOffset(stack);
        if (offset != null) {
            if (tiles.getGrid() != original)
                offset.convertTo(original, tiles.getGrid());
            return offset;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        for (LittleBox box : tiles.allBoxes()) {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
        }
        return new LittleVec(minX, minY, minZ);
    }
    
    public static LittleVec getSize(ILittlePlacer iTile, ItemStack stack, LittleGroup tiles, boolean allowLowResolution, LittleGrid original) {
        LittleVec cached = iTile.getCachedSize(stack);
        if (cached != null) {
            if (tiles.getGrid() != original)
                cached.convertTo(original, tiles.getGrid());
            return cached;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        LittleVec size = new LittleVec(0, 0, 0);
        for (LittleBox box : tiles.allBoxes()) {
            minX = Math.min(minX, box.minX);
            minY = Math.min(minY, box.minY);
            minZ = Math.min(minZ, box.minZ);
            maxX = Math.max(maxX, box.maxX);
            maxY = Math.max(maxY, box.maxY);
            maxZ = Math.max(maxZ, box.maxZ);
        }
        return new LittleVec(maxX - minX, maxY - minY, maxZ - minZ).max(size);
    }
    
    public static void removeCache() {
        lastCached = null;
        lastPreviews = null;
        lastLowResolution = false;
    }
    
    private static boolean lastLowResolution;
    private static CompoundTag lastCached;
    private static LittleGroup lastPreviews;
    
    @OnlyIn(Dist.CLIENT)
    public static PlacementPosition getPosition(Level level, BlockHitResult moving, LittleGrid context, ILittleTool tile, ItemStack stack) {
        Player player = Minecraft.getInstance().player;
        
        int x = moving.getBlockPos().getX();
        int y = moving.getBlockPos().getY();
        int z = moving.getBlockPos().getZ();
        
        boolean canBePlacedInsideBlock = true;
        if (!canBePlacedInside(level, moving.getBlockPos(), moving.getLocation(), Facing.get(moving.getDirection()))) {
            switch (moving.getDirection()) {
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
        
        PlacementPosition result = new PlacementPosition(pos, getHitVec(moving, context, canBePlacedInsideBlock).getVecGrid(), Facing.get(moving.getDirection()));
        
        if (tile instanceof ILittlePlacer && stack != null && (LittleAction.isUsingSecondMode(player) != ((ILittlePlacer) tile).snapToGridByDefault(stack))) {
            Vec3d position = player.getPositionEyes(TickUtils.getPartialTickTime());
            double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
            Vec3d temp = player.getLook(TickUtils.getPartialTickTime());
            Vec3d look = position.addVector(temp.x * d0, temp.y * d0, temp.z * d0);
            position = position.subtract(pos.getX(), pos.getY(), pos.getZ());
            look = look.subtract(pos.getX(), pos.getY(), pos.getZ());
            List<LittleRenderBox> cubes = ((ILittlePlacer) tile).getPositingCubes(world, pos, stack);
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
    public static PlacementPreview getPreviews(Level level, ItemStack stack, PlacementPosition position, boolean centered, boolean fixed, boolean allowLowResolution, PlacementMode mode) {
        ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
        
        LittleGroup tiles = allowLowResolution == lastLowResolution && iTile.shouldCache() && lastCached != null && lastCached.equals(stack.getTag()) ? lastPreviews.copy() : null;
        if (tiles == null && iTile != null)
            tiles = iTile.getTiles(stack, allowLowResolution);
        
        PlacementPreview result = getPreviews(level, tiles, iTile.getTilesGrid(stack), stack, position, centered, fixed, allowLowResolution, mode);
        
        if (result != null) {
            if (stack.getTag() == null) {
                lastCached = null;
                lastPreviews = null;
            } else {
                lastLowResolution = allowLowResolution;
                lastCached = stack.getTag().copy();
                lastPreviews = tiles.copy();
            }
        }
        return result;
    }
    
    public static PlacementPreview getAbsolutePreviews(Level level, LittleGroup previews, BlockPos pos, PlacementMode mode) {
        return new PlacementPreview(level, previews, mode, previews.getSurroundingBox(), true, pos, null, null);
    }
    
    /** @param hit
     *            relative vector to pos
     * @param centered
     *            if the previews should be centered
     * @param facing
     *            if centered is true it will be used to apply the offset
     * @param fixed
     *            if the previews should keep it's original boxes */
    public static PlacementPreview getPreviews(Level world, @Nullable LittleGroup tiles, LittleGrid original, ItemStack stack, PlacementPosition position, boolean centered, boolean fixed, boolean allowLowResolution, PlacementMode mode) {
        ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
        
        if (tiles != null && (!tiles.isEmpty() || tiles.hasChildren())) {
            
            if (tiles.isAbsolute())
                return new PlacementPreview(world, tiles, mode, tiles.getSurroundingBox(), true, tiles.getBlockPos(), null, position.facing);
            
            tiles.forceContext(position);
            LittleGrid context = tiles.getContext();
            
            LittleVec size = getSize(iTile, stack, tiles, allowLowResolution, original);
            
            List<SecondModeHandler> shifthandlers = new ArrayList<SecondModeHandler>();
            
            boolean singleMode = tiles.totalSize() == 1;
            
            if (singleMode) {
                shifthandlers.add(new InsideFixedHandler());
                centered = true;
            }
            
            LittleBox box = getTilesBox(position, size, centered, position.facing, mode);
            
            boolean canBePlaceFixed = false;
            
            if (fixed) {
                canBePlaceFixed = !singleMode && LittleAction.canPlaceInside(tiles, world, position.getPos(), mode.placeInside);
                
                if (!canBePlaceFixed)
                    for (int i = 0; i < shifthandlers.size(); i++)
                        box = shifthandlers.get(i).getBox(world, position.getPos(), context, box);
                    
            }
            
            LittleVecAbsolute offset = new LittleVecAbsolute(position.getPos(), context, box.getMinVec());
            LittleVec internalOffset = getInternalOffset(iTile, stack, tiles, original);
            internalOffset.invert();
            offset.getVec().add(internalOffset);
            
            if ((canBePlaceFixed || (fixed && singleMode)) && mode.placeInside)
                if (position.getVec().get(position.facing.axis) % context.size == 0)
                    offset.getVec().add(position.facing.opposite());
                
            return new PlacementPreview(world, tiles, mode, box, canBePlaceFixed, offset.getPos(), offset.getVec(), position.facing);
        }
        
        return null;
    }
    
    public static LittleBox getTilesBox(LittleVecAbsolute pos, LittleVec size, boolean centered, @Nullable Facing facing, PlacementMode mode) {
        LittleVec temp = pos.getVec().copy();
        if (centered) {
            LittleVec center = size.calculateCenter();
            LittleVec centerInv = size.calculateInvertedCenter();
            
            if (mode.placeInside)
                facing = facing.opposite();
            
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
    
    public static boolean canBlockBeUsed(Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BETiles)
            return true;
        return ChiselsAndBitsManager.isChiselsAndBitsStructure(blockEntity);
    }
    
    public static boolean canBePlacedInside(Level level, BlockPos pos, Vec3 hitVec, Facing side) {
        if (canBlockBeUsed(level, pos)) {
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
    
    public static LittleVecAbsolute getHitVec(BlockHitResult result, LittleGrid grid, boolean isInsideOfBlock) {
        LittleVecAbsolute pos = new LittleVecAbsolute(result, grid);
        
        Facing facing = Facing.get(result.getDirection());
        if (!isInsideOfBlock)
            pos.getVec().set(facing.axis, facing.positive ? 0 : grid.count);
        
        return pos;
    }
}
