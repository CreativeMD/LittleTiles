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
import team.creative.creativecore.common.util.math.box.OBBVoxelShape;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;
import team.creative.littletiles.common.math.vec.LittleHitResult;

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
    
    public void tick() {}
    
    public List<LittleLevelEntity> find(AABB bb) {
        if (entities.isEmpty())
            return Collections.emptyList();
        
        List<LittleLevelEntity> found = new ArrayList<>();
        for (LittleLevelEntity entity : entities)
            if (entity.hasLoaded() && entity.getBoundingBox().intersects(bb))
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
    
    public Iterable<VoxelShape> collisionExcept(@Nullable Entity colliding, AABB box, Level level) {
        List<VoxelShape> shapes = null;
        for (LittleLevelEntity entity : find(box)) {
            if (!entity.physic.shouldPush() || entity.getSubLevel() == level)
                continue;
            
            for (VoxelShape shape : entity.getSubLevel().getBlockCollisions(colliding, entity.getOrigin().getOBB(box)))
                for (AABB bb : shape.toAabbs())
                    if (bb.intersects(box)) {
                        if (shapes == null)
                            shapes = new ArrayList<>();
                        shapes.add(OBBVoxelShape.create(bb, entity.getOrigin()));
                    }
        }
        return shapes;
    }
    
    public LittleHitResult getHit(Vec3 pos, Vec3 look, double reach) {
        AABB box = new AABB(pos, look);
        
        LittleHitResult newHit = null;
        double distance = reach;
        for (LittleLevelEntity entity : find(box)) {
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
