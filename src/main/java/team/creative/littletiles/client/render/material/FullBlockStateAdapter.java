package team.creative.littletiles.client.render.material;

import java.util.concurrent.atomic.AtomicBoolean;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.mc.BlockTile;

/** Render-view-only correction of the ordinary block side of a mixed LT boundary. */
public final class FullBlockStateAdapter {
    private static final ThreadLocal<Boolean> READING_NEIGHBORS = ThreadLocal.withInitial(() -> false);
    private static final AtomicBoolean REPORTED = new AtomicBoolean();
    private FullBlockStateAdapter() {}

    public static boolean eligible(BlockState state) {
        return !READING_NEIGHBORS.get() && ConnectedMaterialState.supports(state);
    }

    public static BlockState resolve(BlockAndTintGetter view, BlockPos pos, BlockState state) {
        if (!eligible(state)) return state;
        READING_NEIGHBORS.set(true);
        try {
            Direction positive = ConnectedMaterialState.positiveDirection(state);
            Direction negative = positive.getOpposite();
            BlockState above = view.getBlockState(pos.relative(positive));
            BlockState below = view.getBlockState(pos.relative(negative));
            // Ordinary-only connections keep Yuushya's saved state and behavior.
            if (!(above.getBlock() instanceof BlockTile) && !(below.getBlock() instanceof BlockTile)) return state;
            var aboveMaterials = neighbor(view, pos.relative(positive), above);
            var belowMaterials = neighbor(view, pos.relative(negative), below);
            if (!aboveMaterials.available() || !belowMaterials.available()) {
                MaterialDiagnostics.record("full/unavailable", pos, state, belowMaterials, aboveMaterials, state);
                return state;
            }
            BlockState material = ConnectedMaterialState.key(state);
            BlockState resolved = ConnectedMaterialState.resolve(state,
                belowMaterials.contains(material), aboveMaterials.contains(material));
            MaterialDiagnostics.record("full", pos, state, belowMaterials, aboveMaterials, resolved);
            if (resolved != state && REPORTED.compareAndSet(false, true))
                LogUtils.getLogger().info("LittleTiles full-block boundary render state adapted at {}: {} -> {} (stored block unchanged)", pos, state, resolved);
            return resolved;
        } finally {
            READING_NEIGHBORS.remove();
        }
    }

    private static NeighborMaterials<BlockState> neighbor(BlockAndTintGetter view, BlockPos pos, BlockState neighbor) {
        if (neighbor.getBlock() instanceof BlockTile) {
            // Read the renderer's snapshot; do not fetch or create entities through the live world.
            var entity = view.getBlockEntity(pos);
            return MaterialNeighbors.tiles(entity instanceof BETiles tiles ? tiles : null);
        }
        return MaterialNeighbors.ordinary(neighbor);
    }
}
