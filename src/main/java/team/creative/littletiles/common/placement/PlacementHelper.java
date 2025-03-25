package team.creative.littletiles.common.placement;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.Maths;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;

/** This class does all calculate on where to place a block. Used for rendering
 * preview and placing **/
public class PlacementHelper {
    
    public static PlacementPosition getPosition(Level level, BlockHitResult moving, LittleGrid context, ILittleTool tile, ItemStack stack) {
        int x = moving.getBlockPos().getX();
        int y = moving.getBlockPos().getY();
        int z = moving.getBlockPos().getZ();
        
        Facing facing = Facing.get(moving.getDirection());
        
        boolean canBePlacedInsideBlock = true;
        if (moving.getType() != Type.MISS && !canBePlacedInside(level, moving.getBlockPos(), moving.getLocation(), facing)) {
            switch (moving.getDirection()) {
                case EAST -> x++;
                case WEST -> x--;
                case UP -> y++;
                case DOWN -> y--;
                case SOUTH -> z++;
                case NORTH -> z--;
            }
            canBePlacedInsideBlock = false;
        }
        
        if (context == null)
            return null;
        
        return new PlacementPosition(new BlockPos(x, y, z), getHitVec(moving, context, canBePlacedInsideBlock).getVecGrid(), facing);
    }
    
    public static LittleVecAbsolute getHitVec(BlockHitResult result, LittleGrid grid, boolean isInsideOfBlock) {
        LittleVecAbsolute pos = new LittleVecAbsolute(result, grid);
        
        Facing facing = Facing.get(result.getDirection());
        if (!isInsideOfBlock && result.getType() != Type.MISS)
            pos.getVec().set(facing.axis, facing.positive ? 0 : grid.count);
        
        return pos;
    }
    
    public static LittleBox getTilesBox(PlacementPosition pos, LittleVec size, boolean centered, boolean placeInside) {
        return getTilesBox(pos, size, centered, pos.facing, placeInside);
    }
    
    public static LittleBox getTilesBox(LittleVecAbsolute pos, LittleVec size, boolean centered, @Nullable Facing facing, boolean placeInside) {
        LittleVec temp = pos.getVec().copy();
        if (centered && facing != null) {
            if (placeInside)
                facing = facing.opposite();
            
            // Make hit the center of the Box
            if (facing.positive)
                temp.set(facing.axis, temp.get(facing.axis) + size.calculateCenter().get(facing.axis));
            else
                temp.set(facing.axis, temp.get(facing.axis) - size.calculateInvertedCenter().get(facing.axis));
        }
        return new LittleBox(temp, size.x, size.y, size.z);
    }
    
    public static boolean canBlockBeUsed(Level world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        return be instanceof BETiles || ChiselsAndBitsManager.isChiselsAndBitsStructure(be);
    }
    
    public static boolean canBePlacedInside(Level level, BlockPos pos, Vec3 hitVec, Facing side) {
        if (canBlockBeUsed(level, pos))
            return !Maths.equals((int) Maths.round(side.axis.get(hitVec)), side.axis.get(hitVec), 0.00001);
        return false;
    }
    
}
