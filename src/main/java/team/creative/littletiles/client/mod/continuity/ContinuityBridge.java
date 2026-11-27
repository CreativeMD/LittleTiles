package team.creative.littletiles.client.mod.continuity;

import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import team.creative.creativecore.client.render.model.ModelQuadSource;
import team.creative.creativecore.common.level.LevelAccessorFake;

/** Loaded only when Continuity and its FRAPI model interface are present. */
public final class ContinuityBridge {
    private static boolean reportedFailure;
    private ContinuityBridge() {}
    public static void init() { ModelQuadSource.register(ContinuityBridge::collect); }

    private static List<BakedQuad> collect(BakedModel model, LevelAccessor level, BlockPos pos, BlockState state,
            Direction side, RandomSource random, ModelData data, RenderType layer) {
        BlockAndTintGetter view = level instanceof LevelAccessorFake fake && fake.getParentLevel() != null
            ? new MaterialBlockView(fake.getParentLevel(), fake.getMaterialPos(), fake.getMaterialState())
            : level instanceof BlockAndTintGetter getter ? getter : null;
        if (view == null || pos == null) return null;
        try {
            return CtmQuadCollector.collect(model, view, pos, state, side, random, data, layer);
        } catch (RuntimeException | LinkageError failure) {
            if (!reportedFailure) {
                reportedFailure = true;
                CtmLog.LOGGER.error("LittleTiles Continuity collection failed; falling back to native model quads", failure);
            }
            return null;
        }
    }
}
