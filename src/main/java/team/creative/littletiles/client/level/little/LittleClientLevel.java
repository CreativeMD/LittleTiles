package team.creative.littletiles.client.level.little;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelDataManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.ChunkEvent;
import team.creative.littletiles.client.render.entity.LittleLevelRenderManager;
import team.creative.littletiles.common.level.little.LittleLevel;

@OnlyIn(Dist.CLIENT)
public abstract class LittleClientLevel extends LittleLevel {
    
    final EntityTickList tickingEntities = new EntityTickList();
    private final TransientEntitySectionManager<Entity> entityStorage = new TransientEntitySectionManager<>(Entity.class, new LittleClientLevel.EntityCallbacks());
    final List<AbstractClientPlayer> players = Lists.newArrayList();
    private final Map<String, MapItemSavedData> mapData = Maps.newHashMap();
    private final ModelDataManager modelDataManager = new ModelDataManager(this);
    public LittleLevelRenderManager renderManager = new LittleLevelRenderManager(this);
    
    protected LittleClientLevel(WritableLevelData worldInfo, int radius, ResourceKey<Level> dimension, Supplier<ProfilerFiller> supplier, boolean debug, long seed, RegistryAccess access) {
        super(worldInfo, radius, dimension, supplier, true, debug, seed, access);
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
    public void sendBlockUpdated(BlockPos pos, BlockState actualState, BlockState setState, int p_104688_) {
        this.renderManager.blockChanged(this, pos, actualState, setState, p_104688_);
    }
    
    @Override
    public void setBlocksDirty(BlockPos pos, BlockState actualState, BlockState setState) {
        this.renderManager.setBlockDirty(pos, actualState, setState);
    }
    
    public void setSectionDirtyWithNeighbors(int x, int y, int z) {
        this.renderManager.setSectionDirtyWithNeighbors(x, y, z);
    }
    
    public void setLightReady(int x, int z) {
        LevelChunk levelchunk = this.getChunkSource().getChunk(x, z, false);
        if (levelchunk != null)
            levelchunk.setClientLightReady(true);
    }
    
    @Override
    public void addLoadedChunk(LevelChunk chunk) {
        super.addLoadedChunk(chunk);
        
    }
    
    @Override
    public void onChunkLoaded(ChunkPos pos) {
        super.onChunkLoaded(pos);
        this.entityStorage.startTicking(pos);
        LevelChunk chunk = getChunkSource().getChunk(pos.x, pos.z, true);
        //chunk.replaceWithPacketData(p_194119_, p_194120_, p_194121_);
        chunk.setClientLightReady(true);
        LevelChunkSection[] section = chunk.getSections();
        for (int i = 0; i < section.length; i++)
            this.renderManager.setSectionDirty(chunk.getPos().x, chunk.getSectionYFromSectionIndex(i), chunk.getPos().z);
        MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk));
    }
    
    final class EntityCallbacks implements LevelCallback<Entity> {
        
        @Override
        public void onCreated(Entity entity) {}
        
        @Override
        public void onDestroyed(Entity entity) {}
        
        @Override
        public void onTickingStart(Entity entity) {
            LittleClientLevel.this.tickingEntities.add(entity);
        }
        
        @Override
        public void onTickingEnd(Entity entity) {
            LittleClientLevel.this.tickingEntities.remove(entity);
        }
        
        @Override
        public void onTrackingStart(Entity entity) {
            if (entity instanceof AbstractClientPlayer)
                LittleClientLevel.this.players.add((AbstractClientPlayer) entity);
        }
        
        @Override
        public void onTrackingEnd(Entity entity) {
            entity.unRide();
            LittleClientLevel.this.players.remove(entity);
            
            entity.onRemovedFromWorld();
            MinecraftForge.EVENT_BUS.post(new EntityLeaveLevelEvent(entity, LittleClientLevel.this));
        }
        
        @Override
        public void onSectionChange(Entity entity) {}
    }
    
}
