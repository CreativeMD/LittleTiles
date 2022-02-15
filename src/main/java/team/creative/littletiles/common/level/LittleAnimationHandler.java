package team.creative.littletiles.common.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import team.creative.creativecore.common.util.math.box.OBB;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.animation.entity.LittleLevelEntity;
import team.creative.littletiles.common.event.GetVoxelShapesEvent;

public abstract class LittleAnimationHandler extends LevelHandler {
    
    public List<LittleLevelEntity> entities = new CopyOnWriteArrayList<>();
    
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
    
    public void createDoor(EntityAnimation door) {
        openDoors.add(door);
    }
    
    public void chunkUnload(ChunkEvent.Unload event) {
        openDoors.removeIf((x) -> {
            if (x.isRemoved()) {
                x.markRemoved();
                return true;
            }
            return false;
        });
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
