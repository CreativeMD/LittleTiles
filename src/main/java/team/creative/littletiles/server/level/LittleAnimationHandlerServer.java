package team.creative.littletiles.server.level;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.littletiles.common.entity.LittleLevelEntity;
import team.creative.littletiles.common.level.LittleAnimationHandler;

public class LittleAnimationHandlerServer extends LittleAnimationHandler {
    
    public LittleAnimationHandlerServer(Level level) {
        super(level);
    }
    
    public void tickServer(WorldTickEvent event) {
        if (event.phase == Phase.END && level == event.world) {
            tick();
            
            for (LittleLevelEntity entity : entities) {
                if (entity.level != level || entity.level instanceof CreativeLevel)
                    continue;
                entity.performTick();
            }
        }
    }
    
}
