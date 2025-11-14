package team.creative.littletiles.client.mod.sodium;

import javax.annotation.Nullable;

import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.LocalSectionIndex;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.sodium.entity.LittleAnimationRenderManagerSodium;
import team.creative.littletiles.client.mod.sodium.pipeline.LittleRenderPipelineSodium;
import team.creative.littletiles.client.mod.sodium.pipeline.LittleRenderPipelineTypeSodium;
import team.creative.littletiles.client.render.cache.build.RenderingLevelHandler;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.mixin.sodium.ChunkBuildBuffersAccessor;
import team.creative.littletiles.mixin.sodium.ChunkBuilderAccessor;
import team.creative.littletiles.mixin.sodium.SodiumWorldRendererAccessor;

public class SodiumInteractor {
    
    public static final LittleRenderPipelineTypeSodium PIPELINE = new LittleRenderPipelineTypeSodium();
    
    private static final int POSITION_MAX_VALUE = 1 << 20;
    
    private static final float MODEL_ORIGIN = 8.0f;
    private static final float MODEL_RANGE = 32.0f;
    
    private static volatile ChunkVertexType TYPE;
    
    public static synchronized ChunkVertexType getVertexType() {
        if (TYPE == null || TYPE.getVertexFormat() == null) {
            RenderSectionManager manager = ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager();
            if (manager == null)
                return null;
            ChunkBuilderAccessor chunkBuilder = (ChunkBuilderAccessor) manager.getBuilder();
            TYPE = ((ChunkBuildBuffersAccessor) chunkBuilder.getLocalContext().buffers).getVertexType();
        }
        return TYPE;
    }
    
    public static void init() {
        LittleTiles.LOGGER.info("Loaded Sodium extension");
        SodiumManager.RENDERING_LEVEL = new RenderingLevelHandler() {
            
            @Override
            public LittleRenderPipelineType getPipeline() {
                return PIPELINE;
            }
            
            @Override
            public RenderChunkExtender getRenderChunk(Level level, long pos) {
                return LittleRenderPipelineSodium.getChunk(pos);
            }
            
            @Override
            public int sectionIndex(Level level, long pos) {
                int rX = SectionPos.x(pos) & RenderRegion.REGION_WIDTH_M;
                int rY = SectionPos.y(pos) & RenderRegion.REGION_HEIGHT_M;
                int rZ = SectionPos.z(pos) & RenderRegion.REGION_LENGTH_M;
                
                return LocalSectionIndex.pack(rX, rY, rZ);
            }
            
            @Override
            public BlockPos standardOffset(Level level, SectionPos pos) {
                return pos.origin();
            }
        };
        SodiumManager.RENDERING_ANIMATION = new RenderingLevelHandler() {
            
            @Override
            public LittleRenderPipelineType getPipeline() {
                return PIPELINE;
            }
            
            @Override
            public RenderChunkExtender getRenderChunk(Level level, long pos) {
                return ((LittleLevel) level).getRenderManager().getRenderChunk(pos);
            }
            
            @Override
            public int sectionIndex(Level level, long pos) {
                int rX = SectionPos.x(pos) & RenderRegion.REGION_WIDTH_M;
                int rY = SectionPos.y(pos) & RenderRegion.REGION_HEIGHT_M;
                int rZ = SectionPos.z(pos) & RenderRegion.REGION_LENGTH_M;
                
                return LocalSectionIndex.pack(rX, rY, rZ);
            }
            
            @Override
            public BlockPos standardOffset(Level level, SectionPos pos) {
                return pos.origin();
            }
        };
    }
    
    public static LittleEntityRenderManager createRenderManager(LittleAnimationEntity entity) {
        return new LittleAnimationRenderManagerSodium(entity);
    }
    
    public static float unpackPositionX(int hi, int lo) {
        return unquantizePosition((((hi >> 0) & 1023) << 10) | ((lo >> 0) & 1023));
    }
    
    public static float unpackPositionY(int hi, int lo) {
        return unquantizePosition((((hi >> 10) & 1023) << 10) | ((lo >> 10) & 1023));
    }
    
    public static float unpackPositionZ(int hi, int lo) {
        return unquantizePosition((((hi >> 20) & 1023) << 10) | ((lo >> 20) & 1023));
    }
    
    public static int packPositionHi(int x, int y, int z) {
        return (x >>> 10 & 1023) << 0 | (y >>> 10 & 1023) << 10 | (z >>> 10 & 1023) << 20;
    }
    
    public static int packPositionLo(int x, int y, int z) {
        return (x & 1023) << 0 | (y & 1023) << 10 | (z & 1023) << 20;
    }
    
    public static float unquantizePosition(int position) {
        return unnormalizePosition((position & 0xFFFFF) / (float) POSITION_MAX_VALUE);
    }
    
    public static int quantizePosition(float position) {
        return ((int) (normalizePosition(position) * POSITION_MAX_VALUE)) & 0xFFFFF;
    }
    
    public static float unnormalizePosition(float v) {
        return v * MODEL_RANGE - MODEL_ORIGIN;
    }
    
    public static float normalizePosition(float v) {
        return (MODEL_ORIGIN + v) / MODEL_RANGE;
    }
    
    @Nullable
    public static Direction toDirection(ModelQuadFacing facing) {
        return switch (facing) {
            case NEG_X -> Direction.WEST;
            case NEG_Y -> Direction.DOWN;
            case NEG_Z -> Direction.NORTH;
            case POS_X -> Direction.EAST;
            case POS_Y -> Direction.UP;
            case POS_Z -> Direction.SOUTH;
            case UNASSIGNED -> null;
        };
    }
    
    public static void reload() {
        TYPE = null;
    }
    
}
