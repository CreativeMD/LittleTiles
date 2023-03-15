package team.creative.littletiles.common.level.tick;

public class LittleTickTicket {
    
    public LittleTickTicket next;
    public int tickTime;
    private Runnable run;
    
    public void run() {
        run.run();
        run = null;
    }
    
    public void setup(int tick, Runnable run) {
        this.tickTime = tick;
        this.run = run;
    }
    
    public Runnable get() {
        return run;
    }
    
}
