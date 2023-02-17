package team.creative.littletiles.server.level.little;

import java.util.HashMap;

import net.minecraft.server.level.ServerPlayer;

public class LittleServerPlayerConnections {
    
    private HashMap<ServerPlayer, LittleServerPlayerHandler> listeners = new HashMap<>();
    
    public LittleServerPlayerConnections() {}
    
    public void remove(ServerPlayer player) {
        listeners.remove(player);
    }
    
    public LittleServerPlayerHandler getOrCreate(LittleServerLevel level, ServerPlayer player) {
        LittleServerPlayerHandler listener = listeners.get(player);
        if (listener == null)
            listeners.put(player, listener = new LittleServerPlayerHandler(level, player));
        return listener;
    }
    
    public void tick() {
        for (LittleServerPlayerHandler handler : listeners.values())
            handler.tick();
    }
    
}
