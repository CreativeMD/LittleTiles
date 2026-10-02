package team.creative.littletiles.common.level.little;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.client.entity.LevelTransitionListener;
import team.creative.littletiles.common.packet.entity.LittleEntityTransitionPacket;
import team.creative.littletiles.mixin.common.entity.EntityAccessor;

public class LittleLevelTransitionManager {
    
    public static void moveTo(Entity entity, Level newlevel) {
        LittleTiles.NETWORK.sendToClientTracking(new LittleEntityTransitionPacket(entity, newlevel), entity);
        
        Level oldLevel = entity.level();
        ((EntityAccessor) entity).getLevelCallback().onRemove(Entity.RemovalReason.CHANGED_DIMENSION);
        
        if (entity instanceof LevelTransitionListener listener)
            listener.prepareChangeLevel(oldLevel, newlevel);
        
        newlevel.addFreshEntity(entity);
        
        if (entity instanceof LevelTransitionListener listener)
            listener.changedLevel(oldLevel, newlevel);
        
    }
    
}
