package team.creative.littletiles.client.render.level;

import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;

@OnlyIn(Dist.CLIENT)
public class RenderUploader {
    
    private static final HashMap<Level, RenderAdditional> CACHES = new HashMap<>();
    
    private static RenderAdditional getOrCreate(Level level) {
        RenderAdditional data = CACHES.get(level);
        if (data == null)
            CACHES.put(level, data = new RenderAdditional(level));
        return data;
    }
    
    public static void queue(Level targetLevel, LittleAnimationEntity entity) {
        synchronized (CACHES) {
            if (getOrCreate(targetLevel).queue(entity.getUUID(), entity.getSubLevel(), entity.getSubLevel())) // Delete it if all cache has already been added to the blocks otherwise wait
                CACHES.remove(entity.getSubLevel());
        }
    }
    
    public static void notifyReceiveClientUpdate(BETiles be) {
        if (CACHES.isEmpty())
            return;
        synchronized (CACHES) {
            RenderAdditional data = CACHES.get(be.getLevel());
            if (data != null && data.notifyReceiveClientUpdate(be))
                CACHES.remove(be.getLevel());
        }
    }
    
    public static void unload() {
        CACHES.clear();
    }
    
    public static void longTick(int index) {
        for (Iterator<RenderAdditional> iterator = CACHES.values().iterator(); iterator.hasNext();) {
            RenderAdditional level = iterator.next();
            if (level.longTick(index))
                iterator.remove();
        }
    }
}