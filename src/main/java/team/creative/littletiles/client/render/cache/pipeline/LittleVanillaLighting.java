package team.creative.littletiles.client.render.cache.pipeline;

import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.TriState;

/** Feeds the already clipped tile quads through vanilla's partial-face lighting. */
final class LittleVanillaLighting {
    private final TileColors colors = new TileColors();
    private final ModelBlockRenderer renderer = new ModelBlockRenderer(colors);
    private final RandomSource random = RandomSource.create();
    private TileModel model;
    private boolean smooth;

    void prepare(BlockAndTintGetter level, BlockPos pos, BlockState tileState, int color, RenderType layer) {
        colors.state = tileState;
        colors.color = color;
        BakedModel original = Minecraft.getInstance().getBlockRenderer().getBlockModel(tileState);
        model = new TileModel(original);
        TriState ao = original.useAmbientOcclusion(tileState, ModelData.EMPTY, layer);
        smooth = Minecraft.useAmbientOcclusion() && ao != TriState.FALSE
                && (ao == TriState.TRUE || tileState.getLightEmission(level, pos) == 0);
    }

    void render(BlockAndTintGetter level, BlockPos pos, BlockState containerState, PoseStack pose,
            VertexConsumer consumer, List<BakedQuad> quads, RenderType layer, int overlay) {
        model.quads = quads;
        boolean previousInsetScope = LittleInsetLighting.begin();
        try {
            // Shape classification must use the actual container, not the full cube
            // material that was cut down to make the tile. Tints use the tile instead.
            if (smooth)
                renderer.tesselateWithAO(level, model, containerState, pos, pose, consumer, false, random, 0L, overlay, ModelData.EMPTY, layer);
            else
                renderer.tesselateWithoutAO(level, model, containerState, pos, pose, consumer, false, random, 0L, overlay, ModelData.EMPTY, layer);
        } finally {
            LittleInsetLighting.end(previousInsetScope);
            model.quads = List.of();
        }
    }

    private static final class TileColors extends BlockColors {
        private BlockState state;
        private int color;
        @Override
        public int getColor(BlockState ignored, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
            return color != -1 ? color : Minecraft.getInstance().getBlockColors().getColor(state, level, pos, tintIndex);
        }
    }

    private static final class TileModel extends BakedModelWrapper<BakedModel> {
        private List<BakedQuad> quads = List.of();
        TileModel(BakedModel original) { super(original); }
        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
            return side == null ? quads : List.of();
        }
        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random, ModelData data, RenderType layer) {
            return getQuads(state, side, random);
        }
    }
}
