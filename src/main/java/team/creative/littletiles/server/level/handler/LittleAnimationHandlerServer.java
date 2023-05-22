package team.creative.littletiles.server.level.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerEvent.StopTracking;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.handler.LittleAnimationHandler;

public class LittleAnimationHandlerServer extends LittleAnimationHandler {
    
    public LittleAnimationHandlerServer(Level level) {
        super(level);
    }
    
    @SubscribeEvent
    public void trackEntity(StartTracking event) {
        if (event.getTarget() instanceof LittleEntity levelEntity) {
            levelEntity.startTracking((ServerPlayer) event.getEntity());
            LittleTiles.NETWORK.sendToClient(levelEntity.initClientPacket(), (ServerPlayer) event.getEntity());
        }
    }
    
    @SubscribeEvent
    public void stopTrackEntity(StopTracking event) {
        if (event.getTarget() instanceof LittleEntity levelEntity)
            levelEntity.stopTracking((ServerPlayer) event.getEntity());
    }
    
}
