package team.creative.littletiles.client.render.cache.build;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.littletiles.client.level.little.FakeClientLevel;
import team.creative.littletiles.client.mod.embeddium.EmbeddiumManager;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.ViewAreaExtender;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.mixin.client.render.LevelRendererAccessor;

public abstract class RenderingLevelHandler {
    
    public static Vec3 offsetCorrection(Vec3i to, Vec3i from) {
        if (to == from || to.equals(from))
            return null;
        return new Vec3(from.getX() - to.getX(), from.getY() - to.getY(), from.getZ() - to.getZ());
    }
    
    public static Vec3 offsetCorrection(Vec3i to, int x, int y, int z) {
        if (to.getX() == x && to.getY() == y && to.getZ() == z)
            return null;
        return new Vec3(x - to.getX(), y - to.getY(), z - to.getZ());
    }
    
    public static Vec3 offsetCorrection(RenderingLevelHandler target, Level targetLevel, RenderingLevelHandler origin, Level originLevel, SectionPos pos) {
        var targetOffset = target.standardOffset(targetLevel, pos);
        var originOffset = origin.standardOffset(originLevel, pos);
        if (targetOffset != null && originOffset != null)
            return offsetCorrection(targetOffset, originOffset);
        return null;
    }
    
    public static final RenderingLevelHandler VANILLA = new RenderingLevelHandler() {
        
        @Override
        public LittleRenderPipelineType getPipeline() {
            return LittleRenderPipelineType.FORGE;
        }
        
        @Override
        public RenderChunkExtender getRenderChunk(Level level, long pos) {
            return (RenderChunkExtender) ((ViewAreaExtender) ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getViewArea()).getSection(pos);
        }
        
        @Override
        public BlockPos standardOffset(Level level, SectionPos pos) {
            return pos.origin();
        }
    };
    
    public static final RenderingLevelHandler ENTITY = new RenderingLevelHandler() {
        
        @Override
        public LittleRenderPipelineType getPipeline() {
            return LittleRenderPipelineType.FORGE;
        }
        
        @Override
        public RenderChunkExtender getRenderChunk(Level level, long pos) {
            return ((LittleLevel) level).getRenderManager().getRenderChunk(pos);
        }
        
        @Override
        public BlockPos standardOffset(Level level, SectionPos pos) {
            return pos.origin();
        }
    };
    
    public static final RenderingLevelHandler ANIMATION = new RenderingLevelHandler() {
        
        @Override
        public LittleRenderPipelineType getPipeline() {
            return LittleRenderPipelineType.FORGE;
        }
        
        @Override
        public void prepareModelOffset(Level level, MutableBlockPos modelOffset, BlockPos pos) {
            BlockPos chunkOffset = ((LittleAnimationEntity) ((LittleLevel) level).getHolder()).getCenter().chunkOrigin;
            modelOffset.set(pos.getX() - chunkOffset.getX(), pos.getY() - chunkOffset.getY(), pos.getZ() - chunkOffset.getZ());
        }
        
        @Override
        public RenderChunkExtender getRenderChunk(Level level, long pos) {
            return ((LittleLevel) level).getRenderManager().getRenderChunk(pos);
        }
        
        @Override
        public BlockPos standardOffset(Level level, SectionPos pos) {
            return ((LittleAnimationEntity) ((LittleLevel) level).getHolder()).getCenter().chunkOrigin;
        }
        
        @Override
        public long prepareQueue(long pos) {
            return 0;
        }
    };
    
    public static RenderingLevelHandler of(Level level) {
        if (EmbeddiumManager.installed())
            if (level instanceof LittleLevel l && l.getRenderManager().isSmall())
                if (l instanceof ISubLevel s && s.getParent() instanceof FakeClientLevel)
                    return ANIMATION;
                else
                    return EmbeddiumManager.RENDERING_ANIMATION;
            else
                return EmbeddiumManager.RENDERING_LEVEL;
        if (level instanceof LittleLevel l)
            return l.getRenderManager().isSmall() ? ANIMATION : ENTITY;
        return VANILLA;
    }
    
    public abstract LittleRenderPipelineType getPipeline();
    
    public void prepareModelOffset(Level level, MutableBlockPos modelOffset, BlockPos pos) {
        modelOffset.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }
    
    public long prepareQueue(long pos) {
        return pos;
    }
    
    public abstract RenderChunkExtender getRenderChunk(Level level, long pos);
    
    public int sectionIndex(Level level, long pos) {
        return -1;
    }
    
    public abstract BlockPos standardOffset(Level level, SectionPos pos);
    
}
