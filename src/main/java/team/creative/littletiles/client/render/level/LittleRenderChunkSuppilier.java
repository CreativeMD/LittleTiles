package team.creative.littletiles.client.render.level;

import java.util.LinkedHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.world.IRenderChunkSupplier;
import team.creative.littletiles.client.render.LittleRenderUtils;

public class LittleRenderChunkSuppilier implements IRenderChunkSupplier {
    
    @OnlyIn(Dist.CLIENT)
    public LinkedHashMap<BlockPos, LittleRenderChunk> renderChunks = new LinkedHashMap<>();
    
    @OnlyIn(Dist.CLIENT)
    public void backToRAM() {
        if (renderChunks == null)
            return;
        for (LittleRenderChunk chunk : renderChunks.values())
            chunk.backToRAM();
    }
    
    @OnlyIn(Dist.CLIENT)
    public void unloadRenderCache() {
        if (renderChunks == null)
            return;
        for (LittleRenderChunk chunk : renderChunks.values())
            chunk.unload();
        renderChunks.clear();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleRenderChunk getRenderChunk(Level level, BlockPos pos) {
        synchronized (renderChunks) {
            BlockPos renderChunkPos = LittleRenderUtils.getRenderChunkPos(pos);
            LittleRenderChunk chunk = renderChunks.get(renderChunkPos);
            if (chunk == null) {
                chunk = new LittleRenderChunk(renderChunkPos);
                renderChunks.put(renderChunkPos, chunk);
            }
            return chunk;
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        unloadRenderCache();
    }
}
