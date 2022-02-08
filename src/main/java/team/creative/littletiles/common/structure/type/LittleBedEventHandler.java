package team.creative.littletiles.common.structure.type;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;

public class LittleBedEventHandler {
    
    @SubscribeEvent
    public void isSleepingLocationAllowed(SleepingLocationCheckEvent event) {
        try {
            LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.getEntityPlayer());
            if (bed instanceof LittleBed && ((LittleBed) bed).getSleepingPlayer() == event.getEntityPlayer())
                event.setResult(Result.ALLOW);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        try {
            LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.player);
            if (bed instanceof LittleBed)
                ((LittleBed) bed).setSleepingPlayer(null);
            LittleBed.littleBed.set(event.player, null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    @SubscribeEvent
    public void onWakeUp(PlayerWakeUpEvent event) {
        try {
            LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.getEntityPlayer());
            if (bed instanceof LittleBed)
                ((LittleBed) bed).setSleepingPlayer(null);
            LittleBed.littleBed.set(event.getEntityPlayer(), null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onPlayerSleep(PlayerSleepInBedEvent event) {
        if (event.getEntityPlayer().world.getBlockState(event.getPos()).getBlock() instanceof BlockTile) {
            TileEntityLittleTiles te = BlockTile.loadTe(event.getEntityPlayer().world, event.getPos());
            if (te != null) {
                for (LittleStructure structure : te.loadedStructures()) {
                    if (structure instanceof LittleBed && ((LittleBed) structure).hasBeenActivated) {
                        try {
                            ((LittleBed) structure).trySleep(event.getEntityPlayer(), structure.getHighestCenterVec());
                            event.setResult(SleepResult.OK);
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
