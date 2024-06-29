package team.creative.littletiles.mixin.embeddium;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;

@Mixin(WorldSlice.class)
public class WorldSliceMixin implements RenderChunkExtender {
    @Override
    public LittleRenderPipelineType getPipeline() {
        return null;
    }

    @Override
    public void begin(BufferBuilder builder) {

    }

    @Override
    public VertexBuffer getVertexBuffer(RenderType layer) {
        return null;
    }

    @Override
    public void markReadyForUpdate(boolean playerChanged) {

    }

    @Override
    public void setQuadSorting(BufferBuilder builder, double x, double y, double z) {

    }

    @Override
    public boolean isEmpty(RenderType layer) {
        return false;
    }

    @Override
    public BufferBuilder.SortState getTransparencyState() {
        return null;
    }

    @Override
    public void setHasBlock(RenderType layer) {

    }

    @Override
    public BlockPos standardOffset() {
        return null;
    }

    @Override
    public int getQueued() {
        return 0;
    }

    @Override
    public void setQueued(int queued) {

    }

    @Override
    public ChunkLayerMap<BufferCollection> getLastUploaded() {
        return null;
    }

    @Override
    public void setLastUploaded(ChunkLayerMap<BufferCollection> uploaded) {

    }
}
