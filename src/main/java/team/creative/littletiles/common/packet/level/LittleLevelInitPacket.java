package team.creative.littletiles.common.packet.level;

import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;

public class LittleLevelInitPacket extends CreativePacket {
    
    public UUID uuid;
    public StructureAbsolute absolute;
    
    public LittleLevelInitPacket() {}
    
    public LittleLevelInitPacket(LittleLevelEntity entity) {
        this.uuid = entity.getUUID();
        this.absolute = entity.getCenter();
    }
    
    @Override
    public void executeClient(Player player) {
        LittleLevelEntity entity = LittleAnimationHandlers.find(player.level.isClientSide, uuid);
        if (entity == null)
            return;
        
        entity.initSubLevelClient(this);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
