package team.creative.littletiles.common.level.tick;

import java.util.function.Function;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import team.creative.littletiles.common.level.handler.LevelHandlers;
import team.creative.littletiles.common.structure.signal.LittleSignalHandler;

public class LittleTickers extends LevelHandlers<LittleTicker> {
    
    private LittleTicker client;
    
    public LittleTickers() {
        super();
    }
    
    @Override
    protected Function<Level, LittleTicker> createFactory() {
        return level -> {
            LittleTicker ticker = new LittleTicker(level);
            if (level.isClientSide)
                client = ticker;
            return ticker;
        };
    }
    
    @Override
    protected void unloadHandler(LittleTicker handler) {
        super.unloadHandler(handler);
        if (handler == client)
            client = null;
    }
    
    public void clientTick(ClientTickEvent event) {
        if (event.phase == Phase.END && client != null)
            client.tick();
    }
    
    public void serverTick(ServerTickEvent event) {
        if (event.phase == Phase.END)
            for (LittleTicker ticker : handlers())
                if (ticker != client)
                    ticker.tick();
                
        LittleSignalHandler.serverTick();
    }
    
}
