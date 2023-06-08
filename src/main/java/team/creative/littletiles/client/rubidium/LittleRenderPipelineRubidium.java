package team.creative.littletiles.client.rubidium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexBufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipeline;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.mixin.rubidium.RenderSectionManagerAccessor;

public class LittleRenderPipelineRubidium extends LittleRenderPipeline {
    
    public static final LittleRenderPipelineRubidium INSTANCE = new LittleRenderPipelineRubidium();
    
    public static RenderChunkExtender getChunk(BlockPos pos) {
        return (RenderChunkExtender) ((RenderSectionManagerAccessor) SodiumWorldRenderer.instance().getRenderSectionManager())
                .callGetRenderSection(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getY()), SectionPos.blockToSectionCoord(pos.getZ()));
    }
    
    @Override
    public void buildCache(PoseStack pose, ChunkLayerMap<BufferHolder> buffers, RenderingBlockContext context, VertexFormat format, SingletonList<BakedQuad> bakedQuadWrapper) {
        ChunkBuilder builder = SodiumWorldRenderer.instance().getRenderSectionManager().getBuilder();
        ChunkVertexBufferBuilder vertexBuilder;
        //ChunkBuildContext context;
        BlockRenderer renderer;
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void startCompile(RenderChunkExtender chunk) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void endCompile(RenderChunkExtender chunk, RebuildTaskExtender task) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void add(RenderChunkExtender chunk, BETiles be, RebuildTaskExtender rebuildTask) {
        // TODO Auto-generated method stub
        
    }
    
}
