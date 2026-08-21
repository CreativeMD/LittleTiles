package team.creative.littletiles.common.level.tick;

import team.creative.littletiles.common.structure.signal.schedule.SignalScheduleTicket;

public class LittleTickTicket {
    
    public LittleTickTicket next;
    public int tickTime;
    private SignalScheduleTicket run;
    private LittleTicker ticker;
    
    public void run() {
        run.run();
        run = null;
        ticker = null;
    }
    
    public void setup(int tick, SignalScheduleTicket run, LittleTicker ticker) {
        this.tickTime = tick;
        this.run = run;
        this.ticker = ticker;
        
        this.run.scheduled(this);
    }
    
    public int timeTillExecution() {
        return tickTime - ticker.tick;
    }
    
    public Runnable get() {
        return run;
    }
    
}
