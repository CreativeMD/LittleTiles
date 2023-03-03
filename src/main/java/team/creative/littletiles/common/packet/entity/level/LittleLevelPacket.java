package team.creative.littletiles.common.packet.entity.level;

import java.util.UUID;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;
import team.creative.littletiles.common.entity.level.LittleLevelPacketListener;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.packet.entity.LittleEntityPacket;

public class LittleLevelPacket extends LittleEntityPacket<LittleLevelEntity> {
    
    public Packet packet;
    
    public LittleLevelPacket() {}
    
    public LittleLevelPacket(LittleLevel level, Packet packet) {
        super(level.key());
        this.packet = packet;
    }
    
    public LittleLevelPacket(UUID uuid, Packet packet) {
        super(uuid);
        this.packet = packet;
    }
    
    @Override
    public void execute(Player player, LittleLevelEntity entity) {
        packet.handle(((LittleLevelPacketListener) entity.getSubLevel()).getPacketListener(player));
    }
    
}
