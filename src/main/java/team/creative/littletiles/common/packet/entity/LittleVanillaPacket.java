package team.creative.littletiles.common.packet.entity;

import java.util.UUID;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.level.little.LittlePlayerConnection;

public class LittleVanillaPacket extends LittleEntityPacket<LittleEntity> {
    
    public Packet packet;
    
    public LittleVanillaPacket() {}
    
    public LittleVanillaPacket(LittleLevel level, Packet packet) {
        super(level.key());
        this.packet = packet;
    }
    
    public LittleVanillaPacket(UUID uuid, Packet packet) {
        super(uuid);
        this.packet = packet;
    }
    
    @Override
    public void execute(Player player, LittleEntity entity) {
        LittlePlayerConnection.runInContext(entity.getSubLevel(), player, x -> packet.handle(x));
    }
    
}
