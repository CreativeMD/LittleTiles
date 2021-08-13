package team.creative.littletiles.common.level;

import java.util.LinkedHashMap;
import java.util.function.Function;

import net.minecraft.world.level.Level;

public abstract class LevelHandlers {
    
    private LinkedHashMap<Class<? extends LevelHandler>, LevelHandlerInstance> handlers = new LinkedHashMap<>();
    
    public <T extends LevelHandler> void register(Class<T> clazz, Function<Level, T> function) {
        handlers.put(clazz, new LevelHandlerInstance<>(function));
    }
    
    protected void loadLevel(Level level) {
        for (LevelHandlerInstance instance : handlers.values())
            instance.load(level);
    }
    
    protected void unloadLevel() {
        for (LevelHandlerInstance instance : handlers.values())
            instance.unload();
    }
    
    private static class LevelHandlerInstance<T extends LevelHandler> {
        
        public final Function<Level, T> factory;
        public T instance;
        
        public LevelHandlerInstance(Function<Level, T> factory) {
            this.factory = factory;
        }
        
        public void load(Level level) {
            this.instance = factory.apply(level);
            this.instance.load();
        }
        
        public void unload() {
            instance.unload();
            instance = null;
        }
        
    }
    
}
