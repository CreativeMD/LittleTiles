package team.creative.littletiles.client.level;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.common.level.LevelHandlers;

public class LevelHandlersClient extends LevelHandlers {
    
    private List<LevelAwareHandler> awareHandlers = new ArrayList<>();
    private boolean loaded = false;
    private int slowTicker = 0;
    private int timeToCheckSlowTick = 100;
    
    public LevelHandlersClient() {
        super(true);
    }
    
    public void register(LevelAwareHandler handler) {
        awareHandlers.add(handler);
    }
    
    @SubscribeEvent
    public void tick(ClientTickEvent event) {
        if (event.phase == Phase.START && ((Minecraft.getInstance().player != null) != loaded)) {
            loaded = Minecraft.getInstance().player != null;
            if (!loaded)
                awareHandlers.forEach(x -> x.unload());
        }
        
        if (Minecraft.getInstance().player != null) {
            slowTicker++;
            if (slowTicker >= timeToCheckSlowTick) {
                awareHandlers.forEach(x -> x.slowTick());
                slowTicker = 0;
            }
        }
    }
}
