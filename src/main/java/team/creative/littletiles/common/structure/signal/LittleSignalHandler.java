package team.creative.littletiles.common.structure.signal;

import java.util.HashSet;
import java.util.Iterator;

import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.schedule.SignalTicker;

public class LittleSignalHandler {
    
    private static final HashSet<LittleStructure> queuedUpdateStructures = new HashSet<>();
    private static final HashSet<LittleStructure> queuedStructures = new HashSet<>();
    
    public static synchronized void queueStructureForUpdatePacket(LittleStructure structure) {
        if (structure.isClient())
            return;
        queuedUpdateStructures.add(structure);
    }
    
    public static synchronized void queueStructureForNextTick(LittleStructure structure) {
        if (structure.isClient())
            return;
        queuedStructures.add(structure);
    }
    
    @SubscribeEvent
    public synchronized void serverTick(ServerTickEvent event) {
        if (event.phase == Phase.START)
            return;
        if (!queuedUpdateStructures.isEmpty()) {
            for (LittleStructure structure : queuedUpdateStructures)
                structure.sendUpdatePacket();
            queuedUpdateStructures.clear();
        }
        if (!queuedStructures.isEmpty()) {
            for (Iterator<LittleStructure> iterator = queuedStructures.iterator(); iterator.hasNext();) {
                LittleStructure structure = iterator.next();
                if (!structure.queueTick())
                    iterator.remove();
            }
        }
        SignalTicker.serverTick();
    }
    
    public synchronized void levelUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) {
            queuedUpdateStructures.clear();
            queuedStructures.clear();
        }
    }
    
}
