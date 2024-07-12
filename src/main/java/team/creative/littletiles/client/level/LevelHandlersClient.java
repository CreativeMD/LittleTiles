package team.creative.littletiles.client.level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import team.creative.littletiles.common.level.handler.LevelHandler;
import team.creative.littletiles.common.level.handler.LevelHandlers;

public class LevelHandlersClient {
    
    private List<LevelAwareHandler> awareHandlers = new ArrayList<>();
    private List<LevelHandlerClient> handlers = new ArrayList<>();
    
    public LevelHandlersClient() {
        NeoForge.EVENT_BUS.addListener(this::load);
        NeoForge.EVENT_BUS.addListener(this::unload);
    }
    
    public void register(LevelAwareHandler handler) {
        awareHandlers.add(handler);
    }
    
    public <T extends LevelHandler> void register(Function<Level, T> function, Consumer<T> consumer) {
        handlers.add(new LevelHandlerClient<>(function, consumer));
    }
    
    public void load(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide() || LevelHandlers.isInvalidLevel(event.getLevel()))
            return;
        
        for (int i = 0; i < handlers.size(); i++)
            handlers.get(i).load((Level) event.getLevel());
        
        awareHandlers.forEach(x -> x.unload());
    }
    
    public void unload(ClientPlayerNetworkEvent.LoggingOut event) {
        for (int i = 0; i < handlers.size(); i++)
            handlers.get(i).unload();
    }
    
    private static record LevelHandlerClient<T extends LevelHandler>(Function<Level, T> factory, Consumer<T> consumer) {
        
        public void load(Level level) {
            consumer.accept(factory.apply(level));
        }
        
        public void unload() {
            consumer.accept(null);
        }
    }
}
