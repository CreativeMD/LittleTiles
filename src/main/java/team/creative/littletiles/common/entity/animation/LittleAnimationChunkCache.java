package team.creative.littletiles.common.entity.animation;

import java.util.HashMap;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class LittleAnimationChunkCache extends ChunkSource {
    
    private final LittleAnimationLevel level;
    private HashMap<Long, LevelChunk> chunks;
    private LevelLightEngine lightEngine;
    
    public LittleAnimationChunkCache(LittleAnimationLevel level) {
        this.level = level;
        this.chunks = new HashMap<>();
        this.lightEngine = new LevelLightEngine(this, false, false);
    }
    
    public void addLoadedChunk(LevelChunk chunk) {
        chunks.put(chunk.getPos().toLong(), chunk);
    }
    
    public Iterable<LevelChunk> all() {
        return chunks.values();
    }
    
    @Override
    @Nullable
    public LevelChunk getChunk(int x, int z, ChunkStatus status, boolean create) {
        LevelChunk chunk = chunks.get(ChunkPos.asLong(x, z));
        if (chunk == null && create) {
            chunk = new LevelChunk(level, new ChunkPos(x, z));
            chunk.setLoaded(true);
            chunks.put(ChunkPos.asLong(x, z), chunk);
        }
        return chunk;
    }
    
    @Override
    public LittleAnimationLevel getLevel() {
        return level;
    }
    
    @Override
    public void tick(BooleanSupplier running, boolean chunks) {}
    
    @Override
    public String gatherStats() {
        return "" + this.getLoadedChunksCount();
    }
    
    @Override
    public int getLoadedChunksCount() {
        return this.chunks.size();
    }
    
    @Override
    public void onLightUpdate(LightLayer layer, SectionPos pos) {
        getLevel().renderManager.setSectionDirty(pos.x(), pos.y(), pos.z());
    }
    
    @Override
    public LevelLightEngine getLightEngine() {
        return lightEngine;
    }
}
