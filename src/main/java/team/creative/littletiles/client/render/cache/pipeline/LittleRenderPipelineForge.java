package team.creative.littletiles.client.render.cache.pipeline;

import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.lighting.QuadLighter;
import team.creative.creativecore.client.render.model.CreativeQuadLighter;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.creativecore.mixin.ForgeModelBlockRendererAccessor;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.sodium.SodiumManager;
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
        boolean smooth = Minecraft.useAmbientOcclusion() && data.state.getLightEmission(data.be.getLevel(), pos) == 0;
        QuadLighter lighter = smooth ? renderer.getSmoothLighter().get() : renderer.getFlatLighter().get();
        
        lighter.setup(renderLevel, pos, data.state);
        
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
                    
                    ((CreativeQuadLighter) lighter).setState(state);
                    ((CreativeQuadLighter) lighter).setCustomTint(cube.color);
                    
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
                                lighter.process(builder, pose.last(), single.get(0), overlay);
                            else
                                for (BakedQuad quad : quads)
                                    lighter.process(builder, pose.last(), quad, overlay);
                    }
                    
                    bakedQuadWrapper.setElement(null);
                    
                    if (!LittleTiles.CONFIG.rendering.useQuadCache)
                        cube.deleteQuadCache();
                }
                
                var indexList = indexes.get(tuple.key);
                indexList.add(entry.getIntKey());
                indexList.add(((BufferBuilderAccessor) builder).getVertices() * format.getVertexSize());
            }
        }
        
        for (RenderType layer : SodiumManager.chunkBufferLayers()) {
            var builder = builders.get(layer);
            if (builder == null)
                continue;
            var mesh = builder.build();
            if (mesh != null)
                buffers.put(layer, new BufferHolder(mesh, indexes.get(layer).toIntArray()));
        }
        
        clearIndexes();
        ((CreativeQuadLighter) lighter).setCustomTint(-1);
        lighter.reset();
    }
    
    @Override
    public void reload() {}
    
    @Override
    public void release() {}
    
}
