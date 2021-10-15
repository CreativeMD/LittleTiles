package team.creative.littletiles.common.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;

import net.minecraft.core.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.event.world.WorldEvent;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.type.door.LittleDoor;

public abstract class LittleAnimationHandler {
    
    public final Level level;
    
    public LittleAnimationHandler(Level level) {
        if (level == null)
            throw new RuntimeException("Creating handler for empty world!");
        
        this.level = level;
    }
    
    public List<EntityAnimation> openDoors = new CopyOnWriteArrayList<>();
    
    public List<EntityAnimation> findAnimations(AABB bb) {
        if (openDoors.isEmpty())
            return Collections.emptyList();
        
        List<EntityAnimation> doors = new ArrayList<>();
        for (EntityAnimation door : openDoors)
            if (door.getBoundingBox().intersects(bb))
                doors.add(door);
        return doors;
    }
    
    public List<LittleDoor> findAnimations(BlockPos pos) {
        if (openDoors.isEmpty())
            return Collections.emptyList();
        
        AxisAlignedBB box = new AxisAlignedBB(pos);
        
        List<LittleDoor> doors = new ArrayList<>();
        for (EntityAnimation door : openDoors)
            if (door.structure instanceof LittleDoor && door.getEntityBoundingBox().intersects(box) && !doors.contains(door.structure))
                try {
                    doors.add(((LittleDoor) door.structure).getParentDoor());
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        return doors;
    }
    
    public EntityAnimation findAnimation(UUID uuid) {
        for (EntityAnimation animation : openDoors) {
            if (animation.getUniqueID().equals(uuid))
                return animation;
        }
        return null;
    }
    
    public void createDoor(EntityAnimation door) {
        openDoors.add(door);
    }
    
    public void chunkUnload(ChunkEvent.Unload event) {
        openDoors.removeIf((x) -> {
            if (x.isDead) {
                x.markRemoved();
                return true;
            }
            return false;
        });
    }
    
    public void worldUnload(WorldEvent.Unload event) {
        openDoors.removeIf((x) -> {
            if (x.world == event.getWorld()) {
                x.markRemoved();
                return true;
            }
            return false;
        });
    }
    
    public void worldCollision(GetCollisionBoxesEvent event) {
        AxisAlignedBB box = event.getAabb();
        for (EntityAnimation animation : findAnimations(box)) {
            if (animation.noCollision || animation.controller.noClip())
                continue;
            
            OrientatedBoundingBox newAlignedBox = animation.origin.getOrientatedBox(box);
            for (OrientatedBoundingBox bb : animation.worldCollisionBoxes) {
                if (bb.intersects(newAlignedBox))
                    event.getCollisionBoxesList().add(bb);
            }
        }
    }
    
}
