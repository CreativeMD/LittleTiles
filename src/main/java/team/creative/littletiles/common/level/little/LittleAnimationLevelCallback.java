package team.creative.littletiles.common.level.little;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.LevelCallback;
import team.creative.littletiles.common.entity.animation.LittleAnimationLevel;

public abstract class LittleAnimationLevelCallback implements LevelCallback<Entity> {
    
    public final LittleAnimationLevel level;
    public final EntityTickList tickingEntities = new EntityTickList();
    
    public LittleAnimationLevelCallback(LittleAnimationLevel level) {
        this.level = level;
    }
    
    public void tick() {
        this.tickingEntities.forEach(this::tickEntity);
        
    }
    
    public abstract void tickEntity(Entity entity);
    
    public abstract void addTrackingPlayer(ServerPlayer player);
    
    public abstract void removeTrackingPlayer(ServerPlayer player);
    
    @Override
    public void onSectionChange(Entity entity) {}
    
    @Override
    public void onTickingStart(Entity p_171704_) {
        tickingEntities.add(p_171704_);
    }
    
    @Override
    public void onTickingEnd(Entity p_171708_) {
        this.tickingEntities.remove(p_171708_);
    }
    
}
