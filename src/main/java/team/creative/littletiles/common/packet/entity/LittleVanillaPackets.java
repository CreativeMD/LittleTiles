package team.creative.littletiles.common.packet.entity;

import java.util.Arrays;
import java.util.List;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.level.little.LittlePlayerConnection;

public class LittleVanillaPackets extends LittleEntityPacket<LittleEntity> {
    
    public List<? extends Packet> packets;
    
    public LittleVanillaPackets() {}
    
    public LittleVanillaPackets(LittleLevel level, List<? extends Packet> packets) {
        super(level.key());
        this.packets = packets;
    }
    
    public LittleVanillaPackets(LittleLevel level, Packet... packets) {
        super(level.key());
        this.packets = Arrays.asList(packets);
    }
    
    @Override
    public void execute(Player player, LittleEntity entity) {
        LittlePlayerConnection.runInContext(entity.getSubLevel(), player, x -> {
            for (Packet packet : packets)
                packet.handle(x);
        });
    }
    
}
