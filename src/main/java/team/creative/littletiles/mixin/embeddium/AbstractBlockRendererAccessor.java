package team.creative.littletiles.mixin.embeddium;

import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.color.ColorProviderRegistry;
import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadOrientation;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import me.jellysquid.mods.sodium.client.util.ModelQuadUtil;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.client.mod.embeddium.BlockRendererAccess;

import java.util.Arrays;

@Mixin(BlockRenderer.class)
public abstract class AbstractBlockRendererAccessor implements BlockRendererAccess {
    @Shadow
    private boolean useReorienting;

    @Shadow
    @Final
    private QuadLightData quadLightData;

    @Shadow
    @Final
    private LightPipelineProvider lighters;
    @Shadow
    @Final
    private ColorProviderRegistry colorProviderRegistry;
    @Shadow
    @Final
    private ChunkVertexEncoder.Vertex[] vertices;

    private static int mulSingleWithoutAlpha(int a, int b) {
        b &= 255;
        int c0 = (a >> 0 & 255) * b >> 8;
        int c1 = (a >> 8 & 255) * b >> 8;
        int c2 = (a >> 16 & 255) * b >> 8;
        int c3 = a >> 24 & 255;
        return c0 << 0 | c1 << 8 | c2 << 16 | c3 << 24;
    }

    @Shadow
    protected abstract LightMode getLightingMode(BlockState state, BakedModel model, BlockAndTintGetter world, BlockPos pos, RenderType renderLayer);

    @Shadow
    protected abstract int[] getVertexColors(BlockRenderContext ctx, ColorProvider<BlockState> colorProvider, BakedQuadView quad);

    @Override
    public void drawQuad(ChunkModelBuilder builder, Direction face, WorldSlice levelSlice, BakedQuadView quad, BlockState state, RenderType renderType, BlockPos pos, BlockPos origin, int color) {
        QuadLightData light = this.quadLightData;
        this.lighters.getLighter(LightMode.SMOOTH).calculate(quad, pos, light, face, quad.getLightFace(), true);
        ColorProvider<BlockState> colorizer = this.colorProviderRegistry.getColorProvider(state.getBlock());
        Vec3 offset = state.getOffset(levelSlice, pos);
        int[] vertexColors = new int[4];
        if (color != -1) {
            int color2 = ColorABGR.pack(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), ColorUtils.alpha(color));
            Arrays.fill(vertexColors, color2);
        } else if (((BakedQuad) quad).isTinted()) {
            colorizer.getColors(levelSlice, pos, state, quad, vertexColors);
        } else
            Arrays.fill(vertexColors, -1);
        this.useReorienting = true;

        ModelQuadOrientation orientation = this.useReorienting ? ModelQuadOrientation.orientByBrightness(light.br, light.lm) : ModelQuadOrientation.NORMAL;
        ChunkVertexEncoder.Vertex[] vertices = this.vertices;
        ModelQuadFacing normalFace = quad.getNormalFace();

        for (int dstIndex = 0; dstIndex < 4; ++dstIndex) {
            int srcIndex = orientation.getVertexIndex(dstIndex);
            ChunkVertexEncoder.Vertex out = vertices[dstIndex];
            out.x = (float) (origin.getX() + quad.getX(srcIndex) + offset.x);
            out.y = (float) (origin.getY() + quad.getY(srcIndex) + offset.y);
            out.z = (float) (origin.getZ() + quad.getZ(srcIndex) + offset.z);
            out.color = mulSingleWithoutAlpha(ModelQuadUtil.mixARGBColors(vertexColors[srcIndex], quad.getColor(srcIndex)), (int) (light.br[srcIndex] * 255));
            out.u = quad.getTexU(srcIndex);
            out.v = quad.getTexV(srcIndex);
            out.light = ModelQuadUtil.mergeBakedLight(quad.getLight(srcIndex), light.lm[srcIndex]);
        }

        ChunkMeshBufferBuilder vertexBuffer = builder.getVertexBuffer(normalFace);
        vertexBuffer.push(vertices, DefaultMaterials.forRenderLayer(renderType));
    }
}
