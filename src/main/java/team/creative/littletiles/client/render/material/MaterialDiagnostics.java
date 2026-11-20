package team.creative.littletiles.client.render.material;

import java.util.HashSet;
import java.util.Set;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Bounded experimental diagnostics, including unchanged and unavailable decisions. */
public final class MaterialDiagnostics {
    private static final Set<String> REPORTED = new HashSet<>();
    public static final boolean ENABLED = Boolean.getBoolean("yuushya.lt.diagnostics");
    private MaterialDiagnostics() {}

    public static void record(String route, BlockPos pos, BlockState input,
            NeighborMaterials<BlockState> negative, NeighborMaterials<BlockState> positive, BlockState output) {
        if (!ENABLED) return;
        synchronized (REPORTED) {
        if (REPORTED.size() >= 96) return;
        String decision = route + " at " + pos + " input=" + input + " negative=" + negative
            + " positive=" + positive + " output=" + output;
        if (REPORTED.add(decision)) LogUtils.getLogger().info("LT pillar decision: {}", decision);
        }
    }
}
