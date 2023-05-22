package team.creative.littletiles.server.level.little;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationLevel;
import team.creative.littletiles.common.level.little.LittleAnimationLevelCallback;
import team.creative.littletiles.common.packet.entity.level.LittleLevelPacket;

public class LittleAnimationLevelServerCallback extends LittleAnimationLevelCallback {
    
    private final Int2ObjectMap<ServerEntity> entities = new Int2ObjectLinkedOpenHashMap<>();
    private final List<ServerPlayer> seenBy = new ArrayList<>();
    
    public LittleAnimationLevelServerCallback(LittleAnimationLevel level) {
        super(level);
    }
    
    @Override
    public void onCreated(Entity entity) {}
    
    @Override
    public void onDestroyed(Entity entity) {
        level.getScoreboard().entityRemoved(entity);
    }
    
    @Override
    public void tick() {
        super.tick();
        for (ServerEntity entity : entities.values())
            entity.sendChanges();
    }
    
    @Override
    public void onTrackingStart(Entity entity) {
        EntityType<?> entitytype = entity.getType();
        if (entitytype.clientTrackingRange() * 16 != 0) {
            ServerEntity server = new ServerEntity((ServerLevel) level.getRealLevel(), entity, entitytype.updateInterval(), entitytype.trackDeltas(), this::broadcast);
            entities.put(entity.getId(), server);
            for (ServerPlayer player : seenBy)
                if (entity.broadcastToPlayer(player))
                    server.addPairing(player);
                
            entity.updateDynamicGameEventListener(DynamicGameEventListener::add);
        }
        
    }
    
    @Override
    public void onTrackingEnd(Entity entity) {
        ServerEntity server = entities.remove(entity.getId());
        for (ServerPlayer player : seenBy)
            server.removePairing(player);
        
        entity.updateDynamicGameEventListener(DynamicGameEventListener::remove);
        
        entity.onRemovedFromWorld();
        MinecraftForge.EVENT_BUS.post(new EntityLeaveLevelEvent(entity, level));
    }
    
    @Override
    public void addTrackingPlayer(ServerPlayer player) {
        if (seenBy.add(player))
            for (ServerEntity entity : entities.values())
                entity.addPairing(player);
    }
    
    @Override
    public void removeTrackingPlayer(ServerPlayer player) {
        if (seenBy.remove(player))
            for (ServerEntity entity : entities.values())
                entity.removePairing(player);
    }
    
    public void broadcast(Packet<?> packet) {
        LittleLevelPacket lt = new LittleLevelPacket(level, packet);
        for (ServerPlayer player : this.seenBy)
            LittleTiles.NETWORK.sendToClient(lt, player);
    }
    
    private boolean shouldDiscardEntity(Entity entity) {
        var server = level.getServer();
        if (server.isSpawningAnimals() || !(entity instanceof Animal) && !(entity instanceof WaterAnimal))
            return !server.areNpcsEnabled() && entity instanceof Npc;
        return true;
    }
    
    @Override
    public void tickEntity(Entity entity) {
        if (entity.isRemoved())
            return;
        
        if (shouldDiscardEntity(entity)) {
            entity.discard();
            return;
        }
        
        entity.checkDespawn();
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            if (!vehicle.isRemoved() && vehicle.hasPassenger(entity)) {
                return;
            }
            
            entity.stopRiding();
        }
        if (!entity.isRemoved() && !(entity instanceof PartEntity))
            level.guardEntityTick(this::tickNonPassenger, entity);
        
    }
    
    public void tickNonPassenger(Entity entity) {
        entity.setOldPosAndRot();
        ++entity.tickCount;
        entity.tick();
        for (Entity passenger : entity.getPassengers())
            this.tickPassenger(entity, passenger);
    }
    
    private void tickPassenger(Entity vehicle, Entity entity) {
        if (!entity.isRemoved() && entity.getVehicle() == vehicle) {
            if (entity instanceof Player || tickingEntities.contains(entity)) {
                entity.setOldPosAndRot();
                ++entity.tickCount;
                if (entity.canUpdate())
                    entity.rideTick();
                
                for (Entity passenger : entity.getPassengers()) {
                    this.tickPassenger(entity, passenger);
                }
                
            }
        } else {
            entity.stopRiding();
        }
    }
}
