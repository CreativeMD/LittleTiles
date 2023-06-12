package team.creative.littletiles.common.structure.type.bed;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class LittleBedEventHandler {
    
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
            bed.setSleepingPlayer(null);
        ((ILittleBedPlayerExtension) event.getEntity()).setBed(null);
    }
    
    @SubscribeEvent
    public void onWakeUp(PlayerWakeUpEvent event) {
        LittleBed bed = ((ILittleBedPlayerExtension) event.getEntity()).getBed();
        if (bed != null)
            bed.setSleepingPlayer(null);
        ((ILittleBedPlayerExtension) event.getEntity()).setBed(null);
    }
    
    @SubscribeEvent
    public void onPlayerSleep(PlayerSleepInBedEvent event) {
        if (event.getEntity().level().getBlockState(event.getPos()).getBlock() instanceof BlockTile) {
            BETiles be = BlockTile.loadBE(event.getEntity().level(), event.getPos());
            if (be != null) {
                for (LittleStructure structure : be.loadedStructures()) {
                    if (structure instanceof LittleBed && ((LittleBed) structure).hasBeenActivated) {
                        try {
                            ((LittleBed) structure).trySleep(event.getEntity(), structure.getHighestCenterVec());
                            event.setResult((BedSleepingProblem) null);
                            ((LittleBed) structure).hasBeenActivated = false;
                            return;
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
}
