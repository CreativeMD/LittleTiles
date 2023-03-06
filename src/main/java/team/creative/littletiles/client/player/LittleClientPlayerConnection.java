package team.creative.littletiles.client.player;

import java.util.function.Consumer;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.Level;
import team.creative.littletiles.client.level.LevelAwareHandler;
import team.creative.littletiles.common.level.little.LittleLevel;

public class LittleClientPlayerConnection implements LevelAwareHandler {
    
    private LittleClientPlayerHandler INSTANCE;
    
    private LittleClientPlayerHandler get() {
        if (INSTANCE == null)
            INSTANCE = new LittleClientPlayerHandler();
        return INSTANCE;
    }
    
    public void send(Level level, Packet packet) {
        send((LittleLevel) level, packet);
    }
    
    public void send(LittleLevel level, Packet packet) {
        LittleClientPlayerHandler listener = get();
        synchronized (listener) {
            Level previous = listener.level;
            listener.level = level.asLevel();
            listener.send(packet);
            listener.level = previous;
        }
    }
    
    public void runInContext(LittleLevel level, Consumer<LittleClientPlayerHandler> consumer) {
        LittleClientPlayerHandler listener = get();
        synchronized (listener) {
            Level previous = listener.level;
            listener.level = level.asLevel();
            consumer.accept(listener);
            listener.level = previous;
        }
    }
    
    @Override
    public void unload() {
        INSTANCE = null;
    }
    
    @Override
    public void slowTick() {
        if (INSTANCE != null)
            INSTANCE.tick();
    }
    
}
