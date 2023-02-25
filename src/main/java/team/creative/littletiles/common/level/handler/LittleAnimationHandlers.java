package team.creative.littletiles.common.level.handler;

import java.util.List;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.entity.level.LittleEntity;
import team.creative.littletiles.server.LittleTilesServer;
import team.creative.littletiles.server.level.handler.LittleAnimationHandlerServer;

public class LittleAnimationHandlers extends LevelHandlers<LittleAnimationHandlerServer> {
    
    @OnlyIn(Dist.CLIENT)
    private static LittleAnimationHandler getClient() {
        return LittleTilesClient.ANIMATION_HANDLER;
    }
    
    public static LittleAnimationHandler get(Level level) {
        if (level instanceof ISubLevel sub)
            level = sub.getRealLevel();
        if (level.isClientSide)
            return getClient();
        return LittleTilesServer.ANIMATION_HANDLERS.getForLevel(level);
    }
    
    public static LittleEntity find(boolean client, UUID uuid) {
        if (client)
            return findClient(uuid);
        return findServer(uuid);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static LittleEntity findClient(UUID uuid) {
        return LittleTilesClient.ANIMATION_HANDLER.find(uuid);
    }
    
    public static LittleEntity findServer(UUID uuid) {
        for (LittleAnimationHandler handler : LittleTilesServer.ANIMATION_HANDLERS.all()) {
            LittleEntity entity = handler.find(uuid);
            if (entity != null)
                return entity;
        }
        return null;
    }
    
    public LittleAnimationHandlers() {
        super(false);
        register(LittleAnimationHandlerServer::new);
    }
    
    protected LittleAnimationHandlerServer getForLevel(Level level) {
        List<LittleAnimationHandlerServer> handlers = getHandlers(level);
        if (handlers.size() == 1)
            return handlers.get(0);
        return null;
    }
    
    @SubscribeEvent
    public void tick(LevelTickEvent event) {
        if (!event.level.isClientSide)
            getHandlers(event.level).forEach(x -> x.tickServer(event));
    }
    
    public static void setPushedByDoor(ServerPlayer entity) {
        // TODO Readd implement pushed by door
    }
    
}
