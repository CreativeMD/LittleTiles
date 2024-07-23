package team.creative.littletiles.client.level.little;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import team.creative.littletiles.common.entity.animation.LittleAnimationLevel;
import team.creative.littletiles.common.level.little.LittleAnimationLevelCallback;

public class LittleAnimationLevelClientCallback extends LittleAnimationLevelCallback {
    
    public LittleAnimationLevelClientCallback(LittleAnimationLevel level) {
        super(level);
    }
    
    @Override
    public void onCreated(Entity entity) {}
    
    @Override
    public void onDestroyed(Entity entity) {}
    
    @Override
    public void onTickingStart(Entity entity) {
        tickingEntities.add(entity);
    }
    
    @Override
    public void onTickingEnd(Entity entity) {
        tickingEntities.remove(entity);
    }
    
    @Override
    public void onTrackingStart(Entity entity) {}
    
    @Override
    public void onTrackingEnd(Entity entity) {
        entity.unRide();
        
        entity.onRemovedFromLevel();
        NeoForge.EVENT_BUS.post(new EntityLeaveLevelEvent(entity, level));
    }
    
    @Override
    public void addTrackingPlayer(ServerPlayer player) {}
    
    @Override
    public void removeTrackingPlayer(ServerPlayer player) {}
    
    @Override
    public void tickEntity(Entity entity) {
        if (!entity.isRemoved() && !entity.isPassenger())
            level.guardEntityTick(this::tickNonPassenger, entity);
    }
    
    public void tickNonPassenger(Entity entity) {
        entity.setOldPosAndRot();
        ++entity.tickCount;
        
        if (!net.neoforged.neoforge.event.EventHooks.fireEntityTickPre(entity).isCanceled()) {
            entity.tick();
            net.neoforged.neoforge.event.EventHooks.fireEntityTickPost(entity);
        }
        
        for (Entity passenger : entity.getPassengers())
            this.tickPassenger(entity, passenger);
    }
    
    private void tickPassenger(Entity vehicle, Entity entity) {
        if (!entity.isRemoved() && entity.getVehicle() == vehicle) {
            if (entity instanceof Player || tickingEntities.contains(entity)) {
                entity.setOldPosAndRot();
                ++entity.tickCount;
                entity.rideTick();
                
                for (Entity passenger : entity.getPassengers())
                    this.tickPassenger(entity, passenger);
            }
        } else {
            entity.stopRiding();
        }
    }
}
