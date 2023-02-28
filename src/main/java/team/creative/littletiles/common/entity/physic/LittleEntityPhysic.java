package team.creative.littletiles.common.entity.physic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.collision.CollisionCoordinator;
import team.creative.littletiles.common.entity.level.LittleEntity;
import team.creative.littletiles.common.level.little.LittleSubLevel;

public abstract class LittleEntityPhysic {
    
    protected boolean preventPush = false;
    public final LittleEntity parent;
    
    public LittleEntityPhysic(LittleEntity parent) {
        this.parent = parent;
    }
    
    public void ignoreCollision(Runnable run) {
        preventPush = true;
        try {
            run.run();
        } finally {
            preventPush = false;
        }
    }
    
    public boolean shouldPush() {
        return !preventPush;
    }
    
    public abstract void setSubLevel(LittleSubLevel level);
    
    public abstract void tick();
    
    public abstract void updateBoundingBox();
    
    public abstract AABB getOBB();
    
    public abstract AABB getBB();
    
    public abstract Vec3 getCenter();
    
    public abstract void load(CompoundTag nbt);
    
    public abstract CompoundTag save();
    
    public abstract void transform(CollisionCoordinator coordinator);
    
}
