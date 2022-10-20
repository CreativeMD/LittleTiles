package team.creative.littletiles.client.level;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelDataManager;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import team.creative.littletiles.common.level.little.CreativeLevel;

@OnlyIn(Dist.CLIENT)
public abstract class CreativeClientLevel extends CreativeLevel {
    
    final EntityTickList tickingEntities = new EntityTickList();
    private final TransientEntitySectionManager<Entity> entityStorage = new TransientEntitySectionManager<>(Entity.class, new CreativeClientLevel.EntityCallbacks());
    final List<AbstractClientPlayer> players = Lists.newArrayList();
    private final Map<String, MapItemSavedData> mapData = Maps.newHashMap();
    private final ModelDataManager modelDataManager = new ModelDataManager(this);
    
    protected CreativeClientLevel(WritableLevelData worldInfo, int radius, Supplier<ProfilerFiller> supplier, boolean debug, long seed) {
        super(worldInfo, radius, supplier, true, debug, seed);
    }
    
    @Override
    public ModelDataManager getModelDataManager() {
        return modelDataManager;
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
        return this.entityStorage.getEntityGetter();
    }
    
    @Override
    public String gatherChunkSourceStats() {
        return "Chunks[C] W: " + this.getChunkSource().gatherStats() + " E: " + this.entityStorage.gatherStats();
    }
    
    @Override
    public MapItemSavedData getMapData(String data) {
        return this.mapData.get(data);
    }
    
    @Override
    public void setMapData(String id, MapItemSavedData data) {
        this.mapData.put(id, data);
    }
    
    @Override
    public void sendBlockUpdated(BlockPos p_104685_, BlockState p_104686_, BlockState p_104687_, int p_104688_) {
        this.levelRenderer.blockChanged(this, p_104685_, p_104686_, p_104687_, p_104688_);
    }
    
    @Override
    public void setBlocksDirty(BlockPos p_104759_, BlockState p_104760_, BlockState p_104761_) {
        this.levelRenderer.setBlockDirty(p_104759_, p_104760_, p_104761_);
    }
    
    public void setSectionDirtyWithNeighbors(int p_104794_, int p_104795_, int p_104796_) {
        this.levelRenderer.setSectionDirtyWithNeighbors(p_104794_, p_104795_, p_104796_);
    }
    
    public void setLightReady(int p_197406_, int p_197407_) {
        LevelChunk levelchunk = this.chunkSource.getChunk(p_197406_, p_197407_, false);
        if (levelchunk != null) {
            levelchunk.setClientLightReady(true);
        }
        
    }
    
    final class EntityCallbacks implements LevelCallback<Entity> {
        
        @Override
        public void onCreated(Entity entity) {}
        
        @Override
        public void onDestroyed(Entity entity) {}
        
        @Override
        public void onTickingStart(Entity entity) {
            CreativeClientLevel.this.tickingEntities.add(entity);
        }
        
        @Override
        public void onTickingEnd(Entity entity) {
            CreativeClientLevel.this.tickingEntities.remove(entity);
        }
        
        @Override
        public void onTrackingStart(Entity entity) {
            if (entity instanceof AbstractClientPlayer)
                CreativeClientLevel.this.players.add((AbstractClientPlayer) entity);
        }
        
        @Override
        public void onTrackingEnd(Entity entity) {
            entity.unRide();
            CreativeClientLevel.this.players.remove(entity);
            
            entity.onRemovedFromWorld();
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new EntityLeaveLevelEvent(entity, CreativeClientLevel.this));
        }
        
        @Override
        public void onSectionChange(Entity entity) {}
    }
    
}
