package team.creative.littletiles.common.level.tick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.level.handler.LevelHandlers;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;
import team.creative.littletiles.common.structure.signal.schedule.ISignalSchedulable;
import team.creative.littletiles.common.structure.signal.schedule.SignalScheduleTicket;

public class LittleTickers extends LevelHandlers<LittleTicker> {
    
    private static final List<SignalScheduleTicket> UNSORTED_TICKETS = new ArrayList<>();
    private LittleTicker client;
    
    public LittleTickers() {
        super();
        MinecraftForge.EVENT_BUS.addListener(this::levelTick);
        MinecraftForge.EVENT_BUS.addListener(this::serverTick);
    }
    
    @Override
    protected Function<Level, LittleTicker> createFactory() {
        return level -> {
            LittleTicker ticker = new LittleTicker(level);
            if (level.isClientSide)
                client = ticker;
            return ticker;
        };
    }
    
    @Override
    protected void unloadHandler(LittleTicker handler) {
        super.unloadHandler(handler);
        if (handler == client)
            client = null;
    }
    
    public void levelTick(LevelTickEvent event) {
        if (event.phase == Phase.END) {
            LittleTicker ticker = getWithoutCreate(event.level);
            if (ticker != null)
                ticker.tick();
        }
    }
    
    public void serverTick(ServerTickEvent event) {
        if (!UNSORTED_TICKETS.isEmpty())
            for (Iterator<SignalScheduleTicket> iterator = UNSORTED_TICKETS.iterator(); iterator.hasNext();) {
                SignalScheduleTicket ticket = iterator.next();
                Level level = ticket.getLevel();
                if (level != null) {
                    LittleTiles.TICKERS.get(level).schedule(ticket.getDelay(), ticket);
                    iterator.remove();
                }
            }
    }
    
    public synchronized List<SignalScheduleTicket> findTickets(ISignalComponent component, SignalOutputHandler condition) {
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
    
    public void markSignalChanged(Level level, ISignalSchedulable schedulable) {
        if (level == null)
            return;
        get(level).markSignalChanged(schedulable);
    }
    
    public synchronized SignalScheduleTicket schedule(SignalOutputHandler handler, SignalState result, int delay) {
        SignalScheduleTicket ticket = new SignalScheduleTicket(handler, result, delay);
        Level level = handler.component.getStructureLevel();
        if (level == null)
            UNSORTED_TICKETS.add(ticket);
        else
            LittleTiles.TICKERS.get(level).schedule(delay, ticket);
        return ticket;
    }
    
    public synchronized void markUpdate(LittleStructure structure, boolean notifyNeighbours) {
        get(structure.getStructureLevel()).markUpdate(structure, notifyNeighbours);
    }
    
    public synchronized void queueNexTick(LittleStructure structure) {
        get(structure.getStructureLevel()).queueNextTick(structure);
    }
    
}
