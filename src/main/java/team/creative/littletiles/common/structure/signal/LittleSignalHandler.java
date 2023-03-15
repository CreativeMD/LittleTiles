package team.creative.littletiles.common.structure.signal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.level.handler.LevelHandler;
import team.creative.littletiles.common.level.tick.LittleTickTicket;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;
import team.creative.littletiles.common.structure.signal.schedule.ISignalSchedulable;
import team.creative.littletiles.common.structure.signal.schedule.SignalScheduleTicket;

public class LittleSignalHandler extends LevelHandler {
    
    public static final int QUEUE_LENGTH = 20;
    private static final List<SignalScheduleTicket> UNSORTED_TICKETS = new ArrayList<>();
    
    public static synchronized void serverTick() {
        if (UNSORTED_TICKETS.isEmpty())
            return;
        for (Iterator<SignalScheduleTicket> iterator = UNSORTED_TICKETS.iterator(); iterator.hasNext();) {
            SignalScheduleTicket ticket = iterator.next();
            Level level = ticket.getLevel();
            if (level != null) {
                LittleTiles.TICKERS.get(level).schedule(ticket.getDelay(), ticket);
                iterator.remove();
            }
        }
    }
    
    public static synchronized List<SignalScheduleTicket> findTickets(ISignalComponent component, SignalOutputHandler condition) {
        Level level = component.getStructureLevel();
        if (level != null && !level.isClientSide) {
            List<SignalScheduleTicket> tickets = new ArrayList<>();
            for (LittleTickTicket ticket : LittleTiles.TICKERS.get(level))
                if (ticket.get() instanceof SignalScheduleTicket s && s.is(condition))
                    tickets.add(s);
            return tickets;
        }
        return Collections.EMPTY_LIST;
    }
    
    public static void schedule(Level level, ISignalSchedulable schedulable) {
        if (level == null)
            return;
        get(level).schedule(schedulable);
    }
    
    public static synchronized SignalScheduleTicket schedule(SignalOutputHandler handler, SignalState result, int delay) {
        SignalScheduleTicket ticket = new SignalScheduleTicket(handler, result, delay);
        Level level = handler.component.getStructureLevel();
        if (level == null)
            UNSORTED_TICKETS.add(ticket);
        else
            LittleTiles.TICKERS.get(level).schedule(delay, ticket);
        return ticket;
    }
    
    private final HashSet<LittleStructure> queuedUpdateStructures = new HashSet<>();
    private final HashSet<LittleStructure> queuedStructures = new HashSet<>();
    private List<ISignalSchedulable> scheduled = new ArrayList<>();
    
    public LittleSignalHandler(Level level) {
        super(level);
    }
    
    @Override
    public void unload() {
        queuedUpdateStructures.clear();
        queuedStructures.clear();
    }
    
    public synchronized void queueStructureForUpdatePacket(LittleStructure structure) {
        if (structure.isClient())
            return;
        queuedUpdateStructures.add(structure);
    }
    
    public synchronized void queueStructureForNextTick(LittleStructure structure) {
        if (structure.isClient())
            return;
        queuedStructures.add(structure);
    }
    
    @SubscribeEvent
    public void levelTick(LevelTickEvent event) {
        if (event.phase == Phase.START)
            return;
        
        if (!queuedStructures.isEmpty()) {
            for (Iterator<LittleStructure> iterator = queuedStructures.iterator(); iterator.hasNext();) {
                LittleStructure structure = iterator.next();
                if (!structure.queueTick())
                    iterator.remove();
            }
        }
        
        if (!queuedUpdateStructures.isEmpty()) {
            for (LittleStructure structure : queuedUpdateStructures)
                structure.sendUpdatePacket();
            queuedUpdateStructures.clear();
        }
    }
    
    @SubscribeEvent
    public synchronized void tick(LevelTickEvent event) {
        if (event.phase == Phase.END && level == event.level) {
            
            for (int i = 0; i < scheduled.size(); i++)
                try {
                    scheduled.get(i).updateSignaling();
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            scheduled.clear();
        }
    }
    
    public synchronized void schedule(ISignalSchedulable schedulable) {
        scheduled.add(schedulable);
    }
    
}
