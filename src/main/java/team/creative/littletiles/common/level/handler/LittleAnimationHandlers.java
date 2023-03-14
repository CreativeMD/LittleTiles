package team.creative.littletiles.common.level.handler;

import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.LittleAnimationHandlerClient;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.server.level.handler.LittleAnimationHandlerServer;

public class LittleAnimationHandlers extends LevelHandlers<LittleAnimationHandler> {
    
    @OnlyIn(Dist.CLIENT)
    private static LittleAnimationHandler createClient(Level level) {
        return LittleTilesClient.ANIMATION_HANDLER = new LittleAnimationHandlerClient(level);
    }
    
    public static void setPushedByDoor(ServerPlayer entity) {
        // TODO Readd implement pushed by door
    }
    
    public LittleAnimationHandlers() {
        super(level -> {
            if (level.isClientSide)
                return createClient(level);
            return new LittleAnimationHandlerServer(level);
        });
        MinecraftForge.EVENT_BUS.addListener(this::tick);
    }
    
    public LittleEntity find(boolean client, UUID uuid) {
        for (LittleAnimationHandler handler : handlers(client)) {
            LittleEntity entity = handler.find(uuid);
            if (entity != null)
                return entity;
        }
        return null;
    }
    
    @SubscribeEvent
    public void tick(LevelTickEvent event) {
        get(event.level).tick(event);
    }
    
}
