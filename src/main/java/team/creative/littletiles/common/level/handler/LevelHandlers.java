package team.creative.littletiles.common.level.handler;

import java.util.HashMap;
import java.util.function.Function;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.type.itr.FilterIterator;
import team.creative.littletiles.common.level.little.LittleSubLevel;

public class LevelHandlers<T extends LevelHandler> {
    
    public static boolean isInvalidLevel(LevelAccessor level) {
        return level instanceof LittleSubLevel;
    }
    
    protected final Function<Level, T> factory;
    private HashMap<Level, T> handlers = new HashMap<>();
    
    public LevelHandlers() {
        this.factory = createFactory();
        MinecraftForge.EVENT_BUS.addListener(this::unloadEvent);
    }
    
    public LevelHandlers(Function<Level, T> factory) {
        this.factory = factory;
        MinecraftForge.EVENT_BUS.addListener(this::unloadEvent);
    }
    
    protected Function<Level, T> createFactory() {
        return null;
    }
    
    public Iterable<T> handlers(boolean client) {
        return () -> new FilterIterator<>(handlers.values(), x -> x.level.isClientSide == client);
    }
    
    public Iterable<T> handlers() {
        return handlers.values();
    }
    
    public T get(Level level) {
        if (level instanceof ISubLevel sub)
            level = sub.getRealLevel();
        T handler = handlers.get(level);
        if (handler == null) {
            handler = factory.apply(level);
            if (handler == null)
                return null;
            handlers.put(level, handler);
        }
        return handler;
    }
    
    public void unloadEvent(LevelEvent.Unload event) {
        if (isInvalidLevel(event.getLevel()))
            return;
        
        T handler = handlers.remove(event.getLevel());
        if (handler != null)
            unloadHandler(handler);
    }
    
    protected void unloadHandler(T handler) {
        handler.unload();
    }
    
}
