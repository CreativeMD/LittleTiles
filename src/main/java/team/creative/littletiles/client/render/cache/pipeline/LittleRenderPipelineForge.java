package team.creative.littletiles.client.render.cache.pipeline;

import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.lighting.QuadLighter;
import team.creative.creativecore.client.render.model.CreativeQuadLighter;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.IndexedCollector;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.creativecore.mixin.BufferBuilderAccessor;
import team.creative.creativecore.mixin.ForgeModelBlockRendererAccessor;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.level.little.LittleSubLevel;

@OnlyIn(Dist.CLIENT)
public class LittleRenderPipelineForge extends LittleRenderPipeline {
    
    private final BufferBuilder builder = new BufferBuilder(131072);
    private final MutableBlockPos modelOffset = new MutableBlockPos();
    
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
        
        data.chunk.prepareModelOffset(modelOffset, pos);
        pose.translate(modelOffset.getX(), modelOffset.getY(), modelOffset.getZ());
        
        // Render vertex buffer
        for (Tuple<RenderType, IndexedCollector<LittleRenderBox>> entry : data.be.render.boxCache.tuples()) {
            
            IndexedCollector<LittleRenderBox> cubes = entry.value;
            if (cubes == null || cubes.isEmpty())
                continue;
            
            builder.begin(VertexFormat.Mode.QUADS, format);
            
            IntArrayList indexes = new IntArrayList();
            for (Iterator<LittleRenderBox> iterator = cubes.sectionIterator(x -> {
                indexes.add(x);
                indexes.add(((BufferBuilderAccessor) builder).getVertices() * format.getVertexSize());
            });iterator.hasNext();) {
                LittleRenderBox cube = iterator.next();
                BlockState state = cube.state;
                
                ((CreativeQuadLighter) lighter).setState(state);
                ((CreativeQuadLighter) lighter).setCustomTint(cube.color);
                
                for (int h = 0; h < Facing.VALUES.length; h++) {
                    Facing facing = Facing.VALUES[h];
                    Object quadObject = cube.getQuad(facing);
                    List<BakedQuad> quads = null;
                    if (quadObject instanceof List) {
                        quads = (List<BakedQuad>) quadObject;
                    } else if (quadObject instanceof BakedQuad quad) {
                        bakedQuadWrapper.setElement(quad);
                        quads = bakedQuadWrapper;
                    }
                    if (quads != null && !quads.isEmpty())
                        for (BakedQuad quad : quads)
                            lighter.process(builder, pose.last(), quad, overlay);
                }
                
                bakedQuadWrapper.setElement(null);
                
                if (!LittleTiles.CONFIG.rendering.useQuadCache)
                    cube.deleteQuadCache();
            }
            
            buffers.put(entry.key, new BufferHolder(builder.end(), indexes.toIntArray()));
        }
        
        ((CreativeQuadLighter) lighter).setCustomTint(-1);
        lighter.reset();
    }
    
    @Override
    public void reload() {}
    
    @Override
    public void release() {}
    
}
