package team.creative.littletiles.common.level.handler;

import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StopTracking;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import team.creative.littletiles.LittleTiles;
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
        NeoForge.EVENT_BUS.addListener(this::tick);
        NeoForge.EVENT_BUS.addListener(this::trackEntity);
        NeoForge.EVENT_BUS.addListener(this::stopTrackEntity);
    }
    
    public LittleEntity find(boolean client, UUID uuid) {
        if (uuid == null)
            return null;
        for (LittleAnimationHandler handler : handlers(client)) {
            LittleEntity entity = handler.find(uuid);
            if (entity != null)
                return entity;
        }
        return null;
    }
    
    public void tick(LevelTickEvent.Post event) {
        get(event.getLevel()).tick();
    }
    
    public void trackEntity(StartTracking event) {
        if (event.getTarget() instanceof LittleEntity levelEntity) {
            LittleTiles.NETWORK.sendToClient(levelEntity.initClientPacket(), (ServerPlayer) event.getEntity());
            levelEntity.startTracking((ServerPlayer) event.getEntity());
        }
    }
    
    public void stopTrackEntity(StopTracking event) {
        if (event.getTarget() instanceof LittleEntity levelEntity)
            levelEntity.stopTracking((ServerPlayer) event.getEntity());
    }
    
}
