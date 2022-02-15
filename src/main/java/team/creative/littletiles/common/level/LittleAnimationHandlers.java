package team.creative.littletiles.common.level;

import java.util.List;
import java.util.UUID;

import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.animation.entity.LittleLevelEntity;
import team.creative.littletiles.common.event.GetVoxelShapesEvent;
import team.creative.littletiles.server.LittleTilesServer;
import team.creative.littletiles.server.level.LittleAnimationHandlerServer;

public class LittleAnimationHandlers extends LevelHandlers<LittleAnimationHandlerServer> {
    
    public static LittleAnimationHandler get(Level level) {
        if (level.isClientSide)
            return LittleTilesClient.ANIMATION_HANDLER;
        return LittleTilesServer.ANIMATION_HANDLERS.getForLevel(level);
    }
    
    public static LittleLevelEntity find(boolean client, UUID uuid) {
        if (client)
            return findClient(uuid);
        return findServer(uuid);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static LittleLevelEntity findClient(UUID uuid) {
        return LittleTilesClient.ANIMATION_HANDLER.find(uuid);
    }
    
    public static LittleLevelEntity findServer(UUID uuid) {
        for (LittleAnimationHandler handler : LittleTilesServer.ANIMATION_HANDLERS.all()) {
            LittleLevelEntity entity = handler.find(uuid);
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
    public void trackEntity(StartTracking event) {
        //if (event.getTarget() instanceof LittleLevelEntity entity)
        // TODO SEND ENTITY INFORMATION TO CLIENT
    }
    
    @SubscribeEvent
    public void chunkUnload(ChunkEvent.Unload event) {
        getHandlers((Level) event.getWorld()).forEach(x -> x.chunkUnload(event));
    }
    
    @SubscribeEvent
    public void tick(WorldTickEvent event) {
        if (!event.world.isClientSide)
            getHandlers(event.world).forEach(x -> x.tick(event));
    }
    
    @SubscribeEvent
    public void collision(GetVoxelShapesEvent event) {
        if (!event.level.isClientSide)
            getHandlers(event.level).forEach(x -> x.collision(event));
    }
    
}
