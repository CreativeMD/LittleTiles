package team.creative.littletiles.common.structure.signal.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;

public class SignalTicker {
    
    private static HashMap<Level, SignalTicker> tickers = new HashMap<>();
    private static List<SignalScheduleTicket> unsortedTickets = new ArrayList<>();
    public static final int queueLength = 20;
    
    public static synchronized void serverTick() {
        if (unsortedTickets.isEmpty())
            return;
        for (Iterator<SignalScheduleTicket> iterator = unsortedTickets.iterator(); iterator.hasNext();) {
            SignalScheduleTicket ticket = iterator.next();
            Level level = ticket.getLevel();
            if (level != null) {
                get(level).openTicket(ticket);
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
    
    public static synchronized SignalTicker get(ISignalComponent component) {
        return get(component.getStructureLevel());
    }
    
    public static synchronized SignalTicker get(Level level) {
        if (level.isClientSide)
            throw new RuntimeException("Client should never ask for a signal ticker");
        if (level instanceof ISubLevel)
            level = ((ISubLevel) level).getRealLevel();
        SignalTicker ticker = tickers.get(level);
        if (ticker == null) {
            ticker = new SignalTicker(level);
            tickers.put(level, ticker);
        }
        return ticker;
    }
    
    public static void schedule(Level level, ISignalSchedulable schedulable) {
        if (level == null)
            return;
        get(level).schedule(schedulable);
    }
    
    public static ISignalScheduleTicket schedule(SignalOutputHandler handler, boolean[] result, int tick) {
        Level level = handler.component.getStructureLevel();
        if (level == null) {
            SignalScheduleTicket ticket = new SignalScheduleTicket(handler, result, tick);
            unsortedTickets.add(ticket);
            return ticket;
        } else
            return get(handler.component).openTicket(handler, result, tick);
    }
    
    private static synchronized void unload(SignalTicker ticker) {
        MinecraftForge.EVENT_BUS.unregister(ticker);
        tickers.remove(ticker.level);
    }
    
    public final Level level;
    private int queueIndex;
    private List<ISignalSchedulable> scheduled = new ArrayList<>();
    private List<SignalScheduleTicket>[] queue;
    
    private List<SignalScheduleTicket> longQueue = new ArrayList<>();
    
    protected SignalTicker(Level level) {
        this.level = level;
        queue = new List[queueLength];
        for (int i = 0; i < queue.length; i++)
            queue[i] = new ArrayList<>();
        queueIndex = 0;
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public synchronized void tick(WorldTickEvent event) {
        if (event.phase == Phase.END && level == event.world) {
            
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
                if (x.tick() <= queueLength) {
                    x.enterShortQueue(queueIndex);
                    tickets.add(x);
                    return true;
                }
                return false;
            });
            queueIndex++;
            if (queueIndex >= queueLength)
                queueIndex = 0;
        }
    }
    
    @SubscribeEvent
    public void levelUnload(WorldEvent.Unload event) {
        if (event.getWorld() == level)
            unload(this);
    }
    
    public int getDelayOfQueue(int index) {
        if (index >= queueIndex)
            return queueIndex - index + 1;
        return queueLength - index + queueIndex;
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
        if (tick <= queueLength) {
            delay = queueIndex + tick - 1;
            if (delay >= queueLength)
                delay -= queueLength;
        } else
            delay = tick;
        if (tick <= queueLength) {
            ticket.enterShortQueue(delay);
            queue[delay].add(ticket);
        } else
            longQueue.add(ticket);
        return ticket;
    }
    
    public synchronized ISignalScheduleTicket openTicket(SignalOutputHandler handler, boolean[] result, int tick) {
        int delay;
        if (tick <= queueLength) {
            delay = queueIndex + tick - 1;
            if (delay >= queueLength)
                delay -= queueLength;
        } else
            delay = tick;
        SignalScheduleTicket ticket = new SignalScheduleTicket(handler, result, delay);
        if (tick <= queueLength)
            queue[delay].add(ticket);
        else
            longQueue.add(ticket);
        return ticket;
    }
    
}
