package team.creative.littletiles.common.level.little;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.client.entity.LevelTransitionListener;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.packet.entity.LittleEntityTransitionPacket;
import team.creative.littletiles.mixin.common.entity.EntityAccessor;
import team.creative.littletiles.mixin.common.level.LevelAccessor;

public class LittleLevelTransitionManager {
    
    @OnlyIn(Dist.CLIENT)
    public static Entity findEntity(UUID uuid) {
        Entity target = LittleTiles.ANIMATION_HANDLERS.find(true, uuid);
        if (target == null)
            return target;
        
        target = ((LevelAccessor) Minecraft.getInstance().level).callGetEntities().get(uuid);
        if (target != null)
            return target;
        
        for (LittleEntity entity : LittleTilesClient.ANIMATION_HANDLER.entities) {
            target = entity.getSubLevel().getEntityGetter().get(uuid);
            if (target != null)
                return target;
        }
        
        return target;
    }
    
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
