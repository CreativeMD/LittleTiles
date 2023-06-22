package team.creative.littletiles.client.render.mc;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipeline.LittleRenderPipelineType;

public interface RenderChunkExtender {
    
    public static Vec3 offsetCorrection(Vec3i to, Vec3i from) {
        if (to == from)
            return Vec3.ZERO;
        return new Vec3(from.getX() - to.getX(), from.getY() - to.getY(), from.getZ() - to.getZ());
    }
    
    public LittleRenderPipelineType getPipeline();
    
    public void begin(BufferBuilder builder);
    
    public VertexBuffer getVertexBuffer(RenderType layer);
    
    public void markReadyForUpdate(boolean playerChanged);
    
    public default void setQuadSorting(BufferBuilder builder, Vec3 vec) {
        setQuadSorting(builder, vec.x, vec.y, vec.z);
    }
    
    public void setQuadSorting(BufferBuilder builder, double x, double y, double z);
    
    public default void prepareBlockTranslation(PoseStack posestack, BlockPos pos) {
        posestack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }
    
    public boolean isEmpty(RenderType layer);
    
    public SortState getTransparencyState();
    
    public void setHasBlock(RenderType layer);
    
    public BlockPos standardOffset();
    
    public default Vec3 offsetCorrection(RenderChunkExtender chunk) {
        return offsetCorrection(standardOffset(), chunk.standardOffset());
    }
    
    public default int chunkId() {
        return -1;
    }
    
}
