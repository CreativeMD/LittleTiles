package team.creative.littletiles.common.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.eventbus.api.Event;

public class GetVoxelShapesEvent extends Event {
    
    // fired in CollisionGetter.noCollision(@Nullable Entity p_45757_, AABB p_45758_) and
    // CollisionGetter.findFreePosition(@Nullable Entity p_151419_, VoxelShape p_151420_, Vec3 p_151421_, double p_151422_, double p_151423_, double p_151424_) and
    // CollisionGetter.Iterable<VoxelShape> getCollisions(@Nullable Entity p_186432_, AABB p_186433_)
    
    public final Level level;
    public final AABB box;
    public final Entity entity;
    
    public GetVoxelShapesEvent(Level level, AABB box, Entity entity) {
        this.level = level;
        this.box = box;
        this.entity = entity;
    }
    
    public void add(AABB bb) {
        // TODO Figure out way to store boxes, maybe rather use voxelshape
    }
}
