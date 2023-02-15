package team.creative.littletiles.common.packet.level;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;

public class LittleLevelPhysicPacket extends CreativePacket {
    
    public UUID uuid;
    public CompoundTag extraData;
    
    public LittleLevelPhysicPacket() {}
    
    public LittleLevelPhysicPacket(LittleLevelEntity entity) {
        this.uuid = entity.getUUID();
        this.extraData = entity.physic.save();
    }
    
    @Override
    public void executeClient(Player player) {
        LittleLevelEntity entity = LittleAnimationHandlers.find(player.level.isClientSide, uuid);
        if (entity == null)
            return;
        
        entity.physic.load(extraData);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
