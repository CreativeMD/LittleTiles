package team.creative.littletiles.common.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.Maths;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;

/** This class does all calculate on where to place a block. Used for rendering
 * preview and placing **/
public class PlacementHelper {
    
    public static PlacementPosition getPositionInside(Level level, BlockHitResult hit, LittleGrid grid) {
        var pos = getPosition(level, hit, grid);
        if (pos.facing != null)
            if (pos.facing.positive && grid.isAtEdge(pos.facing.axis.get(hit.getLocation())))
                pos.box.move(new LittleVecGrid(new LittleVec(pos.facing.opposite()), grid));
        return pos;
    }
    
    public static PlacementPosition getPosition(Level level, BlockHitResult hit, LittleGrid grid) {
        int x = hit.getBlockPos().getX();
        int y = hit.getBlockPos().getY();
        int z = hit.getBlockPos().getZ();
        
        Facing facing = hit.getType() != Type.MISS ? Facing.get(hit.getDirection()) : null;
        
        boolean canBePlacedInsideBlock = true;
        if (facing != null && !canBePlacedInside(level, hit.getBlockPos(), hit.getLocation(), facing)) {
            switch (facing) {
                case EAST -> x++;
                case WEST -> x--;
                case UP -> y++;
                case DOWN -> y--;
                case SOUTH -> z++;
                case NORTH -> z--;
            }
            canBePlacedInsideBlock = false;
        }
        
        if (grid == null)
            return null;
        
        return new PlacementPosition(new BlockPos(x, y, z), getHitVec(hit, grid, canBePlacedInsideBlock).getVecGrid(), facing);
    }
    
    public static LittleVecAbsolute getHitVec(BlockHitResult result, LittleGrid grid, boolean isInsideOfBlock) {
        LittleVecAbsolute pos = new LittleVecAbsolute(result, grid);
        
        Facing facing = Facing.get(result.getDirection());
        if (!isInsideOfBlock && result.getType() != Type.MISS)
            pos.getVec().set(facing.axis, facing.positive ? 0 : grid.count);
        
        return pos;
    }
    
    public static LittleBox getTilesBox(PlacementPosition pos, LittleVec size, boolean centered, boolean placeInside) {
        LittleVec temp = pos.box.box.getMinVec();
        if (centered && pos.facing != null) {
            Facing facing = pos.facing;
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
            return !Maths.equals((int) Math.round(side.axis.get(hitVec)), side.axis.get(hitVec), 0.00001);
        return false;
    }
    
}
