package team.creative.littletiles.client.level.little;

import java.util.HashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;
import team.creative.littletiles.mixin.client.level.ClientChunkCacheAccessor;

public class LittleClientChunkCache extends ClientChunkCache {
    
    private HashMap<Long, LevelChunk> chunks;
    
    public LittleClientChunkCache(ClientLevel level, int distance) {
        super(level, distance);
    }
    
    public void init(LittleClientLevel level) {
        ((ClientChunkCacheAccessor) this).setLevel(level);
        ((ClientChunkCacheAccessor) this).setLightEngine(new LevelLightEngine(this, true, level.dimensionType().hasSkyLight()));
        this.chunks = new HashMap<>();
    }
    
    public void addLoadedChunk(LevelChunk chunk) {
        chunks.put(chunk.getPos().toLong(), chunk);
        getLevel().onChunkLoaded(chunk);
    }
    
    public Iterable<LevelChunk> all() {
        return chunks.values();
    }
    
    @Override
    public void drop(int x, int z) {}
    
    @Override
    @Nullable
    public LevelChunk getChunk(int x, int z, ChunkStatus status, boolean create) {
        LevelChunk chunk = chunks.get(ChunkPos.asLong(x, z));
        if (chunk == null && create)
            chunks.put(ChunkPos.asLong(x, z), chunk = new LevelChunk(getLevel(), new ChunkPos(x, z)));
        return chunk;
    }
    
    @Override
    public LevelChunk replaceWithPacketData(int x, int z, FriendlyByteBuf buffer, CompoundTag tag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer) {
        LevelChunk chunk = getChunk(x, z, ChunkStatus.FULL, true);
        chunk.replaceWithPacketData(buffer, tag, consumer);
        getLevel().onChunkLoaded(chunk);
        MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk, false));
        return chunk;
    }
    
    @Override
    public LittleClientLevel getLevel() {
        return (LittleClientLevel) super.getLevel();
    }
    
    @Override
    public void tick(BooleanSupplier running, boolean chunks) {}
    
    @Override
    public void updateViewCenter(int x, int z) {}
    
    @Override
    public void updateViewRadius(int distance) {}
    
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
    
    public void addEntity(Entity entity) {}
    
    public void removeEntity(Entity entity) {}
    
}