package team.creative.littletiles.common.level.little;

import java.util.HashMap;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class FakeChunkCache extends ChunkSource {
    
    public final LittleLevel level;
    private final LevelLightEngine lightEngine;
    
    private final HashMap<Long, LevelChunk> chunks = new HashMap<>();
    
    public FakeChunkCache(LittleLevel level, RegistryAccess access) {
        this.level = level;
        this.lightEngine = new LevelLightEngine(this, true, level.dimensionType().hasSkyLight());
    }
    
    @Override
    public LevelLightEngine getLightEngine() {
        return this.lightEngine;
    }
    
    @Override
    @Nullable
    public LevelChunk getChunk(int x, int z, ChunkStatus status, boolean create) {
        LevelChunk chunk = chunks.get(ChunkPos.asLong(x, z));
        if (chunk == null && create)
            chunks.put(ChunkPos.asLong(x, z), chunk = new LevelChunk(level, new ChunkPos(x, z)));
        return chunk;
    }
    
    @Override
    public BlockGetter getLevel() {
        return this.level;
    }
    
    @Override
    public void tick(BooleanSupplier p_202162_, boolean p_202163_) {}
    
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
        Minecraft.getInstance().levelRenderer.setSectionDirty(pos.x(), pos.y(), pos.z());
    }
    
    public void addEntity(Entity entity) {}
    
    public void removeEntity(Entity entity) {}
    
}