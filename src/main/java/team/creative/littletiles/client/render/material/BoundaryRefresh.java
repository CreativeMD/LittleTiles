package team.creative.littletiles.client.render.material;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import team.creative.littletiles.common.block.entity.BETiles;

/** Defer until loaded entities are installed in their chunk; coalesce packet bursts. */
public final class BoundaryRefresh {
    private static final Map<Level, Set<BlockPos>> PENDING = new HashMap<>();
    private BoundaryRefresh() {}

    public static synchronized void request(BETiles tiles) {
        Level level = tiles.getLevel();
        if (level != null && level.isClientSide)
            PENDING.computeIfAbsent(level, ignored -> new HashSet<>()).add(tiles.getBlockPos().immutable());
    }

    public static void tick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        Level level = client.level;
        Set<BlockPos> changed;
        synchronized (BoundaryRefresh.class) {
            changed = PENDING.remove(level);
            PENDING.clear(); // Discard updates from disconnected worlds.
        }
        if (level == null || changed == null) return;
        Set<BlockPos> affected = new HashSet<>();
        Set<Long> redrawnInteriors = new HashSet<>();
        for (BlockPos pos : changed) {
            affected.add(pos);
            for (Direction direction : Direction.values()) affected.add(pos.relative(direction));
        }
        for (BlockPos pos : affected) {
            if (!level.hasChunkAt(pos)) continue;
            // LT edits change boundary geometry even when no block state changes.
            // Match the working compatibility refresh: invalidate the boxes too.
            // Retain the neighbor-dirty indication for updates arriving mid-build.
            if (level.getBlockEntity(pos) instanceof BETiles tiles)
                tiles.render.onNeighbourChanged(true);
            if (needsRedraw(redrawnInteriors, pos.getX(), pos.getY(), pos.getZ()))
                client.levelRenderer.setBlocksDirty(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        }
    }

    static boolean needsRedraw(Set<Long> interiors, int x, int y, int z) {
        // Vanilla expands the dirty area by one more block than Sodium does.
        if (!interior(x) || !interior(y) || !interior(z)) return true;
        return interiors.add(net.minecraft.core.SectionPos.asLong(x >> 4, y >> 4, z >> 4));
    }

    private static boolean interior(int coordinate) {
        int local = coordinate & 15;
        return local >= 2 && local <= 13;
    }
}
