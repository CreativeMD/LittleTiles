package team.creative.littletiles.client.mod.embeddium;

import org.embeddedt.embeddium.impl.render.chunk.LocalSectionIndex;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.embeddium.entity.LittleAnimationRenderManagerEmbeddium;
import team.creative.littletiles.client.mod.embeddium.pipeline.LittleRenderPipelineEmbeddium;
import team.creative.littletiles.client.mod.embeddium.pipeline.LittleRenderPipelineTypeEmbeddium;
import team.creative.littletiles.client.mod.embeddium.renderer.DefaultChunkRendererExtender;
import team.creative.littletiles.client.render.cache.build.RenderingLevelHandler;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.level.little.LittleLevel;

public class EmbeddiumInteractor {
    
    public static final LittleRenderPipelineTypeEmbeddium PIPELINE = new LittleRenderPipelineTypeEmbeddium();
    
    public static void init() {
        LittleTiles.LOGGER.info("Loaded Embeddium extension");
        EmbeddiumManager.RENDERING_LEVEL = new RenderingLevelHandler() {
            
            @Override
            public LittleRenderPipelineType getPipeline() {
                return PIPELINE;
            }
            
            @Override
            public RenderChunkExtender getRenderChunk(Level level, long pos) {
                return LittleRenderPipelineEmbeddium.getChunk(pos);
            }
            
            @Override
            public int sectionIndex(Level level, long pos) {
                int rX = SectionPos.x(pos) & (RenderRegion.REGION_WIDTH - 1);
                int rY = SectionPos.y(pos) & (RenderRegion.REGION_HEIGHT - 1);
                int rZ = SectionPos.z(pos) & (RenderRegion.REGION_LENGTH - 1);
                
                return LocalSectionIndex.pack(rX, rY, rZ);
            }
            
            @Override
            public BlockPos standardOffset(SectionPos pos) {
                return null;
            }
        };
        EmbeddiumManager.RENDERING_ANIMATION = new RenderingLevelHandler() {
            
            @Override
            public LittleRenderPipelineType getPipeline() {
                return PIPELINE;
            }
            
            @Override
            public void prepareModelOffset(Level level, MutableBlockPos modelOffset, BlockPos pos) {
                int x = ((pos.getX() >> 4 >> DefaultChunkRendererExtender.REGION_WIDTH_SH) << DefaultChunkRendererExtender.REGION_WIDTH_SH) << 4;
                int y = ((pos.getY() >> 4 >> DefaultChunkRendererExtender.REGION_HEIGHT_SH) << DefaultChunkRendererExtender.REGION_HEIGHT_SH) << 4;
                int z = ((pos.getZ() >> 4 >> DefaultChunkRendererExtender.REGION_LENGTH_SH) << DefaultChunkRendererExtender.REGION_LENGTH_SH) << 4;
                modelOffset.set(pos.getX() - x, pos.getY() - y, pos.getZ() - z);
            }
            
            @Override
            public RenderChunkExtender getRenderChunk(Level level, long pos) {
                return ((LittleLevel) level).getRenderManager().getRenderChunk(pos);
            }
            
            @Override
            public int sectionIndex(Level level, long pos) {
                int rX = SectionPos.x(pos) & (RenderRegion.REGION_WIDTH - 1);
                int rY = SectionPos.y(pos) & (RenderRegion.REGION_HEIGHT - 1);
                int rZ = SectionPos.z(pos) & (RenderRegion.REGION_LENGTH - 1);
                
                return LocalSectionIndex.pack(rX, rY, rZ);
            }
            
            @Override
            public BlockPos standardOffset(SectionPos pos) {
                return null;
            }
            
            @Override
            public long prepareQueue(long pos) {
                return 0;
            }
        };
    }
    
    public static LittleEntityRenderManager createRenderManager(LittleAnimationEntity entity) {
        return new LittleAnimationRenderManagerEmbeddium(entity);
    }
    
}
