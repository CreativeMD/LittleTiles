package team.creative.littletiles.common.entity.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

public class LittleAnimationLevelEntities implements LevelEntityGetter<Entity> {
    
    private final Int2ObjectMap<Entity> byId = new Int2ObjectLinkedOpenHashMap<>();
    private final LevelCallback<Entity> callbacks;
    private final Map<UUID, Entity> byUuid = Maps.newHashMap();
    
    public LittleAnimationLevelEntities(LevelCallback<Entity> callback) {
        this.callbacks = callback;
    }
    
    public boolean addNewEntity(Entity entity) {
        return this.addEntity(entity, false);
    }
    
    public boolean addNewEntityWithoutEvent(Entity entity) {
        return this.addEntityWithoutEvent(entity, false);
    }
    
    private boolean addEntity(Entity entity, boolean loadedFromDisk) {
        if (MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(entity, entity.level(), loadedFromDisk)))
            return false;
        return addEntityWithoutEvent(entity, loadedFromDisk);
    }
    
    private boolean addEntityWithoutEvent(Entity entity, boolean loadedFromDisk) {
        try {
            this.byUuid.putIfAbsent(entity.getUUID(), entity);
            this.byId.put(entity.getId(), entity);
            
            entity.setLevelCallback(new Callback(entity));
            if (!loadedFromDisk)
                this.callbacks.onCreated(entity);
            
            this.startTracking(entity);
            this.startTicking(entity);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public Entity get(int id) {
        return byId.get(id);
    }
    
    @Override
    public Entity get(UUID uuid) {
        return byUuid.get(uuid);
    }
    
    @Override
    public Iterable<Entity> getAll() {
        return byId.values();
    }
    
    void startTicking(Entity entity) {
        this.callbacks.onTickingStart(entity);
    }
    
    void stopTicking(Entity entity) {
        this.callbacks.onTickingEnd(entity);
    }
    
    void startTracking(Entity entity) {
        this.callbacks.onTrackingStart(entity);
    }
    
    void stopTracking(Entity entity) {
        this.callbacks.onTrackingEnd(entity);
    }
    
    @Override
    public <U extends Entity> void get(EntityTypeTest<Entity, U> test, AbortableIterationConsumer<U> consumer) {
        for (Entity entity : this.byId.values()) {
            U u = test.tryCast(entity);
            if (u != null && consumer.accept(u).shouldAbort())
                return;
        }
    }
    
    @Override
    public void get(AABB bb, Consumer<Entity> consumer) {
        for (Entity entity : byId.values())
            if (entity.getBoundingBox().intersects(bb))
                consumer.accept(entity);
    }
    
    @Override
    public <U extends Entity> void get(EntityTypeTest<Entity, U> test, AABB bb, AbortableIterationConsumer<U> consumer) {
        for (Entity entity : this.byId.values()) {
            U u = test.tryCast(entity);
            if (u != null && entity.getBoundingBox().intersects(bb) && consumer.accept(u).shouldAbort())
                return;
        }
    }
    
    public void removeAll() {
        List<Entity> all = new ArrayList<>(byUuid.values());
        for (Entity entity : all)
            entity.setRemoved(RemovalReason.UNLOADED_WITH_PLAYER);
    }
    
    class Callback implements EntityInLevelCallback {
        
        private final Entity entity;
        
        Callback(Entity entity) {
            this.entity = entity;
        }
        
        @Override
        public void onMove() {}
        
        @Override
        public void onRemove(Entity.RemovalReason remove) {
            stopTicking(entity);
            stopTracking(entity);
            
            if (remove.shouldDestroy())
                callbacks.onDestroyed(entity);
            
            entity.setLevelCallback(NULL);
            byUuid.remove(entity.getUUID());
            byId.remove(entity.getId());
            
        }
    }
}
