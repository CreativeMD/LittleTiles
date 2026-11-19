package team.creative.littletiles.client.render.tile;

import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.client.model.data.ModelData;
import team.creative.creativecore.client.render.box.QuadGeneratorContext;
import team.creative.creativecore.client.render.model.ModelQuadSource;
import team.creative.creativecore.common.level.LevelAccessorFake;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.render.material.ConnectedMaterialState;
import team.creative.littletiles.client.render.material.RotatedCutFaces;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleRenderBox extends RenderBox {

    @Override
    public List<BakedQuad> getBakedQuad(QuadGeneratorContext context, LevelAccessor level, BlockPos pos, BlockPos offset,
            BlockState state, BakedModel model, ModelData data, Facing facing, RenderType layer, RandomSource random,
            boolean overrideTint, int defaultColor) {
        var quads = super.getBakedQuad(context, level, pos, offset, state, model, data, facing, layer, random, overrideTint, defaultColor);
        if (pos == null || !quads.isEmpty())
            return quads;
        var direction = facing.toVanilla();
        BlockState capped = RotatedCutFaces.donor(state, direction);
        if (capped == null) {
            if (!ConnectedMaterialState.supports(state)
                    || direction.getAxis() != ConnectedMaterialState.positiveDirection(state).getAxis()) return quads;
            capped = ConnectedMaterialState.key(state);
            if (capped == state) return quads;
        }
        Level parent = level instanceof Level real ? real : level instanceof LevelAccessorFake fake ? fake.getParentLevel() : null;
        if (parent == null) return quads;
        var cappedModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(capped);
        var missingModel = Minecraft.getInstance().getModelManager().getMissingModel();
        if (model == missingModel || cappedModel == missingModel) return quads;
        var cappedView = new LevelAccessorFake();
        cappedView.set(parent, pos, capped);
        var cappedData = ModelQuadSource.prepare(
            cappedModel.getModelData(cappedView, pos, capped, level.getModelData(pos)), state.getSeed(pos));
        // Same native collection and clipping path; the capped state prevents recursion.
        return super.getBakedQuad(context, cappedView, pos, offset, capped, cappedModel, cappedData, facing, layer,
            RandomSource.create(state.getSeed(pos)), overrideTint, defaultColor);
    }
    
    public LittleBox box;
    
    public LittleRenderBox(AlignedBox box) {
        super(box);
    }
    
    public LittleRenderBox(AlignedBox box, BlockState state) {
        super(box, state);
    }
    
    public LittleRenderBox(LittleGrid grid, LittleBox box) {
        super(grid.toVanillaGridF(box.minX), grid.toVanillaGridF(box.minY), grid.toVanillaGridF(box.minZ), grid.toVanillaGridF(box.maxX), grid.toVanillaGridF(box.maxY), grid
                .toVanillaGridF(box.maxZ), (BlockState) null);
        this.color = ColorUtils.WHITE;
        this.box = box;
    }
    
    public LittleRenderBox(LittleGrid grid, LittleBox box, BlockState state) {
        super(grid.toVanillaGridF(box.minX), grid.toVanillaGridF(box.minY), grid.toVanillaGridF(box.minZ), grid.toVanillaGridF(box.maxX), grid.toVanillaGridF(box.maxY), grid
                .toVanillaGridF(box.maxZ), state);
        this.color = ColorUtils.WHITE;
        this.box = box;
    }
    
    public LittleRenderBox(LittleGrid grid, LittleBox box, LittleElement element) {
        super(grid.toVanillaGridF(box.minX), grid.toVanillaGridF(box.minY), grid.toVanillaGridF(box.minZ), grid.toVanillaGridF(box.maxX), grid.toVanillaGridF(box.maxY), grid
                .toVanillaGridF(box.maxZ), element.getState());
        this.color = element.color;
        this.box = box;
    }
    
    @Override
    public LittleRenderBox setColor(int color) {
        return (LittleRenderBox) super.setColor(color);
    }
    
}
