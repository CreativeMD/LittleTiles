package team.creative.littletiles.server.level;

import com.creativemd.creativecore.common.world.CreativeWorld;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.level.LittleAnimationHandler;

public class LittleAnimationHandlerServer extends LittleAnimationHandler {
    
    public LittleAnimationHandlerServer(World world) {
        super(world);
    }
    
    public void tick(WorldTickEvent event) {
        if (event.phase == Phase.END && world == event.world) {
            for (EntityAnimation door : openDoors) {
                
                if (door.world != world || door.world instanceof CreativeWorld)
                    continue;
                
                door.onUpdateForReal();
            }
            
            openDoors.removeIf((x) -> {
                if (x.isDead) {
                    x.markRemoved();
                    return true;
                }
                return false;
            });
        }
    }
    
}
