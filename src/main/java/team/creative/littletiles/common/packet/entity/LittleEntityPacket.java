package team.creative.littletiles.common.packet.entity;

import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;

public abstract class LittleEntityPacket<T extends LittleEntity> extends CreativePacket {
    
    public UUID uuid;
    
    public LittleEntityPacket() {}
    
    public LittleEntityPacket(UUID uuid) {
        this.uuid = uuid;
    }
    
    public LittleEntityPacket(T entity) {
        this(entity.getUUID());
    }
    
    public abstract void execute(Player player, T entity);
    
    @Override
    public void execute(Player player) {
        LittleEntity entity = LittleAnimationHandlers.find(player.level.isClientSide, uuid);
        if (entity == null)
            return;
        
        try {
            execute(player, (T) entity);
        } catch (ClassCastException e) {}
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {}
}
