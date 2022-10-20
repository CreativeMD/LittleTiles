package team.creative.littletiles.client.render.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.level.little.LittleLevel;

@OnlyIn(Dist.CLIENT)
public class LittleLevelRenderManager implements Iterable<LittleRenderChunk> {
    
    private static final Minecraft mc = Minecraft.getInstance();
    
    public final LittleLevel level;
    
    public Boolean isInSight;
    
    private HashMap<Long, LittleRenderChunk> chunks = new HashMap<>();
    
    public LittleLevelRenderManager(LittleLevel level) {
        this.level = level;
    }
    
    public synchronized LittleRenderChunk getChunk(BlockPos pos) {
        return chunks.get(SectionPos.asLong(pos));
    }
    
    public synchronized LittleRenderChunk getChunk(SectionPos pos) {
        return chunks.get(pos.asLong());
    }
    
    public CompletableFuture<Void> uploadChunkLayer(BufferBuilder.RenderedBuffer rendered, VertexBuffer buffer) {
        return LittleTilesClient.ANIMATION_HANDLER.uploadChunkLayer(rendered, buffer);
    }
    
    public void schedule(LittleRenderChunk.ChunkCompileTask task) {
        LittleTilesClient.ANIMATION_HANDLER.schedule(task);
    }
    
    public void addRecentlyCompiledChunk(LittleRenderChunk chunk) {
        LittleTilesClient.ANIMATION_HANDLER.addRecentlyCompiledChunk(chunk);
    }
    
    public ChunkBufferBuilderPack fixedBuffers() {
        return LittleTilesClient.ANIMATION_HANDLER.fixedBuffers;
    }
    
    public Vec3d getCameraPosition() {
        Vec3d vec = new Vec3d(mc.gameRenderer.getMainCamera().getPosition());
        level.getOrigin().transformPointToFakeWorld(vec);
        return vec;
    }
    
    public synchronized void backToRAM() {
        // TODO
    }
    
    public synchronized void unload() {
        for (LittleRenderChunk chunk : this)
            chunk.updateGlobalBlockEntities(Collections.EMPTY_LIST);
    }
    
    @Override
    public Iterator<LittleRenderChunk> iterator() {
        return chunks.values().iterator();
    }
    
}
