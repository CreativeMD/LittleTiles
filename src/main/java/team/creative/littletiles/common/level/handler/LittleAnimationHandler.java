package team.creative.littletiles.common.level.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.math.box.BoxesVoxelShape;
import team.creative.creativecore.common.util.math.box.OBB;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.math.vec.LittleHitResult;

public abstract class LittleAnimationHandler extends LevelHandler {
    
    public Set<LittleEntity> entities = new CopyOnWriteArraySet<>();
    
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
    
    protected void tickEntity(LittleEntity entity) {
        if (entity.level() != level || entity.level() instanceof IOrientatedLevel)
            return;
        entity.performTick();
    }
    
    public void tick(LevelTickEvent event) {
        if (event.phase == Phase.END)
            for (LittleEntity entity : entities)
                tickEntity(entity);
    }
    
    public List<LittleEntity> find(AABB bb) {
        if (entities.isEmpty())
            return Collections.emptyList();
        
        List<LittleEntity> found = new ArrayList<>();
        for (LittleEntity entity : entities)
            if (entity.hasLoaded() && entity.getBoundingBox().intersects(bb))
                found.add(entity);
        return found;
    }
    
    public LittleEntity find(UUID uuid) {
        for (LittleEntity entity : entities)
            if (entity.getUUID().equals(uuid))
                return entity;
        return null;
    }
    
    public void add(LittleEntity entity) {
        entities.add(entity);
    }
    
    public void remove(LittleEntity entity) {
        entities.remove(entity);
    }
    
    public Iterable<VoxelShape> collisionExcept(@Nullable Entity colliding, AABB box, Level level) {
        if (level instanceof ISubLevel)
            return null;
        List<VoxelShape> shapes = null;
        for (LittleEntity entity : find(box)) {
            if (!entity.physic.shouldPush())
                continue;
            
            List<ABB> boxes = null;
            ABB transformedBox = entity.getOrigin().getOBB(box);
            for (VoxelShape shape : entity.getSubLevel().getBlockCollisions(colliding, transformedBox.toVanilla()))
                for (AABB bb : shape.toAabbs())
                    if (transformedBox.intersects(bb)) {
                        if (boxes == null)
                            boxes = new ArrayList<>();
                        boxes.add(new OBB(bb, entity.getOrigin()));
                    }
                
            if (boxes != null) {
                if (shapes == null)
                    shapes = new ArrayList<>();
                shapes.add(BoxesVoxelShape.create(boxes));
            }
        }
        return shapes;
    }
    
    public LittleHitResult getHit(Vec3 pos, Vec3 look, double reach) {
        AABB box = new AABB(pos, look);
        
        LittleHitResult newHit = null;
        double distance = reach;
        for (LittleEntity entity : find(box)) {
            if (!entity.hasLoaded())
                continue;
            LittleHitResult tempResult = entity.rayTrace(pos, look);
            if (tempResult == null || !(tempResult.hit instanceof BlockHitResult))
                continue;
            double tempDistance = pos.distanceTo(entity.getOrigin().transformPointToWorld(tempResult.hit.getLocation()));
            if (newHit == null || tempDistance < distance) {
                newHit = tempResult;
                distance = tempDistance;
            }
        }
        
        return newHit;
    }
    
}
