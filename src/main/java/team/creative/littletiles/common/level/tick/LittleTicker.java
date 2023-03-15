package team.creative.littletiles.common.level.tick;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import team.creative.littletiles.common.level.handler.LevelHandler;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.schedule.ISignalSchedulable;

public class LittleTicker extends LevelHandler implements Iterable<LittleTickTicket> {
    
    private final HashSet<LittleStructure> updateStructures = new HashSet<>();
    private final HashSet<LittleStructure> tickingStructures = new HashSet<>();
    private List<ISignalSchedulable> signalChanged = new ArrayList<>();
    
    public int tick = Integer.MIN_VALUE;
    public int latest = Integer.MIN_VALUE;
    public LittleTickTicket next;
    public LittleTickTicket last;
    public LittleTickTicket unused;
    
    public LittleTicker(Level level) {
        super(level);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    protected LittleTickTicket pollUnused() {
        if (unused == null)
            return new LittleTickTicket();
        LittleTickTicket result = unused;
        unused = result.next;
        return result;
    }
    
    public synchronized void markUpdate(LittleStructure structure) {
        if (structure.isClient())
            return;
        updateStructures.add(structure);
    }
    
    public synchronized void queueNexTick(LittleStructure structure) {
        if (structure.isClient())
            return;
        tickingStructures.add(structure);
    }
    
    public synchronized void markSignalChanged(ISignalSchedulable schedulable) {
        signalChanged.add(schedulable);
    }
    
    public void schedule(int delay, Runnable run) {
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
        
        if (next.tickTime >= result.tickTime) {
            result.next = next;
            next = null;
            return;
        }
        
        LittleTickTicket current = next;
        while (current.next.tickTime <= result.tickTime)
            current = current.next;
        result.next = current.next;
        current.next = result;
    }
    
    public void tick() {
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
        
        if (!tickingStructures.isEmpty()) {
            for (Iterator<LittleStructure> iterator = tickingStructures.iterator(); iterator.hasNext();) {
                LittleStructure structure = iterator.next();
                if (!structure.queuedTick())
                    iterator.remove();
            }
        }
        
        if (!updateStructures.isEmpty()) {
            for (LittleStructure structure : updateStructures)
                structure.sendUpdatePacket();
            updateStructures.clear();
        }
        
        if (!signalChanged.isEmpty()) {
            for (ISignalSchedulable signal : signalChanged)
                try {
                    signal.updateSignaling();
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            signalChanged.clear();
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
        MinecraftForge.EVENT_BUS.unregister(this);
    }
    
}
