package team.creative.littletiles.common.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import team.creative.creativecore.common.util.math.box.OBB;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.type.door.LittleDoor;

public abstract class LittleAnimationHandler extends LevelHandler {
    
    public LittleAnimationHandler(Level level) {
        super(level);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @Override
    public void unload() {
        super.unload();
        openDoors.clear();
        MinecraftForge.EVENT_BUS.unregister(this);
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
        
        AABB box = new AABB(pos);
        
        List<LittleDoor> doors = new ArrayList<>();
        for (EntityAnimation door : openDoors)
            if (door.structure instanceof LittleDoor && door.getBoundingBox().intersects(box) && !doors.contains(door.structure))
                try {
                    doors.add(((LittleDoor) door.structure).getParentDoor());
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        return doors;
    }
    
    public EntityAnimation findAnimation(UUID uuid) {
        for (EntityAnimation animation : openDoors) {
            if (animation.getUUID().equals(uuid))
                return animation;
        }
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
    
    public void worldCollision(GetCollisionBoxesEvent event) {
        AABB box = event.getAabb();
        for (EntityAnimation animation : findAnimations(box)) {
            if (animation.noCollision || animation.controller.noClip())
                continue;
            
            OBB newAlignedBox = animation.origin.getOrientatedBox(box);
            for (OBB bb : animation.worldCollisionBoxes) {
                if (bb.intersects(newAlignedBox))
                    event.getCollisionBoxesList().add(bb);
            }
        }
    }
    
}
