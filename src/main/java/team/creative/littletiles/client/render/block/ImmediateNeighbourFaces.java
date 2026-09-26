package team.creative.littletiles.client.render.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import team.creative.littletiles.common.block.entity.BETiles;

/** Invalidate client meshes when geometry changes, without waiting for server neighbour packets. */
public final class ImmediateNeighbourFaces {
    private ImmediateNeighbourFaces() {}

    public static void tilesChanged(BETiles changed) {
        Level level = changed.getLevel();
        if (level == null || !level.isClientSide)
            return;
        BlockPos pos = changed.getBlockPos();
        for (Direction direction : Direction.values())
            shapeChanged(level, pos.relative(direction));
    }

    public static void shapeChanged(LevelAccessor level, BlockPos pos) {
        if (!level.isClientSide() || !level.hasChunkAt(pos))
            return;
        if (level.getBlockEntity(pos) instanceof BETiles neighbour && !neighbour.isRemoved())
            neighbour.render.onNeighbourChanged();
    }
}
