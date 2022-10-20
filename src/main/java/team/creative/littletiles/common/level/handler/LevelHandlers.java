package team.creative.littletiles.common.level.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.util.type.map.HashMapList;

public class LevelHandlers<T extends LevelHandler> {
    
    private List<Function<Level, T>> factories = new ArrayList<>();
    private HashMapList<Level, T> handlers = new HashMapList<>();
    public final boolean client;
    
    public LevelHandlers(boolean client) {
        this.client = client;
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    protected Iterable<T> all() {
        return handlers;
    }
    
    protected List<T> getHandlers(Level level) {
        return handlers.get(level);
    }
    
    public void register(Function<Level, T> function) {
        factories.add(function);
    }
    
    protected void load(Level level) {
        List<T> levelHandlers = handlers.removeKey(level);
        if (levelHandlers != null)
            throw new RuntimeException("This should not happen");
        
        List<T> newHandlers = new ArrayList<>(factories.size());
        for (Function<Level, T> func : factories)
            newHandlers.add(func.apply(level));
        handlers.add(level, newHandlers);
        for (LevelHandler handler : newHandlers)
            handler.load();
    }
    
    protected void unload(Level level) {
        List<T> levelHandlers = handlers.removeKey(level);
        if (levelHandlers != null)
            for (LevelHandler handler : levelHandlers)
                handler.unload();
    }
    
    @SubscribeEvent
    public void load(LevelEvent.Load event) {
        if (event.getLevel().isClientSide() != client)
            return;
        load((Level) event.getLevel());
    }
    
    @SubscribeEvent
    public void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide() != client)
            return;
        unload((Level) event.getLevel());
    }
    
}
