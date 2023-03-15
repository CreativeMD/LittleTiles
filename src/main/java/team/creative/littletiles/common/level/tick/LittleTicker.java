package team.creative.littletiles.common.level.tick;

import java.util.Iterator;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import team.creative.littletiles.common.level.handler.LevelHandler;

public class LittleTicker extends LevelHandler implements Iterable<LittleTickTicket> {
    
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
