package team.creative.littletiles.common.placement;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.Maths;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import team.creative.littletiles.common.placement.mode.PlacementMode;

/** This class does all calculate on where to place a block. Used for rendering
 * preview and placing **/
public class PlacementHelper {
    
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
        LittleVec offset = iTile.getCachedMin(stack);
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
    
    public static LittleVec getSize(ILittlePlacer iTile, ItemStack stack, LittleGroup tiles, LittleGrid original) {
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
    
    @OnlyIn(Dist.CLIENT)
    public static PlacementPosition getPosition(Level level, BlockHitResult moving, LittleGrid context, ILittleTool tile, ItemStack stack) {
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
        
        if (context == null)
            return null;
        
        return new PlacementPosition(new BlockPos(x, y, z), getHitVec(moving, context, canBePlacedInsideBlock).getVecGrid(), Facing.get(moving.getDirection()));
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
                    return !Maths.equals((int) Maths.round(hitVec.x), hitVec.x);
                case UP:
                case DOWN:
                    return !Maths.equals((int) Maths.round(hitVec.y), hitVec.y);
                case SOUTH:
                case NORTH:
                    return !Maths.equals((int) Maths.round(hitVec.z), hitVec.z);
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
