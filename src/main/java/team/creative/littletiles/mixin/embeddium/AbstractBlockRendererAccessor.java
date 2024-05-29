package team.creative.littletiles.mixin.embeddium;

import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.color.ColorProviderRegistry;
import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadOrientation;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import me.jellysquid.mods.sodium.client.util.ModelQuadUtil;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.littletiles.client.mod.embeddium.BlockRendererAccess;

import java.util.Arrays;

@Mixin(BlockRenderer.class)
public abstract class AbstractBlockRendererAccessor implements BlockRendererAccess {
    @Shadow private boolean useReorienting;

    @Shadow @Final private QuadLightData quadLightData;

    @Shadow @Final private LightPipelineProvider lighters;

    @Shadow protected abstract LightMode getLightingMode(BlockState state, BakedModel model, BlockAndTintGetter world, BlockPos pos, RenderType renderLayer);

    @Shadow protected abstract int[] getVertexColors(BlockRenderContext ctx, ColorProvider<BlockState> colorProvider, BakedQuadView quad);

    @Shadow @Final private ColorProviderRegistry colorProviderRegistry;

    @Shadow @Final private ChunkVertexEncoder.Vertex[] vertices;

    @Override
    public void drawQuad(ChunkModelBuilder builder, Direction face, WorldSlice levelSlice, BakedQuadView quad, BlockState state, RenderType renderType, BlockPos pos, BlockPos origin, int color) {
        QuadLightData light = this.quadLightData;
        this.lighters.getLighter(LightMode.SMOOTH).calculate(quad, pos, light, face, quad.getLightFace(), quad.hasShade());
        ColorProvider<BlockState> colorizer = this.colorProviderRegistry.getColorProvider(state.getBlock());
        Vec3 offset = state.getOffset(levelSlice, pos);
        int[] vertexColors = new int[4];
        if (colorizer != null) {
            colorizer.getColors(levelSlice, pos, state, quad, vertexColors);
        } else {
            Arrays.fill(vertexColors, 0xFFFFFFFF);
        }
        ModelQuadOrientation orientation = this.useReorienting ? ModelQuadOrientation.orientByBrightness(light.br, light.lm) : ModelQuadOrientation.NORMAL;
        ChunkVertexEncoder.Vertex[] vertices = this.vertices;
        ModelQuadFacing normalFace = quad.getNormalFace();

        for(int dstIndex = 0; dstIndex < 4; ++dstIndex) {
            int srcIndex = orientation.getVertexIndex(dstIndex);
            ChunkVertexEncoder.Vertex out = vertices[dstIndex];
            out.x = (float) (origin.getX() + quad.getX(srcIndex) + offset.x);
            out.y = (float) (origin.getY() + quad.getY(srcIndex) + offset.y);
            out.z = (float) (origin.getZ() + quad.getZ(srcIndex) + offset.z);
            out.color = ColorABGR.withAlpha(ModelQuadUtil.mixARGBColors(vertexColors[srcIndex], color), light.br[srcIndex]);
            out.u = quad.getTexU(srcIndex);
            out.v = quad.getTexV(srcIndex);
            out.light = ModelQuadUtil.mergeBakedLight(quad.getLight(srcIndex), light.lm[srcIndex]);
        }

        ChunkMeshBufferBuilder vertexBuffer = builder.getVertexBuffer(normalFace);
        vertexBuffer.push(vertices, DefaultMaterials.forRenderLayer(renderType));
    }
}
