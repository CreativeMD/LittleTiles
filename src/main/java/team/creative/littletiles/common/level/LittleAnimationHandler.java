package team.creative.littletiles.common.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import team.creative.creativecore.common.util.math.box.OBB;
import team.creative.littletiles.common.animation.entity.LittleLevelEntity;
import team.creative.littletiles.common.event.GetVoxelShapesEvent;

public abstract class LittleAnimationHandler extends LevelHandler {
    
    public Set<LittleLevelEntity> entities = new CopyOnWriteArraySet<>();
    
    public LittleAnimationHandler(Level level) {
        super(level);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @Override
    public void unload() {
        super.unload();
        entities.clear();
        MinecraftForge.EVENT_BUS.unregister(this);
    }
    
    public void tick() {
        
    }
    
    public List<LittleLevelEntity> find(AABB bb) {
        if (entities.isEmpty())
            return Collections.emptyList();
        
        List<LittleLevelEntity> found = new ArrayList<>();
        for (LittleLevelEntity entity : entities)
            if (entity.getBoundingBox().intersects(bb))
                found.add(entity);
        return found;
    }
    
    public LittleLevelEntity find(UUID uuid) {
        for (LittleLevelEntity entity : entities)
            if (entity.getUUID().equals(uuid))
                return entity;
        return null;
    }
    
    public void add(LittleLevelEntity entity) {
        entities.add(entity);
    }
    
    public void remove(LittleLevelEntity entity) {
        entities.remove(entity);
    }
    
    public void collision(GetVoxelShapesEvent event) {
        for (LittleLevelEntity entity : find(event.box)) {
            if (!entity.physic.shouldPush())
                continue;
            
            for (OBB bb : entity.physic.collision(event.box))
                event.add(bb);
        }
    }
    
}
