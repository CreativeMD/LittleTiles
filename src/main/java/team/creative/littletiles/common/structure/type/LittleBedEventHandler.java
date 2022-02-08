package team.creative.littletiles.common.structure.type;

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
        try {
            if (event.getEntityLiving() instanceof Player player) {
                LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(player);
                if (bed instanceof LittleBed && ((LittleBed) bed).getSleepingPlayer() == player)
                    event.setResult(Result.ALLOW);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        try {
            LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.getPlayer());
            if (bed instanceof LittleBed)
                ((LittleBed) bed).setSleepingPlayer(null);
            LittleBed.littleBed.set(event.getPlayer(), null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    @SubscribeEvent
    public void onWakeUp(PlayerWakeUpEvent event) {
        try {
            LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.getPlayer());
            if (bed instanceof LittleBed)
                ((LittleBed) bed).setSleepingPlayer(null);
            LittleBed.littleBed.set(event.getPlayer(), null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    @SubscribeEvent
    public void onPlayerSleep(PlayerSleepInBedEvent event) {
        if (event.getPlayer().level.getBlockState(event.getPos()).getBlock() instanceof BlockTile) {
            BETiles be = BlockTile.loadBE(event.getPlayer().level, event.getPos());
            if (be != null) {
                for (LittleStructure structure : be.loadedStructures()) {
                    if (structure instanceof LittleBed && ((LittleBed) structure).hasBeenActivated) {
                        try {
                            ((LittleBed) structure).trySleep(event.getPlayer(), structure.getHighestCenterVec());
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
