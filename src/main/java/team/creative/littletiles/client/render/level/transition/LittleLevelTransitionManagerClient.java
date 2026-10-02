package team.creative.littletiles.client.render.level.transition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.mixin.common.level.LevelAccessor;

@OnlyIn(Dist.CLIENT)
public class LittleLevelTransitionManagerClient {
    
    private static final HashMap<Level, LevelTransition> CACHES = new HashMap<>();
    
    private static LevelTransition getOrCreate(Level level, BlockPos pos) {
        LevelTransition data = CACHES.get(level);
        if (data == null)
            CACHES.put(level, data = new LevelTransition(level, pos));
        return data;
    }
    
    public static void queue(Level targetLevel, LittleAnimationEntity entity) {
        synchronized (CACHES) {
            if (getOrCreate(targetLevel, entity.getCenter().baseOffset).queue(entity.getUUID(), entity.getSubLevel(), entity.getSubLevel())) // Delete it if all cache has already been added to the blocks otherwise wait
                CACHES.remove(entity.getSubLevel());
        }
    }
    
    public static void unload() {
        CACHES.clear();
    }
    
    public static void longTick(int index) {
        for (Iterator<LevelTransition> iterator = CACHES.values().iterator(); iterator.hasNext();) {
            LevelTransition level = iterator.next();
            if (level.longTick(index)) {
                level.delete();
                iterator.remove();
            }
        }
    }
    
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
    
}