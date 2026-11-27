package team.creative.littletiles.client.render.material;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.mc.BlockTile;

/** Choose one full-block appearance per material, then let LT clip it to every piece. */
public final class MaterialStateAdapter {
    private static final AtomicBoolean REPORTED = new AtomicBoolean();
    private MaterialStateAdapter() {}

    public static void apply(RenderingBlockContext context, Int2ObjectMap<ChunkLayerMapList<LittleRenderBox>> boxes) {
        if (boxes == null) return;
        Map<Direction, NeighborMaterials<BlockState>> neighbors = new EnumMap<>(Direction.class);
        Map<BlockState, BlockState> resolvedStates = new HashMap<>();
        for (var layer : boxes.values()) for (LittleRenderBox box : layer) {
            if (box.state == null || !ConnectedMaterialState.supports(box.state)) continue;
            BlockState material = ConnectedMaterialState.key(box.state);
            Direction positive = ConnectedMaterialState.positiveDirection(material);
            var above = neighbors.computeIfAbsent(positive, d -> neighbor(context, d));
            var below = neighbors.computeIfAbsent(positive.getOpposite(), d -> neighbor(context, d));
            if (!above.available() || !below.available()) {
                MaterialDiagnostics.record("LT/unavailable", context.be.getBlockPos(), box.state, below, above, box.state);
                continue;
            }
            BlockState resolved = resolvedStates.computeIfAbsent(material,
                key -> ConnectedMaterialState.resolve(key, below.contains(key), above.contains(key)));
            MaterialDiagnostics.record("LT", context.be.getBlockPos(), box.state, below, above, resolved);
            if (resolved != box.state) {
                if (REPORTED.compareAndSet(false, true))
                    LogUtils.getLogger().info("LittleTiles block-position material state adapted at {}: {} -> {} (all pieces share appearance; stored tile unchanged)",
                        context.be.getBlockPos(), box.state, resolved);
                box.state = resolved;
                box.deleteQuadCache();
            }
        }
    }

    private static NeighborMaterials<BlockState> neighbor(RenderingBlockContext context, Direction direction) {
        var pos = context.be.getBlockPos().relative(direction);
        if (!context.getLevel().hasChunkAt(pos)) return NeighborMaterials.unavailable();
        BlockState state = context.getLevel().getBlockState(pos);
        return state.getBlock() instanceof BlockTile
            ? MaterialNeighbors.tiles(BlockTile.loadBE(context.getLevel(), pos)) : MaterialNeighbors.ordinary(state);
    }
}
