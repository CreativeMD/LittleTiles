package team.creative.littletiles.common.level.tick;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry;
import net.minecraft.world.level.Level;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.level.handler.LevelHandler;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.schedule.ISignalSchedulable;

public class LittleTicker extends LevelHandler implements Iterable<LittleTickTicket> {
    
    private final Object2BooleanMap<LittleStructure> updateStructures = new Object2BooleanLinkedOpenHashMap<>();
    private final HashSet<LittleStructure> tickingStructures = new HashSet<>();
    private boolean ticking = false;
    private final List<LittleStructure> queuedTickingStructures = new ArrayList<>();
    private List<ISignalSchedulable> signalChanged = new ArrayList<>();
    private boolean processingChanged = false;
    private List<ISignalSchedulable> signalChangedSchedule = new ArrayList<>();
    
    public int tick = Integer.MIN_VALUE;
    public int latest = Integer.MIN_VALUE;
    public LittleTickTicket next;
    public LittleTickTicket last;
    public LittleTickTicket unused;
    
    public LittleTicker(Level level) {
        super(level);
    }
    
    protected LittleTickTicket pollUnused() {
        if (unused == null)
            return new LittleTickTicket();
        LittleTickTicket result = unused;
        unused = result.next;
        return result;
    }
    
    public void markUpdate(LittleStructure structure, boolean notifyNeighbours) {
        if (structure.isClient())
            return;
        synchronized (updateStructures) {
            if (notifyNeighbours)
                updateStructures.put(structure, true);
            else
                updateStructures.put(structure, updateStructures.getBoolean(structure));
        }
    }
    
    public void queueNextTick(LittleStructure structure) {
        synchronized (tickingStructures) {
            if (ticking)
                queuedTickingStructures.add(structure);
            else
                tickingStructures.add(structure);
        }
    }
    
    public void markSignalChanged(ISignalSchedulable schedulable) {
        synchronized (signalChanged) {
            if (processingChanged)
                signalChangedSchedule.add(schedulable);
            else
                signalChanged.add(schedulable);
        }
    }
    
    public void schedule(int delay, Runnable run) {
        synchronized (this) {
            if (delay < 0)
                run.run();
            LittleTickTicket result = pollUnused();
            result.setup(delay + tick, run);
            if (latest < result.tickTime) {
                if (last != null)
                    last.next = result;
                last = result;
                latest = result.tickTime;
                return;
            }
            
            if (next == null || next.tickTime >= result.tickTime) {
                result.next = next;
                next = result;
                return;
            }
            
            LittleTickTicket current = next;
            while (current.next.tickTime <= result.tickTime)
                current = current.next;
            result.next = current.next;
            current.next = result;
        }
    }
    
    public void tick() {
        synchronized (this) {
            while (next != null && next.tickTime <= tick) {
                next.run();
                if (next == last)
                    last = null;
                LittleTickTicket temp = next;
                next = temp.next;
                if (unused != null)
                    temp.next = unused;
                unused = temp;
            }
            tick++;
        }
        
        synchronized (tickingStructures) {
            if (!queuedTickingStructures.isEmpty()) {
                for (LittleStructure structure : queuedTickingStructures)
                    tickingStructures.add(structure);
                queuedTickingStructures.clear();
            }
            ticking = true;
            if (!tickingStructures.isEmpty()) {
                for (Iterator<LittleStructure> iterator = tickingStructures.iterator(); iterator.hasNext();) {
                    LittleStructure structure = iterator.next();
                    if (!structure.queuedTick())
                        iterator.remove();
                }
            }
            ticking = false;
        }
        
        if (!updateStructures.isEmpty()) {
            synchronized (updateStructures) {
                for (Entry<LittleStructure> entry : updateStructures.object2BooleanEntrySet()) {
                    LittleStructure structure = entry.getKey();
                    if (structure.mainBlock.isRemoved())
                        continue;
                    LittleTiles.NETWORK.sendToClient(structure.generateUpdatePacket(entry.getBooleanValue()), structure.getStructureLevel(), structure.getStructurePos());
                }
                
                updateStructures.clear();
            }
        }
        
        if (!signalChanged.isEmpty()) {
            synchronized (signalChanged) {
                processingChanged = true;
                for (ISignalSchedulable signal : signalChanged)
                    try {
                        signal.updateSignaling();
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                signalChanged.clear();
                processingChanged = false;
                signalChanged.addAll(signalChangedSchedule);
                signalChangedSchedule.clear();
            }
        }
    }
    
    @Override
    public Iterator<LittleTickTicket> iterator() {
        return new Iterator<LittleTickTicket>() {
            
            public LittleTickTicket next = LittleTicker.this.next;
            
            @Override
            public boolean hasNext() {
                return next != null;
            }
            
            @Override
            public LittleTickTicket next() {
                LittleTickTicket ticket = next;
                next = next.next;
                return ticket;
            }
        };
    }
    
    @Override
    public void unload() {
        super.unload();
    }
    
}
