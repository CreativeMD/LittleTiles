package team.creative.littletiles.common.structure.type.bed;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

public class LittleBedEventHandler {
    
    @SubscribeEvent
    public void continueSleep(CanContinueSleepingEvent event) {
        if (event.getEntity() instanceof ILittleBedPlayerExtension b && b.getBed() != null)
            event.setContinueSleeping(true);
    }
    
    @SubscribeEvent
    public void isSleepingLocationAllowed(CanPlayerSleepEvent event) {
        if (event.getEntity() instanceof Player player) {
            LittleBed bed = ((ILittleBedPlayerExtension) player).getBed();
            if (bed != null && bed.getSleepingPlayer() == player)
                event.setProblem(null);
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        LittleBed bed = ((ILittleBedPlayerExtension) event.getEntity()).getBed();
        if (bed != null)
            bed.wakeUp();
        ((ILittleBedPlayerExtension) event.getEntity()).setBed(null);
    }
    
    @SubscribeEvent
    public void onWakeUp(PlayerWakeUpEvent event) {
        LittleBed bed = ((ILittleBedPlayerExtension) event.getEntity()).getBed();
        if (bed != null)
            bed.wakeUp();
        ((ILittleBedPlayerExtension) event.getEntity()).setBed(null);
    }
    
}
