package team.creative.littletiles.client.render.cache.pipeline;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.lighting.QuadLighter;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.NeoForgeConfig;
import team.creative.creativecore.client.render.model.CreativeQuadLighter;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.creativecore.mixin.ForgeModelBlockRendererAccessor;
import team.creative.littletiles.api.client.IFakeRenderingBlock;
import team.creative.littletiles.client.mod.iris.IrisManager;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.mixin.client.render.BufferBuilderAccessor;

@OnlyIn(Dist.CLIENT)
public class LittleRenderPipelineForge extends LittleRenderPipeline {
    
    private final ChunkLayerMap<ByteBufferBuilder> byteBuilder = new ChunkLayerMap<>();
    private final ChunkLayerMap<IntArrayList> indexes = new ChunkLayerMap<>(layer -> new IntArrayList());
    private final MutableBlockPos modelOffset = new MutableBlockPos();
    private final float[] shape = new float[12];
    private final BitSet shapeFlags = new BitSet(3);
    private ModelBlockRenderer.AmbientOcclusionFace aoFace;
    private ModelBlockRenderer.AmbientOcclusionFace exteriorFace;
    
    private ByteBufferBuilder getOrCreate(RenderType layer) {
        ByteBufferBuilder buffer = byteBuilder.get(layer);
        if (buffer == null)
            byteBuilder.put(layer, buffer = new ByteBufferBuilder(layer.bufferSize()));
        return buffer;
    }
    
    private void clearIndexes() {
        for (IntArrayList list : indexes)
            list.clear();
    }
    
    @Override
    public void buildCache(PoseStack pose, ChunkLayerMap<BufferCache> buffers, RenderingBlockContext data, VertexFormat format, SingletonList<BakedQuad> bakedQuadWrapper) {
        Level renderLevel = data.be.getLevel();
        while (renderLevel instanceof LittleSubLevel sub && !sub.shouldUseLightingForRenderig())
            renderLevel = sub.getParent();
        
        BlockPos pos = data.be.getBlockPos();
        
        ForgeModelBlockRendererAccessor renderer = (ForgeModelBlockRendererAccessor) MC.getBlockRenderer().getModelRenderer();
        
        QuadLighter smoothLighter = null;
        QuadLighter flatLighter = null;
        boolean vanillaLighting = !NeoForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.get();
        
        int overlay = OverlayTexture.NO_OVERLAY;
        
        data.prepareModelOffset(modelOffset, pos);
        pose.translate(modelOffset.getX(), modelOffset.getY(), modelOffset.getZ());
        
        ChunkLayerMap<BufferBuilder> builders = new ChunkLayerMap<>();
        
        for (Entry<ChunkLayerMapList<LittleRenderBox>> entry : data.be.render.cachedBoxes().int2ObjectEntrySet()) {
            ChunkLayerMapList<LittleRenderBox> structureMap = entry.getValue();
            if (structureMap == null || structureMap.isEmpty())
                continue;
            
            for (Tuple<RenderType, List<LittleRenderBox>> tuple : structureMap.tuples()) {
                var cubes = tuple.value;
                if (cubes.isEmpty())
                    continue;
                
                BufferBuilder builder = builders.get(tuple.key);
                if (builder == null)
                    builders.put(tuple.key, builder = new BufferBuilder(getOrCreate(tuple.key), VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK));
                
                for (LittleRenderBox cube : cubes) {
                    BlockState state = cube.state;
                    
                    QuadLighter lighter;
                    if (Minecraft.useAmbientOcclusion() && state.getLightEmission(data.be.getLevel(), pos) == 0) {
                        if (smoothLighter == null) {
                            smoothLighter = renderer.getSmoothLighter().get();
                            smoothLighter.setup(renderLevel, pos, data.state);
                        }
                        lighter = smoothLighter;
                    } else {
                        if (flatLighter == null) {
                            flatLighter = renderer.getFlatLighter().get();
                            flatLighter.setup(renderLevel, pos, data.state);
                        }
                        lighter = flatLighter;
                    }
                    
                    ((CreativeQuadLighter) lighter).setState(state);
                    ((CreativeQuadLighter) lighter).setCustomTint(cube.color);
                    
                    if (IrisManager.isShaders()) {
                        if (state.getBlock() instanceof IFakeRenderingBlock fake)
                            state = fake.getFakeState(state);
                        // Iris computes at_midBlock from the submitted vertex positions.
                        // Use their local origin; Sable's large storage positions lose
                        // sub-block precision when Iris converts them to floats.
                        IrisManager.beginBlock(builder, state, modelOffset);
                    }
                    
                    for (int h = 0; h < Facing.VALUES.length; h++) {
                        Facing facing = Facing.VALUES[h];
                        Object quadObject = cube.getQuad(facing);
                        List<BakedQuad> quads = null;
                        if (quadObject instanceof List q)
                            quads = q;
                        else if (quadObject instanceof BakedQuad quad) {
                            bakedQuadWrapper.setElement(quad);
                            quads = bakedQuadWrapper;
                        }
                        if (quads != null && !quads.isEmpty())
                            if (quads instanceof SingletonList<BakedQuad> single)
                                processQuad(lighter, vanillaLighting, lighter == smoothLighter, renderLevel, data.state, pos, builder, pose.last(), single.get(0), overlay);
                            else
                                for (BakedQuad quad : quads)
                                    processQuad(lighter, vanillaLighting, lighter == smoothLighter, renderLevel, data.state, pos, builder, pose.last(), quad, overlay);
                    }
                    
                    IrisManager.resetBlockContext(builder);
                    
                    bakedQuadWrapper.setElement(null);
                    
                    cube.deleteQuadCache();
                }
                
                var indexList = indexes.get(tuple.key);
                indexList.add(entry.getIntKey());
                indexList.add(((BufferBuilderAccessor) builder).getVertices() * format.getVertexSize());
            }
        }
        
        for (RenderType layer : RenderType.CHUNK_BUFFER_LAYERS) {
            var builder = builders.get(layer);
            if (builder == null)
                continue;
            var mesh = builder.build();
            if (mesh != null)
                buffers.put(layer, new BufferHolder(mesh, indexes.get(layer).toIntArray()));
        }
        
        clearIndexes();
        
        if (smoothLighter != null) {
            ((CreativeQuadLighter) smoothLighter).setCustomTint(-1);
            smoothLighter.reset();
        }
        if (flatLighter != null) {
            ((CreativeQuadLighter) flatLighter).setCustomTint(-1);
            flatLighter.reset();
        }
    }
    
