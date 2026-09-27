package team.creative.littletiles.client.render.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.mc.BlockTile;

/** Position-dependent coverage must not enter a cache keyed only by block states. */
public final class VanillaNeighbourCulling {
    private VanillaNeighbourCulling() {}

    public static boolean fullyHidden(BlockState state, BlockGetter level, BlockPos pos, Direction direction, BlockPos neighbourPos) {
        if (state.getBlock() instanceof BlockTile)
            return false;
        if (!(level.getBlockState(neighbourPos).getBlock() instanceof BlockTile))
            return false;
        if (!state.isSolidRender(level, pos))
            return false;
        if (!(level.getBlockEntity(neighbourPos) instanceof BETiles neighbour) || neighbour.isRemoved())
            return false;
        return neighbour.sideCache.get(Facing.get(direction.getOpposite())).doesBlockLight();
    }
}
