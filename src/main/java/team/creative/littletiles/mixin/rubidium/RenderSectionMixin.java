package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import com.mojang.blaze3d.vertex.VertexBuffer;

import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Vec3i;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipeline;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.rubidium.LittleRenderPipelineRubidium;

@Mixin(RenderSection.class)
public abstract class RenderSectionMixin implements RenderChunkExtender {
    
    @Shadow(remap = false)
    public abstract void markForUpdate(ChunkUpdateType type);
    
    @Override
    public void begin(BufferBuilder builder) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public VertexBuffer getVertexBuffer(RenderType layer) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void markReadyForUpdate(boolean playerChanged) {
        markForUpdate(playerChanged ? ChunkUpdateType.IMPORTANT_REBUILD : ChunkUpdateType.REBUILD);
    }
    
    @Override
    public void setQuadSorting(BufferBuilder builder, double x, double y, double z) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public boolean isEmpty(RenderType layer) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public SortState getTransparencyState() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void setHasBlock(RenderType layer) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public Vec3i standardOffset() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public LittleRenderPipeline getPipeline() {
        return LittleRenderPipelineRubidium.INSTANCE;
    }
}
