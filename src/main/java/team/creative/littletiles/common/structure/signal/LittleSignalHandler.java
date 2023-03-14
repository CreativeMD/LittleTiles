package team.creative.littletiles.common.structure.signal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.level.handler.LevelHandler;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;
import team.creative.littletiles.common.structure.signal.schedule.ISignalSchedulable;
import team.creative.littletiles.common.structure.signal.schedule.ISignalScheduleTicket;
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
                LittleTiles.SIGNAL_HANDLERS.get(level).openTicket(ticket);
                iterator.remove();
            }
        }
    }
    
    public static synchronized List<ISignalScheduleTicket> findTickets(ISignalComponent component, SignalOutputHandler condition) {
        Level level = component.getStructureLevel();
        if (level != null && !level.isClientSide)
            return get(level).findTickets(condition);
        return Collections.EMPTY_LIST;
    }
    
    public static synchronized LittleSignalHandler get(ISignalComponent component) {
        return get(component.getStructureLevel());
    }
    
    public static LittleSignalHandler get(Level level) {
        return LittleTiles.SIGNAL_HANDLERS.get(level);
    }
    
    public static void schedule(Level level, ISignalSchedulable schedulable) {
        if (level == null)
            return;
        get(level).schedule(schedulable);
    }
    
    public static ISignalScheduleTicket schedule(SignalOutputHandler handler, SignalState result, int tick) {
        Level level = handler.component.getStructureLevel();
        if (level == null) {
            SignalScheduleTicket ticket = new SignalScheduleTicket(handler, result, tick);
            UNSORTED_TICKETS.add(ticket);
            return ticket;
        } else
            return get(handler.component).openTicket(handler, result, tick);
    }
    
    private final HashSet<LittleStructure> queuedUpdateStructures = new HashSet<>();
    private final HashSet<LittleStructure> queuedStructures = new HashSet<>();
    
    private int queueIndex;
    private List<ISignalSchedulable> scheduled = new ArrayList<>();
    private List<SignalScheduleTicket>[] queue;
    
    private List<SignalScheduleTicket> longQueue = new ArrayList<>();
    
    public LittleSignalHandler(Level level) {
        super(level);
        MinecraftForge.EVENT_BUS.register(this);
        queue = new List[QUEUE_LENGTH];
        for (int i = 0; i < queue.length; i++)
            queue[i] = new ArrayList<>();
        queueIndex = 0;
    }
    
    @Override
    public void unload() {
        queuedUpdateStructures.clear();
        queuedStructures.clear();
        MinecraftForge.EVENT_BUS.unregister(this);
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
            
            // Process queue
            List<SignalScheduleTicket> tickets = queue[queueIndex];
            for (int i = 0; i < tickets.size(); i++)
                tickets.get(i).run();
            tickets.clear();
            longQueue.removeIf((x) -> {
                if (x.tick() <= QUEUE_LENGTH) {
                    x.enterShortQueue(queueIndex);
                    tickets.add(x);
                    return true;
                }
                return false;
            });
            queueIndex++;
            if (queueIndex >= QUEUE_LENGTH)
                queueIndex = 0;
        }
    }
    
    public int getDelayOfQueue(int index) {
        if (index >= queueIndex)
            return queueIndex - index + 1;
        return QUEUE_LENGTH - index + queueIndex;
    }
    
    public synchronized List<ISignalScheduleTicket> findTickets(SignalOutputHandler condition) {
        List<ISignalScheduleTicket> tickets = new ArrayList<>();
        for (int i = 0; i < queue.length; i++)
            for (SignalScheduleTicket ticket : queue[i])
                if (ticket.is(condition))
                    tickets.add(ticket);
        for (SignalScheduleTicket ticket : longQueue)
            if (ticket.is(condition))
                tickets.add(ticket);
        return tickets;
    }
    
    public synchronized void schedule(ISignalSchedulable schedulable) {
        scheduled.add(schedulable);
    }
    
    public synchronized ISignalScheduleTicket openTicket(SignalScheduleTicket ticket) {
        int delay;
        int tick = ticket.getExactDelayValue();
        if (tick <= QUEUE_LENGTH) {
            if (tick <= 0)
                tick = 1;
            delay = queueIndex + tick - 1;
            if (delay >= QUEUE_LENGTH)
                delay -= QUEUE_LENGTH;
        } else
            delay = tick;
        if (tick <= QUEUE_LENGTH) {
            ticket.enterShortQueue(delay);
            queue[delay].add(ticket);
        } else
            longQueue.add(ticket);
        return ticket;
    }
    
    public synchronized ISignalScheduleTicket openTicket(SignalOutputHandler handler, SignalState result, int tick) {
        int delay;
        if (tick <= QUEUE_LENGTH) {
            if (tick <= 0)
                tick = 1;
            delay = queueIndex + tick - 1;
            if (delay >= QUEUE_LENGTH)
                delay -= QUEUE_LENGTH;
        } else
            delay = tick;
        SignalScheduleTicket ticket = new SignalScheduleTicket(handler, result, delay);
        if (tick <= QUEUE_LENGTH)
            queue[delay].add(ticket);
        else
            longQueue.add(ticket);
        return ticket;
    }
    
}
