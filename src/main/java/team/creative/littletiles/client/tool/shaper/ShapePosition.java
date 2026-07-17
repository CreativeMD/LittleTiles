package team.creative.littletiles.client.tool.shaper;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.PlacementPosition;

public class ShapePosition extends PlacementPosition {
    
    public final BlockHitResult ray;
    public final LittleTileContext result;
    
    public ShapePosition(Player player, Level level, PlacementPosition position, BlockHitResult result, boolean whenClicked, boolean inside) {
        super(position.box().copy(), position.facing);
        this.ray = result;
        this.result = LittleTileContext.selectFocused(level, result.getBlockPos(), player, whenClicked ? 1 : TickUtils.getFrameTime(player.level()));
        LittleGrid grid = getGrid();
        if (position.facing != null)
            if (inside) {
                if (position.facing.positive && grid.isAtEdge(position.facing.axis.get(result.getLocation())))
                    box.move(new LittleVecGrid(new LittleVec(facing.opposite()), grid));
            } else if (!position.facing.positive && grid.isAtEdge(position.facing.axis.get(result.getLocation())))
                box.move(new LittleVecGrid(new LittleVec(facing), grid));
    }
    
    public ShapePosition(PlacementPosition position, BlockHitResult ray, LittleTileContext result) {
        super(position.box().copy(), position.facing);
        this.ray = ray;
        this.result = result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ShapePosition s) {
            if (!super.equals(obj))
                return false;
            if (result.parent != s.result.parent)
                return false;
            if (result.tile != s.result.tile)
                return false;
            return true;
        }
        return false;
    }
    
    @Override
    public ShapePosition copy() {
        return new ShapePosition(super.copy(), ray, result);
    }
    
}
