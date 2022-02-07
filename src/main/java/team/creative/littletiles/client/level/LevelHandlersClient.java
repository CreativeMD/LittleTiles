package team.creative.littletiles.client.level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.common.level.LevelHandler;
import team.creative.littletiles.common.level.LevelHandlers;

public class LevelHandlersClient extends LevelHandlers {
    
    private List<LevelAwareHandler> awareHandlers = new ArrayList<>();
    private List<Consumer<? extends LevelHandler>> unloaders = new ArrayList<>();
    private boolean loaded = false;
    private int slowTicker = 0;
    private int timeToCheckSlowTick = 100;
    
    public LevelHandlersClient() {
        super(true);
    }
    
    public void register(LevelAwareHandler handler) {
        awareHandlers.add(handler);
    }
    
    public <T extends LevelHandler> void register(Function<Level, T> function, Consumer<T> consumer) {
        register(x -> {
            T handler = function.apply(x);
            consumer.accept(handler);
            return handler;
        });
        unloaders.add(consumer);
    }
    
    @Override
    protected void unload(Level level) {
        super.unload(level);
        unloaders.forEach(x -> x.accept(null));
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