    private void processQuad(QuadLighter lighter, boolean vanillaLighting, boolean smooth, BlockAndTintGetter level, BlockState containerState,
            BlockPos pos, VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, int overlay) {
        if (!vanillaLighting) {
            lighter.process(consumer, pose, quad, overlay);
            return;
        }

        // Classify the clipped geometry using the LT container, not its uncut material.
        MC.getBlockRenderer().getModelRenderer().calculateShape(level, containerState, pos, quad.getVertices(), quad.getDirection(), shape, shapeFlags);
        if (shape[quad.getDirection().get3DDataValue()] != shape[quad.getDirection().getOpposite().get3DDataValue()]) {
            // Preserve NeoForge's normal-based lighting for sloped faces.
            lighter.process(consumer, pose, quad, overlay);
            return;
        }

        if (aoFace == null)
            aoFace = new ModelBlockRenderer.AmbientOcclusionFace();
        calculateLighting(smooth, level, containerState, pos, quad, aoFace);
        if (!shapeFlags.get(0)) {
            // Vanilla switches sample layers abruptly at the block boundary. For
            // an inset face, interpolate those layers using its actual depth.
            float coordinate = shape[quad.getDirection().get3DDataValue()];
            float exteriorWeight = Math.clamp(quad.getDirection().getAxisDirection() == Direction.AxisDirection.POSITIVE ? coordinate : 1 - coordinate, 0, 1);
            if (exteriorWeight > 0) {
                if (exteriorFace == null)
                    exteriorFace = new ModelBlockRenderer.AmbientOcclusionFace();
                shapeFlags.set(0);
                try {
                    calculateLighting(smooth, level, containerState, pos, quad, exteriorFace);
                } finally {
                    shapeFlags.clear(0);
                }
                blendLighting(aoFace, exteriorFace, exteriorWeight);
            }
        }

        // Keep CreativeCore's tile-state/custom-tint handling and LT's existing buffers.
        float[] tint = quad.isTinted() ? lighter.getColorFast(quad.getTintIndex()) : null;
        consumer.putBulkData(pose, quad, aoFace.brightness, tint == null ? 1 : tint[0], tint == null ? 1 : tint[1],
                tint == null ? 1 : tint[2], 1, aoFace.lightmap, overlay, true);
    }

    private void calculateLighting(boolean smooth, BlockAndTintGetter level, BlockState containerState, BlockPos pos, BakedQuad quad,
            ModelBlockRenderer.AmbientOcclusionFace face) {
        if (smooth) {
            if (!ClientHooks.calculateFaceWithoutAO(level, containerState, pos, quad, shapeFlags.get(0), face.brightness, face.lightmap))
                face.calculate(level, containerState, pos, quad.getDirection(), shape, shapeFlags, quad.isShade());
        } else {
            BlockPos samplePos = shapeFlags.get(0) ? pos.relative(quad.getDirection()) : pos;
            Arrays.fill(face.lightmap, LevelRenderer.getLightColor(level, containerState, samplePos));
            Arrays.fill(face.brightness, level.getShade(quad.getDirection(), quad.isShade()));
        }
    }

    private static void blendLighting(ModelBlockRenderer.AmbientOcclusionFace interior, ModelBlockRenderer.AmbientOcclusionFace exterior, float weight) {
        for (int i = 0; i < 4; i++) {
            interior.brightness[i] += (exterior.brightness[i] - interior.brightness[i]) * weight;
            int inner = interior.lightmap[i];
            int outer = exterior.lightmap[i];
            int block = Math.round((inner & 0xFFFF) + ((outer & 0xFFFF) - (inner & 0xFFFF)) * weight);
            int sky = Math.round((inner >>> 16) + ((outer >>> 16) - (inner >>> 16)) * weight);
            interior.lightmap[i] = block | sky << 16;
        }
    }

    @Override
    public void reload() {}
    
    @Override
    public void release() {}
    
}
