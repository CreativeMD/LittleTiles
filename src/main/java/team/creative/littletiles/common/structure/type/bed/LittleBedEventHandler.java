package team.creative.littletiles.common.structure.type.bed;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LittleBedEventHandler {
    
    @SubscribeEvent
    public void continueSleep(SleepingLocationCheckEvent event) {
        if (event.getEntity() instanceof ILittleBedPlayerExtension b && b.getBed() != null)
            event.setResult(Result.ALLOW);
    }
    
    @SubscribeEvent
    public void isSleepingLocationAllowed(SleepingLocationCheckEvent event) {
        if (event.getEntity() instanceof Player player) {
            LittleBed bed = ((ILittleBedPlayerExtension) player).getBed();
            if (bed != null && bed.getSleepingPlayer() == player)
                event.setResult(Result.ALLOW);
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
