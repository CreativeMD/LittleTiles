package team.creative.littletiles.server.level.little;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.common.level.little.CreativeLevel;

public abstract class CreativeServerLevel extends CreativeLevel {
    
    private final MinecraftServer server;
    final List<ServerPlayer> players = Lists.newArrayList();
    final EntityTickList entityTickList = new EntityTickList();
    private final PersistentEntitySectionManager<Entity> entityManager;
    public boolean noSave;
    
    protected CreativeServerLevel(MinecraftServer server, WritableLevelData worldInfo, int radius, Supplier<ProfilerFiller> supplier, boolean debug, long seed) {
        super(worldInfo, radius, supplier, false, debug, seed);
        this.server = server;
        this.entityManager = new PersistentEntitySectionManager<>(Entity.class, new CreativeServerLevel.EntityCallbacks(), new EntityPersistentStorage<>() {
            
            @Override
            public CompletableFuture<ChunkEntities<Entity>> loadEntities(ChunkPos pos) {
                return CompletableFuture.completedFuture(new ChunkEntities<Entity>(pos, Collections.EMPTY_LIST));
            }
            
            @Override
            public void storeEntities(ChunkEntities<Entity> chunk) {}
            
            @Override
            public void flush(boolean p_182503_) {}
        });
    }
    
    @Override
    public MinecraftServer getServer() {
        return server;
    }
    
    @Override
    public List<? extends Player> players() {
        return players;
    }
    
    @Override
    public Entity getEntity(int id) {
        return this.getEntities().get(id);
    }
    
    @Override
    public LevelEntityGetter<Entity> getEntities() {
        return this.entityManager.getEntityGetter();
    }
    
    @Override
    public String gatherChunkSourceStats() {
        return "Chunks[C] W: " + this.getChunkSource().gatherStats() + " E: " + this.entityManager.gatherStats();
    }
    
    public boolean areEntitiesLoaded(long p_143320_) {
        return this.entityManager.areEntitiesLoaded(p_143320_);
    }
    
    public boolean isPositionEntityTicking(BlockPos p_143341_) {
        return this.entityManager.canPositionTick(p_143341_);
    }
    
    public boolean isPositionEntityTicking(ChunkPos p_143276_) {
        return this.entityManager.canPositionTick(p_143276_);
    }
    
    @Override
    public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, Context p_220406_) {}
    
    final class EntityCallbacks implements LevelCallback<Entity> {
        
        @Override
        public void onCreated(Entity entity) {}
        
        @Override
        public void onDestroyed(Entity entity) {
            CreativeServerLevel.this.getScoreboard().entityRemoved(entity);
        }
        
        @Override
        public void onTickingStart(Entity entity) {
            CreativeServerLevel.this.entityTickList.add(entity);
        }
        
        @Override
        public void onTickingEnd(Entity entity) {
            CreativeServerLevel.this.entityTickList.remove(entity);
        }
        
        @Override
        public void onTrackingStart(Entity entity) {
            CreativeServerLevel.this.getChunkSource().addEntity(entity);
            
        }
        
        @Override
        public void onTrackingEnd(Entity entity) {
            CreativeServerLevel.this.getChunkSource().removeEntity(entity);
            entity.updateDynamicGameEventListener(DynamicGameEventListener::move);
        }
        
        @Override
        public void onSectionChange(Entity entity) {}
    }
    
}
